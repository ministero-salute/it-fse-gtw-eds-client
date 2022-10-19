/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.edsclient.repository;

import java.io.Serializable;

import it.finanze.sanita.fse2.ms.edsclient.repository.entity.IniEdsInvocationETY;

public interface IEdsInvocationRepo extends Serializable {

	IniEdsInvocationETY findByWorkflowInstanceId(String workflowInstanceId);
}
