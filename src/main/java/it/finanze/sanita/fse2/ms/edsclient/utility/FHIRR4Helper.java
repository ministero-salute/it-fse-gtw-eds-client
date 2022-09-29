package it.finanze.sanita.fse2.ms.edsclient.utility;

import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.narrative.DefaultThymeleafNarrativeGenerator;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;

public class FHIRR4Helper {

	private static FhirContext context;

	static {
		context = FhirContext.forR4();
		getContext().setNarrativeGenerator(new DefaultThymeleafNarrativeGenerator());
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T deserializeResource(Class<? extends IBaseResource> resourceClass, String input, Boolean flagJson) {
		IParser parser = null;
		if (flagJson!=null && flagJson) {
			parser = context.newJsonParser();
		} else {
			parser = context.newXmlParser();
		}
		return (T) parser.parseResource(resourceClass, input);
	}

	public static IGenericClient createClient(String serverURL) {
		return context.newRestfulGenericClient(serverURL);
	}

	public static FhirContext getContext() {
		return context;
	}
}