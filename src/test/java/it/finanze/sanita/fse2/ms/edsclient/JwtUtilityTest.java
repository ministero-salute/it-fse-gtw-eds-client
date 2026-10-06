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
 * Unit test per {@link JwtUtility}. Eseguito senza contesto Spring (no MongoDB / broker)
 * poiché {@link JwtUtility} non ha dipendenze esterne.
 *
 * <p>Verifica i due flussi pubblici distinti:</p>
 * <ul>
 *   <li>{@link JwtUtility#buildTokenFromClaims(Map)} — flusso da Mongo</li>
 *   <li>{@link JwtUtility#buildTokenFromInboundJwt(String)} — flusso da request</li>
 * </ul>
 */
class JwtUtilityTest {

    private JwtUtility jwtUtility;

    @BeforeEach
    void setUp() {
        jwtUtility = new JwtUtility();
    }

    /**
     * Parsea un token non firmato ({@code alg: none}) prodotto da {@link JwtUtility}
     * e restituisce le sue claim. Il parser viene messo in modalità unsecured
     * esplicitamente perché jjwt rifiuta i token non firmati per default.
     */
    private Claims parseUnsigned(String token) {
        Jwt<?, Claims> jwt = Jwts.parser().unsecured().build().parseUnsecuredClaims(token);
        return jwt.getPayload();
    }

    // -------------------------------------------------------------------------
    // buildTokenFromClaims — flusso da Mongo
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("buildTokenFromClaims: porta tutte le claim e forza subject_role a GTW")
    void buildTokenFromClaims_preservesClaimsAndForcesGtw() {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("sub", "RSSMRA22A01A399Z");
        claims.put("subject_role", "AAS"); // valore upstream — deve essere sovrascritto
        claims.put("person_id", "BMTBTS01A01I526W");
        claims.put("purpose_of_use", "TREATMENT");
        claims.put("locality", "201123456");
        claims.put("subject_organization", "Regione Marche");
        claims.put("subject_organization_id", "110");

        String token = jwtUtility.buildTokenFromClaims(claims);
        assertNotNull(token);

        Claims parsed = parseUnsigned(token);
        // subject_role deve sempre essere rimappato a GTW
        assertEquals(Constants.AppConstants.SUBJECT_ROLE_GTW, parsed.get("subject_role"),
                "subject_role deve essere forzato a GTW");
        // tutte le altre claim devono essere preserved invariate
        assertEquals("RSSMRA22A01A399Z", parsed.get("sub"));
        assertEquals("BMTBTS01A01I526W", parsed.get("person_id"));
        assertEquals("TREATMENT", parsed.get("purpose_of_use"));
        assertEquals("201123456", parsed.get("locality"));
        assertEquals("Regione Marche", parsed.get("subject_organization"));
        assertEquals("110", parsed.get("subject_organization_id"));
        assertFalse(parsed.containsKey("delegation_scope"), "delegation_scope non deve essere presente se non fornito");
    }

    @Test
    @DisplayName("buildTokenFromClaims(null): fallback al token minimale con subject_role=GTW")
    void buildTokenFromClaims_nullFallsBack() {
        Claims parsed = parseUnsigned(jwtUtility.buildTokenFromClaims(null));
        assertEquals(Constants.AppConstants.SUBJECT_ROLE_GTW, parsed.get("subject_role"));
        assertNotNull(parsed.getIssuedAt());
        assertNull(parsed.getIssuer());
        assertNull(parsed.getExpiration());
    }

    @Test
    @DisplayName("buildTokenFromClaims(empty): fallback al token minimale con subject_role=GTW")
    void buildTokenFromClaims_emptyFallsBack() {
        Claims parsed = parseUnsigned(jwtUtility.buildTokenFromClaims(new LinkedHashMap<>()));
        assertEquals(Constants.AppConstants.SUBJECT_ROLE_GTW, parsed.get("subject_role"));
    }

    // -------------------------------------------------------------------------
    // buildTokenFromInboundJwt — flusso da request
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("buildTokenFromInboundJwt: preserva le claim ammesse e forza subject_role a GTW")
    void buildTokenFromInboundJwt_preservesClaimsAndForcesGtw() {
        // Costruisce un token non firmato come se provenisse da un'entità upstream
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", "RSSMRA22A01A399Z");
        payload.put("subject_role", "AAS"); // valore upstream — deve essere sovrascritto
        payload.put("person_id", "BMTBTS01A01I526W");
        payload.put("purpose_of_use", "TREATMENT");
        payload.put("locality", "201123456");
        payload.put("subject_organization", "Regione Marche");
        payload.put("subject_organization_id", "110");
        payload.put("patient_consent", true); // non in JWT_PAYLOAD_CLAIMS → deve essere scartato
        String inbound = Jwts.builder()
                .issuer("upstream-issuer")
                .claims().add(payload).and()
                .compact();

        String repackaged = jwtUtility.buildTokenFromInboundJwt(inbound);
        assertNotNull(repackaged);

        // Ri-pacchettizzato in un token non firmato con solo le claim ammesse;
        // subject_role deve sempre essere forzato a GTW
        Claims parsed = parseUnsigned(repackaged);
        assertEquals("RSSMRA22A01A399Z", parsed.get("sub"));
        assertEquals(Constants.AppConstants.SUBJECT_ROLE_GTW, parsed.get("subject_role"),
                "subject_role deve essere forzato a GTW");
        assertEquals("BMTBTS01A01I526W", parsed.get("person_id"));
        assertEquals("TREATMENT", parsed.get("purpose_of_use"));
        assertEquals("201123456", parsed.get("locality"));
        assertEquals("Regione Marche", parsed.get("subject_organization"));
        assertEquals("110", parsed.get("subject_organization_id"));
        assertFalse(parsed.containsKey("delegation_scope"), "delegation_scope non deve essere presente se assente nel token inbound");
        assertFalse(parsed.containsKey("patient_consent"), "claim fuori da JWT_PAYLOAD_CLAIMS devono essere scartate");
    }

    @Test
    @DisplayName("buildTokenFromInboundJwt: scarta delegation_scope se presente nel token inbound")
    void buildTokenFromInboundJwt_dropsDelegationScope() {
        String payloadJson = "{\"sub\":\"RSSMRA22A01A399Z\",\"delegation_scope\":\"CAREGIVER\"}";
        String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        String encodedHeader = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));
        String inbound = encodedHeader + "." + encodedPayload + ".";

        Claims parsed = parseUnsigned(jwtUtility.buildTokenFromInboundJwt(inbound));
        assertEquals("RSSMRA22A01A399Z", parsed.get("sub"));
        assertFalse(parsed.containsKey("delegation_scope"));
    }

    @Test
    @DisplayName("buildTokenFromInboundJwt: fallback al token minimale se il JWT è malformato")
    void buildTokenFromInboundJwt_malformedFallsBack() {
        Claims parsed = parseUnsigned(jwtUtility.buildTokenFromInboundJwt("not-a-jwt"));
        assertEquals(Constants.AppConstants.SUBJECT_ROLE_GTW, parsed.get("subject_role"));
    }
}
