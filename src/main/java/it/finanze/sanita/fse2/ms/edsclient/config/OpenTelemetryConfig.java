package it.finanze.sanita.fse2.ms.edsclient.config;

import static it.finanze.sanita.fse2.ms.edsclient.config.Constants.Properties.MS_NAME;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.ObjectProvider;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;

@Configuration
public class OpenTelemetryConfig {

	@Bean
	public Tracer tracer(ObjectProvider<AutoConfiguredOpenTelemetrySdk> sdkProvider) {
		AutoConfiguredOpenTelemetrySdk configuredSdk = sdkProvider.getIfAvailable();
		OpenTelemetry openTelemetry = configuredSdk == null
				? OpenTelemetry.noop()
				: configuredSdk.getOpenTelemetrySdk();
		return openTelemetry.getTracer(MS_NAME);
	}
}
