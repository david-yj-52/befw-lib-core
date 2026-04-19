package com.tsh.starter.befw.lib.core.data.orm.common.tenant;

import org.springframework.stereotype.Component;

@Component
public class ThreadLocalTenantResolver implements TenantResolver {

	@Override
	public void setTenant(String tenant) {
		TenantContext.set(tenant);
	}

	@Override
	public String getTenant() {
		return TenantContext.get();
	}

	@Override
	public void clear() {
		TenantContext.clear();
	}

}
