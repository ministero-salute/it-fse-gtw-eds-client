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
package it.finanze.sanita.fse2.ms.edsclient;

import it.finanze.sanita.fse2.ms.edsclient.config.Constants;
import it.finanze.sanita.fse2.ms.edsclient.enums.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = {Constants.ComponentScan.BASE})
@ActiveProfiles(Constants.Profile.TEST)
class EnumsTest {

    @Test
    @DisplayName("ProcessOperationEnum test")
    void processorOperationEnumTest() {
        String name = ProcessorOperationEnum.PUBLISH.getName();
        Assertions.assertEquals(name, ProcessorOperationEnum.PUBLISH.getName());
    }

    @Test
    @DisplayName("AttivitaClinicaEnum test")
    void attivitaClinicaEnumTest() {
        String code = "CON";
        String description = "Consulto";
        Assertions.assertEquals(code, AttivitaClinicaEnum.CON.getCode());
        Assertions.assertEquals(description, AttivitaClinicaEnum.CON.getDescription());
    }

    @Test
    @DisplayName("AttivitaClinicaEnum test")
    void practiceSettingCodeEnumTest() {
        String description = "Allergologia";
        Assertions.assertEquals(description, PracticeSettingCodeEnum.AD_PSC001.getDescription());
    }

    @Test
    @DisplayName("tipoDocLivAltoEnumTest test")
    void tipoDocLivAltoEnumTest() {
        String code = TipoDocAltoLivEnum.CER.getCode();
        String description = TipoDocAltoLivEnum.CER.getDescription();
        Assertions.assertEquals(code, TipoDocAltoLivEnum.CER.getCode());
        Assertions.assertEquals(description, TipoDocAltoLivEnum.CER.getDescription());
    }

    @Test
    @DisplayName("healthcareFacilityEnumTest test")
    void healthcareFacilityEnumTest() {
        String code = HealthcareFacilityEnum.Cittadino.getCode();
        Assertions.assertEquals(code, HealthcareFacilityEnum.Cittadino.getCode());
    }

    @Test
    @DisplayName("testResultLogEnum")
    void testResultLogEnum() {
        String code = ResultLogEnum.KO.getCode();
        assertEquals(code, ResultLogEnum.KO.getCode());
    }
    
    @Test
    @DisplayName("testDocumentTypeEnum")
    void testDocumentTypeEnum() {
        String docTypeCFV = DocumentTypeEnum.CFV.getDocumentType();
        String tempCFV = DocumentTypeEnum.CFV.getTemplateId();
        Assertions.assertEquals("2.16.840.1.113883.2.9.10.1.11.1.2", tempCFV);
        Assertions.assertEquals("Certificato vaccinale", docTypeCFV);

        String docTypeLAB = DocumentTypeEnum.LAB.getDocumentType();
        String tempLAB = DocumentTypeEnum.LAB.getTemplateId(); 
        Assertions.assertEquals("2.16.840.1.113883.2.9.10.1.1", tempLAB);
        Assertions.assertEquals("Referto di Laboratorio", docTypeLAB);

        String docTypeRAD = DocumentTypeEnum.RAD.getDocumentType();
        String tempRAD = DocumentTypeEnum.RAD.getTemplateId(); 
        Assertions.assertEquals("2.16.840.1.113883.2.9.10.1.7.1", tempRAD);
        Assertions.assertEquals("Referto di Radiologia", docTypeRAD);

        String docTypeLDO = DocumentTypeEnum.LDO.getDocumentType();
        String tempLDO = DocumentTypeEnum.LDO.getTemplateId();  
        Assertions.assertEquals("2.16.840.1.113883.2.9.10.1.5", tempLDO);
        Assertions.assertEquals("Lettera di Dimissione Ospedaliera", docTypeLDO);

        String docTypeVPS = DocumentTypeEnum.VPS.getDocumentType();
        String tempVPS = DocumentTypeEnum.VPS.getTemplateId();
        Assertions.assertEquals("2.16.840.1.113883.2.9.10.1.6.1", tempVPS);
        Assertions.assertEquals("Verbale di Pronto Soccorso", docTypeVPS);
       
        String docTypeRSA = DocumentTypeEnum.RSA.getDocumentType();
        String tempRSA = DocumentTypeEnum.RSA.getTemplateId();
        Assertions.assertEquals("2.16.840.1.113883.2.9.10.1.9.1", tempRSA);
        Assertions.assertEquals("Referto di Specialistica Ambulatoriale", docTypeRSA);

        String docTypePSS = DocumentTypeEnum.PSS.getDocumentType();
        String tempPSS = DocumentTypeEnum.PSS.getTemplateId();
        Assertions.assertEquals("2.16.840.1.113883.2.9.10.1.4.1.1", tempPSS);
        Assertions.assertEquals("Profilo Sanitario Sintetico", docTypePSS);

        String docTypePRS = DocumentTypeEnum.PRS.getDocumentType();
        String tempPRS = DocumentTypeEnum.PRS.getTemplateId();
        Assertions.assertEquals("2.16.840.1.113883.2.9.10.1.2", tempPRS);
        Assertions.assertEquals("Prescrizione", docTypePRS);

        String docTypePTO = DocumentTypeEnum.PTO.getDocumentType();
        String tempPTO = DocumentTypeEnum.PTO.getTemplateId();
        Assertions.assertEquals("2.16.840.1.113883.2.9.4.3.14", tempPTO);
        Assertions.assertEquals("Piano Terapeutico", docTypePTO);

        String docTypeVAC = DocumentTypeEnum.VAC.getDocumentType();
        String tempVAC = DocumentTypeEnum.VAC.getTemplateId();
        Assertions.assertEquals("2.16.840.1.113883.2.9.10.1.11.1.1", tempVAC);
        Assertions.assertEquals("Scheda della singola Vaccinazione", docTypeVAC);
    }
}
