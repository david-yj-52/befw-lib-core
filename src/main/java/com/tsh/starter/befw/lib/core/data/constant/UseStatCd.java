package com.tsh.starter.befw.lib.core.data.constant;

public enum UseStatCd {
	Usable("Possible to use"),
	UnUsable("Wrong data, Need to investigate"),
	Delete("Ready to delete");


	private final String description;

	UseStatCd(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
