package it.finanze.sanita.fse2.ms.edsclient.utility;

import it.finanze.sanita.fse2.ms.edsclient.config.Constants;
import org.bson.Document;

import java.util.List;

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

    public static String extractFieldFromToken(final List<Document> metadata, final String fieldName) {
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
}
