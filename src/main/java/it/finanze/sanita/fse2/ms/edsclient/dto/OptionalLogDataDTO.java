package it.finanze.sanita.fse2.ms.edsclient.dto;

import java.util.Collections;
import java.util.Map;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OptionalLogDataDTO {
    String issuer;
    String role;
    String fiscalCode;
    String documentType;
    String workflowInstanceId;
    String documentId;
    String locality;
    String applicationId;
    String applicationVendor;
    String applicationVersion;
    @Builder.Default
    Map<String, Object> jwtClaims = Collections.emptyMap();
}
