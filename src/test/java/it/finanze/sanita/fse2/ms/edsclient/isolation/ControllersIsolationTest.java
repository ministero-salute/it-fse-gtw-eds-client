package it.finanze.sanita.fse2.ms.edsclient.isolation;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import it.finanze.sanita.fse2.ms.edsclient.client.IBrokerClient;
import it.finanze.sanita.fse2.ms.edsclient.controller.impl.*;
import it.finanze.sanita.fse2.ms.edsclient.controller.handler.ControllerExceptionHandler;
import it.finanze.sanita.fse2.ms.edsclient.dto.EdsResponseDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.response.*;
import it.finanze.sanita.fse2.ms.edsclient.exceptions.*;
import it.finanze.sanita.fse2.ms.edsclient.service.IEdsInvocationSRV;

@Tag("ISOLATION")
@ActiveProfiles("ISOLATION")
@SpringJUnitConfig({EDSPublicationCTL.class, LivenessCheckCTL.class, ControllerExceptionHandler.class})
class ControllersIsolationTest {
    @Autowired private EDSPublicationCTL controller;
    @Autowired private LivenessCheckCTL health;
    @Autowired private ControllerExceptionHandler advice;
    @MockitoBean private IEdsInvocationSRV service;
    @MockitoBean private IBrokerClient broker;
    @MockitoBean private Tracer tracer;
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        when(tracer.spanBuilder(anyString())).thenAnswer(call -> OpenTelemetry.noop().getTracer("isolation").spanBuilder(call.getArgument(0)));
        mvc = MockMvcBuilders.standaloneSetup(controller, health).setControllerAdvice(advice).build();
    }

    private EdsResponseDTO success() {
        EdsResponseDTO result = new EdsResponseDTO();
        result.setEsito(true);
        return result;
    }

    @Test
    void publishesAndReplacesThroughMvc() throws Exception {
        when(service.publish("doc", "workflow")).thenReturn(success());
        mvc.perform(post("/v1/documents").contentType(MediaType.APPLICATION_JSON)
                .content("{\"identificativoDoc\":\"doc\",\"workflowInstanceId\":\"workflow\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.esito").value(true));
        when(service.replace("doc", "workflow")).thenReturn(success());
        mvc.perform(put("/v1/documents/doc").contentType(MediaType.APPLICATION_JSON)
                .content("{\"idDoc\":\"doc\",\"workflowInstanceId\":\"workflow\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.esito").value(true));
        verify(service).publish("doc", "workflow");
        verify(service).replace("doc", "workflow");
    }

    @Test
    void deletesAndUpdatesForwardingJwt() throws Exception {
        when(service.delete("doc", "fiscal", "jwt")).thenReturn(success());
        mvc.perform(delete("/v1/documents/doc/fiscal").header("Agid-JWT-Signature", "jwt"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.esito").value(true));
        when(service.update(eq("doc"), any(), eq("fiscal"), eq("jwt"))).thenReturn(success());
        mvc.perform(put("/v1/documents/doc/metadata").header("Agid-JWT-Signature", "jwt")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idDoc\":\"doc\",\"workflowInstanceId\":\"workflow\",\"documentReference\":\"{}\",\"fiscalCode\":\"fiscal\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.esito").value(true));
        verify(service).delete("doc", "fiscal", "jwt");
        verify(service).update(eq("doc"), argThat(dto -> "workflow".equals(dto.getWorkflowInstanceId())), eq("fiscal"), eq("jwt"));
    }

    @Test
    void readsReferenceStatusAndHealth() throws Exception {
        GetDocumentReferenceResDTO reference = new GetDocumentReferenceResDTO(new LogTraceInfoDTO("span", "trace"), "reference");
        when(service.getDocumentReference("doc", "fiscal", "jwt")).thenReturn(reference);
        mvc.perform(get("/v1/document/fiscal/doc").header("Agid-JWT-Signature", "jwt"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.documentReference").value("reference"));
        when(broker.getIngestionStatus("workflow")).thenReturn(GetIngestionStatusResponseDTO.builder()
                .workflowInstanceId("workflow").status("SUCCESS").build());
        mvc.perform(get("/v1/status/workflow")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("SUCCESS"));
        mvc.perform(get("/status")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void mapsServiceExceptionsToProblemResponses() throws Exception {
        when(service.delete("doc", "fiscal", "jwt")).thenThrow(new DocumentNotFoundException("missing"));
        mvc.perform(delete("/v1/documents/doc/fiscal").header("Agid-JWT-Signature", "jwt"))
                .andExpect(status().isNotFound()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        doThrow(new BusinessException("failed")).when(service).delete("doc", "fiscal", "jwt");
        mvc.perform(delete("/v1/documents/doc/fiscal").header("Agid-JWT-Signature", "jwt"))
                .andExpect(status().isInternalServerError()).andExpect(jsonPath("$.detail").value("failed"));
        doThrow(new InternalServerException("offline")).when(service).delete("doc", "fiscal", "jwt");
        mvc.perform(delete("/v1/documents/doc/fiscal").header("Agid-JWT-Signature", "jwt"))
                .andExpect(status().isInternalServerError()).andExpect(jsonPath("$.detail").value("offline"));
    }

    @Test
    void rejectsMissingHeadersAndMalformedJsonBeforeCallingService() throws Exception {
        mvc.perform(delete("/v1/documents/doc/fiscal")).andExpect(status().isBadRequest());
        mvc.perform(post("/v1/documents").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}
