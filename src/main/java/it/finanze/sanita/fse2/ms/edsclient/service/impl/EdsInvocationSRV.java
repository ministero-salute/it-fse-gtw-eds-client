package it.finanze.sanita.fse2.ms.edsclient.service.impl;

import org.hl7.fhir.r4.model.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.edsclient.client.IEdsClient;
import it.finanze.sanita.fse2.ms.edsclient.client.impl.FHIRClient;
import it.finanze.sanita.fse2.ms.edsclient.config.EdsCFG;
import it.finanze.sanita.fse2.ms.edsclient.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.edsclient.repository.IEdsInvocationRepo;
import it.finanze.sanita.fse2.ms.edsclient.repository.entity.IniEdsInvocationETY;
import it.finanze.sanita.fse2.ms.edsclient.service.IEdsInvocationSRV;
import it.finanze.sanita.fse2.ms.edsclient.utility.FHIRR4Helper;
import it.finanze.sanita.fse2.ms.edsclient.utility.ProfileUtility;
import it.finanze.sanita.fse2.ms.edsclient.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EdsInvocationSRV implements IEdsInvocationSRV {

	@Autowired
	private IEdsInvocationRepo edsInvocationRepo;
	
	@Autowired
	private IEdsClient edsClient;

	@Autowired
	private ProfileUtility profileUtility;
	
	@Autowired
	private EdsCFG edsCFG;
	
	@Override
	public Boolean findAndSendToEdsByWorkflowInstanceId(final String workflowInstanceId) {
		boolean out = false;
		try {
			IniEdsInvocationETY iniEdsInvocationETY = edsInvocationRepo.findByWorkflowInstanceId(workflowInstanceId);
			if (iniEdsInvocationETY != null && iniEdsInvocationETY.getData() != null) {
				if (profileUtility.isDevProfile()) {
					String json = StringUtility.toJSON(iniEdsInvocationETY.getData());
					
					Bundle bundle = FHIRR4Helper.deserializeResource(Bundle.class, json, true);
					FHIRClient client = new FHIRClient(edsCFG.getFhirServerTestUrl());
					client.saveBundleWithTransaction(bundle);
					out = true;
					log.info("FHIR bundle: {}", json);
				} else {
					out = edsClient.sendData(iniEdsInvocationETY);
				}
				
			}
		} catch(Exception ex) {
			log.error("Error while running find and send to eds by workflow instance id : " , ex);
			throw new BusinessException(ex);
		}
		return out;
	}
}
