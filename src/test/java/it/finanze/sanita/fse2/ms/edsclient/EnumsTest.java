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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import it.finanze.sanita.fse2.ms.edsclient.enums.AttivitaClinicaEnum;
import it.finanze.sanita.fse2.ms.edsclient.enums.HealthcareFacilityEnum;
import it.finanze.sanita.fse2.ms.edsclient.enums.PracticeSettingCodeEnum;
import it.finanze.sanita.fse2.ms.edsclient.enums.ProcessorOperationEnum;
import it.finanze.sanita.fse2.ms.edsclient.enums.ResultLogEnum;
import it.finanze.sanita.fse2.ms.edsclient.enums.TipoDocAltoLivEnum;

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
    
}
