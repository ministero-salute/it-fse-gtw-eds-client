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
package it.finanze.sanita.fse2.ms.edsclient.utility;

import io.jsonwebtoken.Jwts;
import it.finanze.sanita.fse2.ms.edsclient.config.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility class for JWT token generation.
 * Generates unsigned ({@code alg: none}) JWT tokens that act as a plain claims container
 * for broker authentication — the receiving microservice reads the claims without needing
 * any shared secret. The token carries only an {@code iat} timestamp plus the payload
 * claims (no {@code iss} / {@code exp}).
 */
@Slf4j
@Component
public class JwtUtility {

    /**
     * Generates a JWT token with subject_role claim set to "GTW".
     *
     * @return JWT token as string
     */
    public String generateToken() {
        String token = Jwts.builder()
                .issuedAt(new Date())
                .claim("subject_role", Constants.AppConstants.SUBJECT_ROLE_GTW)
                .compact();

        log.debug("JWT token generated");
        return token;
    }

    /**
     * Generates a JWT token with custom subject_role.
     *
     * @param subjectRole the subject role to include in the token
     * @return JWT token as string
     */
    public String generateToken(String subjectRole) {
        String token = Jwts.builder()
                .issuedAt(new Date())
                .claim("subject_role", subjectRole)
                .compact();

        log.debug("JWT token generated with subject_role: {}", subjectRole);
        return token;
    }

    /**
     * Generates a JWT token carrying the supplied payload claims.
     *
     * <p>Carries only an {@code iat} timestamp plus every entry of {@code claims} as a data
     * claim. The token is unsigned ({@code alg: none}). If {@code claims} is null or empty,
     * falls back to the simple token.</p>
     *
     * @param claims the payload claims to include (e.g. from {@code RequestUtility.extractJwtClaims})
     * @return JWT token as string
     */
    public String generateToken(Map<String, Object> claims) {
        if (claims == null || claims.isEmpty()) {
            return generateToken();
        }
        String token = Jwts.builder()
                .issuedAt(new Date())
                .claims().add(claims).and()
                .compact();

        log.debug("JWT token generated with {} payload claim(s)", claims.size());
        return token;
    }

    /**
     * Re-packages an inbound JWT into a fresh unsigned JWT, preserving only the claims
     * listed in {@link Constants.AppConstants#JWT_PAYLOAD_CLAIMS}.
     *
     * <p>The inbound token's payload is decoded <b>without signature verification</b> (it works
     * for both signed and unsigned inbound tokens). On any parsing failure, falls back to the
     * simple {@code subject_role=GTW} token so the call still succeeds.</p>
     *
     * @param incomingJwt the inbound {@code Agid-JWT-Signature} value
     * @return a fresh unsigned JWT with the same claim structure, or the fallback token
     */
    @SuppressWarnings("unchecked")
    public String reSignToken(String incomingJwt) {
        try {
            String[] parts = incomingJwt.split("\\.");
            if (parts.length < 2) {
                log.warn("Inbound JWT is malformed (expected at least 2 segments); falling back to simple token");
                return generateToken();
            }
            String payloadJson = new String(
                Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            Map<String, Object> parsed = JsonUtility.jsonToObject(payloadJson, Map.class);
            if (parsed == null) {
                log.warn("Inbound JWT payload could not be parsed; falling back to simple token");
                return generateToken();
            }
            Map<String, Object> claims = new LinkedHashMap<>();
            for (String name : Constants.AppConstants.JWT_PAYLOAD_CLAIMS) {
                if (parsed.containsKey(name)) {
                    claims.put(name, parsed.get(name));
                }
            }
            return generateToken(claims);
        } catch (Exception ex) {
            log.warn("Failed to re-sign inbound JWT; falling back to simple token: {}", ex.getMessage());
            return generateToken();
        }
    }
}
