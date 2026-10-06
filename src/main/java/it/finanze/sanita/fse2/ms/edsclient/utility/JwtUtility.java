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
 * Utility per la generazione di token JWT non firmati ({@code alg: none}) usati
 * come contenitore di claim per l'autenticazione verso il broker.
 *
 * <p>Espone due flussi pubblici distinti, che condividono la stessa logica
 * interna tramite il metodo privato {@link #buildToken(Map)}:</p>
 * <ul>
 *   <li>{@link #buildTokenFromClaims(Map)} — flusso <b>da Mongo</b>: riceve le
 *       claim già estratte dal documento {@code tokenEntry.payload} salvato su
 *       MongoDB e genera un token pulito con {@code subject_role} forzato a
 *       {@code "GTW"}.</li>
 *   <li>{@link #buildTokenFromInboundJwt(String)} — flusso <b>da request</b>:
 *       riceve il token JWT in arrivo dalla chiamata HTTP, ne decodifica il
 *       payload senza verifica firma, filtra le sole claim ammesse e genera un
 *       token pulito con {@code subject_role} forzato a {@code "GTW"}.</li>
 * </ul>
 *
 * <p>Il token prodotto porta solo un timestamp {@code iat} e le claim di payload;
 * non contiene {@code iss} né {@code exp}.</p>
 */
@Slf4j
@Component
public class JwtUtility {

    /**
     * Flusso <b>da Mongo</b>: genera un token JWT a partire dalle claim già estratte
     * da {@code tokenEntry.payload} (tramite {@link RequestUtility#extractJwtClaims}).
     *
     * <p>Se {@code claims} è {@code null} o vuoto, produce un token minimale con
     * il solo {@code subject_role=GTW}. In tutti gli altri casi {@code subject_role}
     * viene sempre forzato a {@code "GTW"} indipendentemente dal valore upstream.</p>
     *
     * @param claims le claim da includere nel token (es. da {@code RequestUtility.extractJwtClaims})
     * @return token JWT non firmato come stringa
     */
    public String buildTokenFromClaims(Map<String, Object> claims) {
        if (claims == null || claims.isEmpty()) {
            return buildToken(new LinkedHashMap<>());
        }
        return buildToken(claims);
    }

    /**
     * Flusso <b>da request</b>: ri-pacchettizza un JWT in arrivo in un nuovo token
     * non firmato, preservando solo le claim elencate in
     * {@link Constants.AppConstants#JWT_PAYLOAD_CLAIMS} e forzando
     * {@code subject_role} a {@code "GTW"}.
     *
     * <p>Il payload del token in ingresso viene decodificato <b>senza verifica
     * della firma</b> (funziona sia con token firmati sia non firmati). In caso di
     * errore di parsing, il fallback produce un token minimale con il solo
     * {@code subject_role=GTW}.</p>
     *
     * @param inboundJwt il valore {@code Agid-JWT-Signature} in ingresso
     * @return token JWT non firmato come stringa, o token di fallback in caso di errore
     */
    @SuppressWarnings("unchecked")
    public String buildTokenFromInboundJwt(String inboundJwt) {
        try {
            String[] parts = inboundJwt.split("\\.");
            if (parts.length < 2) {
                log.warn("JWT in ingresso malformato (attesi almeno 2 segmenti); fallback al token minimale");
                return buildToken(new LinkedHashMap<>());
            }
            String payloadJson = new String(
                Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            Map<String, Object> parsed = JsonUtility.jsonToObject(payloadJson, Map.class);
            if (parsed == null) {
                log.warn("Payload del JWT in ingresso non parsabile; fallback al token minimale");
                return buildToken(new LinkedHashMap<>());
            }
            Map<String, Object> filtered = new LinkedHashMap<>();
            for (String name : Constants.AppConstants.JWT_PAYLOAD_CLAIMS) {
                if (parsed.containsKey(name)) {
                    filtered.put(name, parsed.get(name));
                }
            }
            return buildToken(filtered);
        } catch (Exception ex) {
            log.warn("Impossibile ri-pacchettizzare il JWT in ingresso; fallback al token minimale: {}", ex.getMessage());
            return buildToken(new LinkedHashMap<>());
        }
    }

    /**
     * Metodo privato condiviso dai due flussi pubblici.
     *
     * <p>Costruisce un token JWT non firmato ({@code alg: none}) con le claim
     * fornite, forzando sempre {@code subject_role} a {@code "GTW"} e aggiungendo
     * il timestamp {@code iat}. Se {@code claims} è vuoto, produce un token con il
     * solo {@code subject_role}.</p>
     *
     * @param claims le claim già filtrate e validate dal chiamante
     * @return token JWT non firmato come stringa
     */
    private String buildToken(Map<String, Object> claims) {
        Map<String, Object> payload = new LinkedHashMap<>(claims);
        payload.put("subject_role", Constants.AppConstants.SUBJECT_ROLE_GTW);

        String token = Jwts.builder()
                .issuedAt(new Date())
                .claims().add(payload).and()
                .compact();

        log.debug("Token JWT generato con {} claim (subject_role forzato a GTW)", payload.size());
        return token;
    }
}
