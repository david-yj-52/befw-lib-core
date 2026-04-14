package com.tsh.starter.befw.lib.core.data.orm.common.tenant;

import com.tsh.starter.befw.lib.core.data.orm.common.exception.TenantMissingException;

public final class TenantContext {

	private static final ThreadLocal<String> TENANT_HOLDER = new ThreadLocal<>();

	private TenantContext() {
		throw new UnsupportedOperationException("TenantContext is a utility class");
	}

	public static void set(String tenant) {
		TENANT_HOLDER.set(tenant);
	}

	public static String get() {
		String tenant = TENANT_HOLDER.get();
		if (tenant == null || tenant.isBlank()) {
			throw new TenantMissingException();
		}
		return tenant;
	}

	public static void clear() {
		TENANT_HOLDER.remove();
	}

}
