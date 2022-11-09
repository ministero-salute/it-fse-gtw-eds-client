/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
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
}
