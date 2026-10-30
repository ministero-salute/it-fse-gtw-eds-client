package it.finanze.sanita.fse2.ms.edsclient.isolation;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.test.util.ReflectionTestUtils;
import it.finanze.sanita.fse2.ms.edsclient.client.RestTemplateResponseErrorHandler;
import it.finanze.sanita.fse2.ms.edsclient.config.Constants;
import it.finanze.sanita.fse2.ms.edsclient.dto.OptionalLogDataDTO;
import it.finanze.sanita.fse2.ms.edsclient.enums.*;
import it.finanze.sanita.fse2.ms.edsclient.exceptions.*;
import it.finanze.sanita.fse2.ms.edsclient.repository.entity.IniEdsInvocationETY;
import it.finanze.sanita.fse2.ms.edsclient.utility.*;

@Tag("ISOLATION")
class UtilitiesIsolationTest {
    @Test
    void clinicalCodeTablesHaveUniqueNonBlankCodesAndDescriptions() {
        java.util.Set<String> codes = new java.util.HashSet<>();
        for (PracticeSettingCodeEnum value : PracticeSettingCodeEnum.values()) {
            assertEquals(value.name(), value.getCode());
            assertTrue(codes.add(value.getCode()));
            assertFalse(value.getDescription().isBlank());
        }
        codes.clear();
        for (AttivitaClinicaEnum value : AttivitaClinicaEnum.values()) {
            assertTrue(codes.add(value.getCode()));
            assertFalse(value.getCode().isBlank());
            assertFalse(value.getDescription().isBlank());
        }
        codes.clear();
        for (TipoDocAltoLivEnum value : TipoDocAltoLivEnum.values()) {
            assertEquals(value.name(), value.getCode());
            assertTrue(codes.add(value.getCode()));
            assertFalse(value.getDescription().isBlank());
        }
        codes.clear();
        for (HealthcareFacilityEnum value : HealthcareFacilityEnum.values()) {
            assertTrue(codes.add(value.getCode()));
            assertFalse(value.getCode().isBlank());
        }
        for (DocumentTypeEnum value : DocumentTypeEnum.values()) {
            assertSame(value, DocumentTypeEnum.getByCode(value.getCode()));
            assertFalse(value.getDocumentType().isBlank());
        }
        assertNull(DocumentTypeEnum.getByCode("unknown"));
        assertEquals("uar", DestinationEnum.SEND_TO_UAR.getRestPath());
        assertEquals("udp", DestinationEnum.SEND_TO_UDP.getRestPath());
        assertSame(DestinationEnum.SEND_TO_UAR, DestinationEnum.fromString("send_to_uar"));
        assertSame(DestinationEnum.SEND_TO_UDP, DestinationEnum.fromString("SEND_TO_UDP"));
        assertThrows(IllegalArgumentException.class, () -> DestinationEnum.fromString("unknown"));
        assertThrows(IllegalArgumentException.class, () -> DestinationEnum.fromString(null));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> payload(String token) {
        return JsonUtility.jsonToObject(new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]), StandardCharsets.UTF_8), Map.class);
    }

    @Test
    void jwtGenerationRemapsRolePreservesClaimsAndDoesNotMutateInput() {
        JwtUtility jwt = new JwtUtility();
        assertEquals("GTW", payload(jwt.generateToken()).get("subject_role"));
        assertEquals("custom", payload(jwt.generateToken("custom")).get("subject_role"));
        assertEquals("GTW", payload(jwt.generateToken((Map<String, Object>) null)).get("subject_role"));
        assertEquals("GTW", payload(jwt.generateToken(Map.of())).get("subject_role"));
        Map<String, Object> claims = Map.of("iss", "issuer", "subject_role", "doctor");
        Map<String, Object> actual = payload(jwt.generateToken(claims));
        assertEquals("GTW", actual.get("subject_role"));
        assertEquals("issuer", actual.get("iss"));
        assertTrue(actual.containsKey("iat"));
        assertEquals("doctor", claims.get("subject_role"));
    }

    @Test
    void resignsStandardAndRawTokensWhitelistingClaimsAndFallsBackForInvalidInput() {
        JwtUtility jwt = new JwtUtility();
        for (int padding = 0; padding < 4; padding++) {
            String json = "{\"iss\":\"issuer" + "x".repeat(padding) + "\",\"unknown\":\"secret\",\"subject_role\":\"doctor\"}";
            String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes(StandardCharsets.UTF_8));
            for (String token : List.of(raw, "header." + raw + ".signature")) {
                Map<String, Object> actual = payload(jwt.reSignToken(token));
                assertEquals("issuer" + "x".repeat(padding), actual.get("iss"));
                assertEquals("GTW", actual.get("subject_role"));
                assertFalse(actual.containsKey("unknown"));
            }
        }
        for (String token : new String[] {null, "%%%", "bnVsbA", "e30", "a.b.c"}) {
            assertEquals("GTW", payload(jwt.reSignToken(token)).get("subject_role"));
        }
    }

    @Test
    void extractsWhitelistedClaimsAndStructuredLogFieldsFromRealMetadata() {
        String typeCode = "60591-5";
        Document payload = new Document("iss", "issuer").append("subject_role", "doctor")
                .append("locality", "Rome").append("subject_application_id", "app")
                .append("subject_application_vendor", "vendor").append("subject_application_version", "1")
                .append("unlisted", "ignored");
        List<Document> metadata = List.of(new Document(), new Document("tokenEntry", new Document("payload", payload)),
                new Document("documentEntry", new Document("typeCode", typeCode)));
        IniEdsInvocationETY invocation = new IniEdsInvocationETY();
        invocation.setMetadata(metadata);
        invocation.setFiscalCode("fiscal");
        OptionalLogDataDTO actual = RequestUtility.extractOptionalLogData(invocation, "doc", "workflow");
        assertEquals("issuer", actual.getIssuer());
        assertEquals("doctor", actual.getRole());
        assertEquals("Rome", actual.getLocality());
        assertEquals("app", actual.getApplicationId());
        assertEquals("vendor", actual.getApplicationVendor());
        assertEquals("1", actual.getApplicationVersion());
        assertEquals("fiscal", actual.getFiscalCode());
        assertEquals("doc", actual.getDocumentId());
        assertEquals("workflow", actual.getWorkflowInstanceId());
        assertEquals(DocumentTypeEnum.getByCode(typeCode).getDocumentType(), actual.getDocumentType());
        assertFalse(actual.getJwtClaims().containsKey("unlisted"));
        assertEquals("issuer", RequestUtility.extractJwtClaims(metadata).get("iss"));
        assertEquals(typeCode, RequestUtility.extractFieldFromMetadata(metadata, "typeCode"));
        assertEquals("issuer", RequestUtility.extractIssuerFromToken(metadata, "iss"));
        assertEquals("doctor", RequestUtility.extractSubjectRoleFromToken(metadata));
    }

    @Test
    void metadataDefaultsHandleAbsentEmptyAndUnknownFields() {
        OptionalLogDataDTO defaults = RequestUtility.extractOptionalLogData(null, "doc", "workflow");
        assertEquals(Constants.AppConstants.UNKNOWN_ISSUER, defaults.getIssuer());
        assertEquals(Constants.AppConstants.JWT_MISSING_SUBJECT_ROLE, defaults.getRole());
        assertEquals(Constants.AppConstants.UNKNOWN_DOCUMENT_TYPE, defaults.getDocumentType());
        assertNull(defaults.getFiscalCode());
        assertTrue(RequestUtility.extractJwtClaims(null).isEmpty());
        for (List<Document> metadata : List.of(List.<Document>of(), List.of(new Document()),
                List.of(new Document("tokenEntry", new Document())),
                List.of(new Document("tokenEntry", new Document("payload", new Document("iss", " ").append("subject_role", ""))),
                        new Document("documentEntry", new Document("typeCode", "unknown"))))) {
            IniEdsInvocationETY invocation = new IniEdsInvocationETY();
            invocation.setMetadata(metadata);
            assertEquals(Constants.AppConstants.UNKNOWN_DOCUMENT_TYPE,
                    RequestUtility.extractOptionalLogData(invocation, "doc", "workflow").getDocumentType());
            RequestUtility.extractJwtClaims(metadata);
            RequestUtility.extractIssuerFromToken(metadata, "iss");
            RequestUtility.extractSubjectRoleFromToken(metadata);
            RequestUtility.extractFieldFromMetadata(metadata, "typeCode");
        }
        assertEquals(Constants.AppConstants.UNKNOWN_ISSUER, RequestUtility.extractIssuerFromToken(List.of(), "iss"));
        assertEquals(Constants.AppConstants.JWT_MISSING_SUBJECT_ROLE, RequestUtility.extractSubjectRoleFromToken(List.of()));
    }

    @Test
    void jsonConversionAndProfilesRespectSuccessAndFailureCases() {
        assertEquals("{\"key\":\"value\"}", JsonUtility.objectToJson(Map.of("key", "value")));
        assertEquals(Map.of("key", "value"), JsonUtility.jsonToObject("{\"key\":\"value\"}", Map.class));
        assertNull(JsonUtility.jsonToObject("broken", Map.class));
        assertEquals("", JsonUtility.objectToJson(new Object()));
        ProfileUtility profiles = new ProfileUtility();
        assertFalse(profiles.isTestProfile());
        assertFalse(profiles.isDevOrDockerProfile());
        MockEnvironment environment = new MockEnvironment();
        ReflectionTestUtils.setField(profiles, "environment", environment);
        assertFalse(profiles.isTestProfile());
        assertFalse(profiles.isDevOrDockerProfile());
        environment.setActiveProfiles("TEST");
        assertTrue(profiles.isTestProfile());
        assertFalse(profiles.isDevOrDockerProfile());
        environment.setActiveProfiles("dev");
        assertFalse(profiles.isTestProfile());
        assertTrue(profiles.isDevOrDockerProfile());
        environment.setActiveProfiles("docker");
        assertTrue(profiles.isDevOrDockerProfile());
        environment.setActiveProfiles("ISOLATION");
        assertFalse(profiles.isTestProfile());
        assertFalse(profiles.isDevOrDockerProfile());
    }

    @ParameterizedTest
    @ValueSource(ints = {200, 302, 399, 400, 404, 499, 500, 599, 600})
    void detectsHttpErrors(int status) throws Exception {
        assertEquals(status >= 400 && status < 600, new RestTemplateResponseErrorHandler()
                .hasError(new MockClientHttpResponse(new byte[0], status)));
    }

    @Test
    void mapsHttpErrorPayloadsWithDefaultsAndNullBody() throws Exception {
        RestTemplateResponseErrorHandler handler = new RestTemplateResponseErrorHandler();
        for (String body : List.of("{\"detail\":\"detail\"}", "{}")) {
            assertThrows(DocumentNotFoundException.class, () -> handler.handleError(new MockClientHttpResponse(body.getBytes(StandardCharsets.UTF_8), 404)));
            assertThrows(InternalServerException.class, () -> handler.handleError(new MockClientHttpResponse(body.getBytes(StandardCharsets.UTF_8), 500)));
            assertThrows(BusinessException.class, () -> handler.handleError(new MockClientHttpResponse(body.getBytes(StandardCharsets.UTF_8), 400)));
        }
        for (int status : new int[] {404, 500, 400}) {
            handler.handleError(new MockClientHttpResponse("null".getBytes(StandardCharsets.UTF_8), status));
        }
    }
}
