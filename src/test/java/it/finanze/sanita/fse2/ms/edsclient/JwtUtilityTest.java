/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (C) 2023 Ministero della Salute
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package it.finanze.sanita.fse2.ms.edsclient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import it.finanze.sanita.fse2.ms.edsclient.config.Constants;
import it.finanze.sanita.fse2.ms.edsclient.utility.JwtUtility;

/**
 * Unit test for {@link JwtUtility}. Built as a plain JUnit test (no Spring context) so it
 * verifies the token-building logic without requiring MongoDB / broker infrastructure —
 * {@link JwtUtility} has no dependencies.
 */
class JwtUtilityTest {

    private JwtUtility jwtUtility;

    @BeforeEach
    void setUp() {
        jwtUtility = new JwtUtility();
    }

    /**
     * Parses an unsigned ({@code alg: none}) token produced by {@link JwtUtility} and returns
     * its claims. The parser is put into unsecured mode explicitly, since jjwt rejects
     * unsecured JWTs by default.
     */
    private Claims parseUnsigned(String token) {
        Jwt<?, Claims> jwt = Jwts.parser().unsecured().build().parseUnsecuredClaims(token);
        return jwt.getPayload();
    }

    @Test
    @DisplayName("generateToken(Map) carries each supplied claim in an unsigned token")
    void generateTokenWithClaims() {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("sub", "RSSMRA22A01A399Z");
        claims.put("subject_role", "AAS");
        claims.put("person_id", "BMTBTS01A01I526W");
        claims.put("purpose_of_use", "TREATMENT");
        claims.put("locality", "201123456");
        claims.put("subject_organization", "Regione Marche");
        claims.put("subject_organization_id", "110");

        String token = jwtUtility.generateToken(claims);
        assertNotNull(token);

        Claims parsed = parseUnsigned(token);
        for (Map.Entry<String, Object> entry : claims.entrySet()) {
            assertEquals(entry.getValue(), parsed.get(entry.getKey()),
                    "claim " + entry.getKey() + " should be preserved");
        }
        assertFalse(parsed.containsKey("delegation_scope"), "delegation_scope must be absent when not supplied");
    }

    @Test
    @DisplayName("generateToken(null) falls back to the simple subject_role=GTW token")
    void generateTokenNullFallsBack() {
        Claims parsed = parseUnsigned(jwtUtility.generateToken((Map<String, Object>) null));
        assertEquals(Constants.AppConstants.SUBJECT_ROLE_GTW, parsed.get("subject_role"));
    }

    @Test
    @DisplayName("reSignToken preserves the payload claims and omits delegation_scope when absent")
    void reSignTokenPreservesClaims() {
        // Build an inbound unsigned token as if produced by an upstream party.
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", "RSSMRA22A01A399Z");
        payload.put("subject_role", "AAS");
        payload.put("person_id", "BMTBTS01A01I526W");
        payload.put("purpose_of_use", "TREATMENT");
        payload.put("locality", "201123456");
        payload.put("subject_organization", "Regione Marche");
        payload.put("subject_organization_id", "110");
        payload.put("patient_consent", true); // not in JWT_PAYLOAD_CLAIMS -> must be dropped
        String inbound = Jwts.builder()
                .issuer("upstream-issuer")
                .claims().add(payload).and()
                .compact();

        String reSigned = jwtUtility.reSignToken(inbound);
        assertNotNull(reSigned);

        // Re-packaged into a fresh unsigned token carrying only the allowed payload claims.
        Claims parsed = parseUnsigned(reSigned);
        assertEquals("RSSMRA22A01A399Z", parsed.get("sub"));
        assertEquals("AAS", parsed.get("subject_role"));
        assertEquals("BMTBTS01A01I526W", parsed.get("person_id"));
        assertEquals("TREATMENT", parsed.get("purpose_of_use"));
        assertEquals("201123456", parsed.get("locality"));
        assertEquals("Regione Marche", parsed.get("subject_organization"));
        assertEquals("110", parsed.get("subject_organization_id"));
        assertFalse(parsed.containsKey("delegation_scope"), "delegation_scope must be absent when not present inbound");
        assertFalse(parsed.containsKey("patient_consent"), "claims outside JWT_PAYLOAD_CLAIMS must be dropped");
    }

    @Test
    @DisplayName("reSignToken forwards delegation_scope when present inbound")
    void reSignTokenForwardsDelegationScope() {
        String payloadJson = "{\"sub\":\"RSSMRA22A01A399Z\",\"delegation_scope\":\"CAREGIVER\"}";
        String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        String encodedHeader = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));
        String inbound = encodedHeader + "." + encodedPayload + ".";

        Claims parsed = parseUnsigned(jwtUtility.reSignToken(inbound));
        assertEquals("RSSMRA22A01A399Z", parsed.get("sub"));
        assertEquals("CAREGIVER", parsed.get("delegation_scope"));
    }

    @Test
    @DisplayName("reSignToken falls back to the simple token on a malformed input")
    void reSignTokenMalformedFallsBack() {
        Claims parsed = parseUnsigned(jwtUtility.reSignToken("not-a-jwt"));
        assertEquals(Constants.AppConstants.SUBJECT_ROLE_GTW, parsed.get("subject_role"));
    }

    @Test
    @DisplayName("generateToken() with no claims produces an unsigned token with iat and no iss/exp")
    void generateSimpleToken() {
        Claims parsed = parseUnsigned(jwtUtility.generateToken());
        assertEquals(Constants.AppConstants.SUBJECT_ROLE_GTW, parsed.get("subject_role"));
        assertNotNull(parsed.getIssuedAt());
        assertNull(parsed.getIssuer());
        assertNull(parsed.getExpiration());
    }
}
