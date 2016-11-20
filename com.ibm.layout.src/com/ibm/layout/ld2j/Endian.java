package com.ibm.layout.ld2j;

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
			return ">";
		case "LITTLE":
			return "<";
		case "NATIVE":
			return "";
		default:
			return "";
		}
	}
}
