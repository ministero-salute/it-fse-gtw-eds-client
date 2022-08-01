package it.finanze.sanita.fse2.ms.edsclient.utility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.narrative.DefaultThymeleafNarrativeGenerator;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import it.finanze.sanita.fse2.ms.edsclient.exceptions.BusinessException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FHIRR4Helper {

	private static FhirContext context;

	static {
		context = FhirContext.forR4();
		getContext().setNarrativeGenerator(new DefaultThymeleafNarrativeGenerator());
	}

	public static String trasform(Transformer t, byte[] cdaXMLByte) {
		String xml = null;
		InputStream isCDAXML = new ByteArrayInputStream(cdaXMLByte);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			StreamSource ss = new StreamSource(isCDAXML);
			StreamResult sr = new StreamResult(baos);
			t.transform(ss, sr);
			
			byte[] bytes = baos.toByteArray();
			xml = new String(bytes);
		} catch (Exception e) {
			throw new BusinessException(e);
		}
		return xml;
	}
	 
	
	public static String serializeResource(IBaseResource resource, Boolean flagPrettyPrint, Boolean flagSuppressNarratives, Boolean flagSummaryMode) {
		IParser parser = context.newJsonParser();
		parser.setPrettyPrint(flagPrettyPrint);
		parser.setSuppressNarratives(flagSuppressNarratives);
		parser.setSummaryMode(flagSummaryMode);
		return parser.encodeResourceToString(resource);
	}

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