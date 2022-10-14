package it.finanze.sanita.fse2.ms.edsclient.enums;

import lombok.Getter;

public enum PriorityTypeEnum {
    LOW("LOW"),
    MEDIUM("MEDIUM"),
    HIGH("HIGH");

    @Getter
    private final String description;

    PriorityTypeEnum(String description) {
        this.description = description;
    }

}
