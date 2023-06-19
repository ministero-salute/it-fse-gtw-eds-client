/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package it.finanze.sanita.fse2.ms.edsclient.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Accepted document types defined by the affinity domain: {@link http://www.hl7italia.it/hl7italia_D7/node/2359}.
 */
@Getter
@AllArgsConstructor
public enum DocumentTypeEnum {

    LAB("2.16.840.1.113883.2.9.10.1.1", "Referto di Laboratorio"),
    RAD("2.16.840.1.113883.2.9.10.1.7.1", "Referto di Radiologia"),
    LDO("2.16.840.1.113883.2.9.10.1.5", "Lettera di Dimissione Ospedaliera"),
    VPS("2.16.840.1.113883.2.9.10.1.6.1", "Verbale di Pronto Soccorso"),
    RSA("2.16.840.1.113883.2.9.10.1.9.1", "Referto di Specialistica Ambulatoriale"),
    PSS("2.16.840.1.113883.2.9.10.1.4.1.1", "Profilo Sanitario Sintetico"),
    CFV("2.16.840.1.113883.2.9.10.1.11.1.2", "Certificato vaccinale"),
    PRS("2.16.840.1.113883.2.9.10.1.2", "Prescrizione"),
    PTO("2.16.840.1.113883.2.9.4.3.14", "Piano Terapeutico"),
    VAC("2.16.840.1.113883.2.9.10.1.11.1.1", "Scheda della singola Vaccinazione");

    private String templateId;

    private String documentType;

    public static DocumentTypeEnum getByTemplateId(String templateId) {
        for (DocumentTypeEnum documentTypeEnum : DocumentTypeEnum.values()) {
            if (documentTypeEnum.getTemplateId().equals(templateId)) {
                return documentTypeEnum;
            }
        }
        return null;
    }

}
