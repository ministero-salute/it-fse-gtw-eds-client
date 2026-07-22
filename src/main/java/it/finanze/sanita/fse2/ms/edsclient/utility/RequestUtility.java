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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import it.finanze.sanita.fse2.ms.edsclient.config.Constants;

public class RequestUtility {

    private RequestUtility() {}

    /**
     * Extracts the JWT claims to forward to the broker from the {@code tokenEntry.payload}
     * object inside the {@code metadata} array of an {@link it.finanze.sanita.fse2.ms.edsclient.repository.entity.IniEdsInvocationETY}.
     *
     * <p>Only the names listed in {@link Constants.AppConstants#JWT_PAYLOAD_CLAIMS} are copied,
     * and only when the payload actually contains them, so absent fields (e.g. {@code delegation_scope})
     * are omitted rather than defaulted.</p>
     *
     * @param metadata the ETY metadata list (may be {@code null})
     * @return an ordered map of the claims present in the payload; empty if metadata/payload is missing
     */
    public static Map<String, Object> extractJwtClaims(final List<Document> metadata) {
        final Map<String, Object> claims = new LinkedHashMap<>();
        if (metadata == null) {
            return claims;
        }
        for (Document meta : metadata) {
            if (meta.get("tokenEntry") != null) {
                final Document token = (Document) meta.get("tokenEntry");
                final Document payload = (Document) token.get("payload");
                if (payload != null) {
                    for (String name : Constants.AppConstants.JWT_PAYLOAD_CLAIMS) {
                        if (payload.containsKey(name)) {
                            claims.put(name, payload.get(name));
                        }
                    }
                }
                break;
            }
        }
        return claims;
    }
    public static String extractFieldFromMetadata(List<Document> metadata, String fieldName) {
        String field = Constants.AppConstants.UNKNOWN_DOCUMENT_TYPE;
        for (Document meta : metadata) {
            if (meta.get("documentEntry") != null) {
                field = ((Document) meta.get("documentEntry")).getString(fieldName);
                break;
            }
        }
        return field;
    }

    public static String extractIssuerFromToken(final List<Document> metadata, final String fieldName) {
        String field = Constants.AppConstants.UNKNOWN_ISSUER;
        for (Document meta : metadata) {
            if (meta.get("tokenEntry") != null) {
                final Document token = (Document) meta.get("tokenEntry");
                if (token.get("payload") != null) {
                    final Document payload = (Document) token.get("payload");
                    if (payload != null) {
                        field = payload.getString(fieldName);
                    }
                }
                break;
            }
        }
        return field;
    }

    public static String extractSubjectRoleFromToken(final List<Document> metadata) {
        String subjectRole = Constants.AppConstants.JWT_MISSING_SUBJECT_ROLE;
        
        for (Document meta : metadata) {
            if (meta.get("tokenEntry") != null) {
                final Document token = (Document) meta.get("tokenEntry");
                if (token.get("payload") != null) {
                    final Document payload = (Document) token.get("payload");
                    if (payload != null) {
                        subjectRole = payload.getString("subject_role");
                    }
                }
                break;
            }
        }
        return subjectRole;
    }
}
