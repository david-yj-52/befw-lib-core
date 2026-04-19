package com.tsh.starter.befw.lib.core.data.orm.common.tenant;

public interface TenantResolver {

	void setTenant(String tenant);

	String getTenant();

	void clear();

}
