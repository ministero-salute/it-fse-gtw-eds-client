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
