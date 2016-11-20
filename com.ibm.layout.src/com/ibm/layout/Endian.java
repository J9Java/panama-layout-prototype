package com.ibm.layout;

public enum Endian {
	BIG(">"),
	LITTLE("<"),
	NATIVE("");
	
	String type;
	Endian(String type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		switch (this.name()) {
		case "BIG":
			return "BE";
		case "LITTLE":
			return "LE";
		case "NATIVE":
			return "Native";
		default:
			return "Native";
		}
	}
}
