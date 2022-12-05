/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.edsclient.utility;

import java.util.List;

import org.bson.Document;

import it.finanze.sanita.fse2.ms.edsclient.config.Constants;

public class RequestUtility {
	
    private RequestUtility() {}
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
