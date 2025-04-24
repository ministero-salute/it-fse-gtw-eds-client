package it.finanze.sanita.fse2.ms.edsclient.enums;

import lombok.Getter;

public enum DestinationEnum {

	SEND_TO_UAR("uar"),
	SEND_TO_UDP("udp");
	
	@Getter
	private String restPath;
	
	DestinationEnum(String inRestPath){
		restPath = inRestPath;
	}

	public static DestinationEnum fromString(String str) {
        for (DestinationEnum destination : DestinationEnum.values()) {
            if (destination.name().equalsIgnoreCase(str)) {
                return destination;
            }
        }
        throw new IllegalArgumentException("No enum constant for string: " + str);
    }
}

