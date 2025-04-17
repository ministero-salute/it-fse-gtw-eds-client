package it.finanze.sanita.fse2.ms.edsclient.nce.enums;

public enum ModeEnum {
	CMS("CMS"), CIPHER("CIPHER");

    private final String type;

	static public ModeEnum get(String t) {
		ModeEnum out = null;
		for (ModeEnum e:ModeEnum.values()) {
			if (e.getType().equalsIgnoreCase(t)) {
				out = e;
				break;
			}
		}
		return out;
	}
	
    public String getType() {
		return type;
	}

	ModeEnum(String inType) {
        this.type = inType;
    }

}