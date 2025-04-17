package it.finanze.sanita.fse2.ms.edsclient.nce.enums;

public enum StoreTypeEnum {
	JKS("JKS"), PKCS12("PKCS12");

    private final String type;

	static public StoreTypeEnum get(String t) {
		StoreTypeEnum out = null;
		for (StoreTypeEnum e:StoreTypeEnum.values()) {
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

	StoreTypeEnum(String inType) {
        this.type = inType;
    }

}