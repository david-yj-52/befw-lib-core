package com.tsh.starter.befw.lib.core.data.orm.common.exception;

public class TenantMissingException extends RuntimeException {

	public TenantMissingException(String message) {
		super(message);
	}

	public TenantMissingException() {
		super("Tenant is required but not set. Please set tenant before any DB access.");
	}

}
