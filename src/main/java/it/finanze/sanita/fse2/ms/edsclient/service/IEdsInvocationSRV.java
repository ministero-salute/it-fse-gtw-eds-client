package it.finanze.sanita.fse2.ms.edsclient.service;

public interface IEdsInvocationSRV {

	Boolean findAndSendToEdsByWorkflowInstanceId(String workflowInstanceId);
}
