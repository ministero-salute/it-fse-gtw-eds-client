package it.finanze.sanita.fse2.ms.edsclient.isolation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.net.HttpURLConnection;
import javax.net.ssl.HttpsURLConnection;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.util.ReflectionTestUtils;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import it.finanze.sanita.fse2.ms.edsclient.client.RestTemplateResponseErrorHandler;
import it.finanze.sanita.fse2.ms.edsclient.config.OpenTelemetryConfig;
import it.finanze.sanita.fse2.ms.edsclient.config.RestTemplateConfig;

@Tag("ISOLATION")
class ConfigurationIsolationTest {
    @Test
    void createsRestTemplateAndConfiguresHttpAndHttpsWithoutOpeningConnections() throws Exception {
        RestTemplateConfig config = new RestTemplateConfig();
        assertEquals("TLS", config.createSslCustomContext().getProtocol());
        var template = config.restTemplate();
        assertInstanceOf(RestTemplateResponseErrorHandler.class, template.getErrorHandler());
        SimpleClientHttpRequestFactory factory = (SimpleClientHttpRequestFactory) template.getRequestFactory();
        HttpURLConnection plain = mock(HttpURLConnection.class);
        ReflectionTestUtils.invokeMethod(factory, "prepareConnection", plain, "GET");
        verify(plain).setRequestMethod("GET");
        HttpsURLConnection secure = mock(HttpsURLConnection.class);
        ReflectionTestUtils.invokeMethod(factory, "prepareConnection", secure, "POST");
        verify(secure).setSSLSocketFactory(any());
        verify(secure).setHostnameVerifier(any());
        verify(secure).setRequestMethod("POST");
    }

    @SuppressWarnings("unchecked")
    @Test
    void usesNoopTracingWithoutSdkAndConfiguredTracingWhenPresent() {
        ObjectProvider<AutoConfiguredOpenTelemetrySdk> provider = mock(ObjectProvider.class);
        OpenTelemetryConfig config = new OpenTelemetryConfig();
        assertNotNull(config.tracer(provider));
        AutoConfiguredOpenTelemetrySdk sdk = mock(AutoConfiguredOpenTelemetrySdk.class);
        try (OpenTelemetrySdk telemetry = OpenTelemetrySdk.builder().build()) {
            when(provider.getIfAvailable()).thenReturn(sdk);
            when(sdk.getOpenTelemetrySdk()).thenReturn(telemetry);
            assertNotNull(config.tracer(provider));
        }
    }
}
