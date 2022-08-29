package it.finanze.sanita.fse2.ms.edsclient.client.impl;

import org.hl7.fhir.r4.model.Bundle;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import it.finanze.sanita.fse2.ms.edsclient.utility.FHIRR4Helper;

public class FHIRClient {

	private IGenericClient client;

	public FHIRClient(String serverURL) {
		client = FHIRR4Helper.createClient(serverURL);
	}

	public Bundle saveBundleWithTransaction(Bundle bundle) {
		return client.transaction().withBundle(bundle).execute();
	}
	
}