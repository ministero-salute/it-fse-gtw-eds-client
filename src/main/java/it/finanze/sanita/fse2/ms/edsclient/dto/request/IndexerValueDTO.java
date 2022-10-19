/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.edsclient.dto.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IndexerValueDTO {

    /**
     * Workflow instance id.
     */
    private String workflowInstanceId;

    /**
     * Identifier of document to be updated. Is not {@code null} only if the notification is sent for update.
     */
    private String idDoc;
}
