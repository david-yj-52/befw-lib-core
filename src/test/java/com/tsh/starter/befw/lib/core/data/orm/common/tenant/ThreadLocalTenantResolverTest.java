package com.tsh.starter.befw.lib.core.data.orm.common.tenant;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.tsh.starter.befw.lib.core.data.orm.common.exception.TenantMissingException;

class ThreadLocalTenantResolverTest {

	private final TenantResolver resolver = new ThreadLocalTenantResolver();

	@AfterEach
	void tearDown() {
		resolver.clear();
	}

	@Test
	void setTenant_and_getTenant() {
		resolver.setTenant("tenantB");
		assertThat(resolver.getTenant()).isEqualTo("tenantB");
	}

	@Test
	void clear_removes_tenant() {
		resolver.setTenant("tenantB");
		resolver.clear();
		assertThatThrownBy(resolver::getTenant)
			.isInstanceOf(TenantMissingException.class);
	}

}
