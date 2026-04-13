package com.tsh.starter.befw.lib.core.data.orm.common.tenant;

import com.tsh.starter.befw.lib.core.data.orm.common.exception.TenantMissingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TenantContextTest {

	@AfterEach
	void tearDown() {
		TenantContext.clear();
	}

	@Test
	void set_and_get_tenant() {
		TenantContext.set("tenantA");
		assertThat(TenantContext.get()).isEqualTo("tenantA");
	}

	@Test
	void get_throws_when_tenant_not_set() {
		assertThatThrownBy(TenantContext::get)
			.isInstanceOf(TenantMissingException.class)
			.hasMessageContaining("Tenant is required");
	}

	@Test
	void get_throws_when_tenant_is_blank() {
		TenantContext.set("   ");
		assertThatThrownBy(TenantContext::get)
			.isInstanceOf(TenantMissingException.class);
	}

	@Test
	void clear_removes_tenant() {
		TenantContext.set("tenantA");
		TenantContext.clear();
		assertThatThrownBy(TenantContext::get)
			.isInstanceOf(TenantMissingException.class);
	}

	@Test
	void clear_prevents_tenant_leakage_across_requests() throws InterruptedException {
		TenantContext.set("tenantA");
		TenantContext.clear();

		// 다음 요청 시뮬레이션: 이전 tenant가 남아있지 않아야 함
		assertThatThrownBy(TenantContext::get)
			.isInstanceOf(TenantMissingException.class);
	}

	@Test
	void thread_isolation() throws InterruptedException {
		TenantContext.set("tenantMain");

		Thread[] tenantHolder = new Thread[1];
		String[] threadTenant = new String[1];
		Exception[] threadException = new Exception[1];

		Thread thread = new Thread(() -> {
			try {
				TenantContext.set("tenantThread");
				threadTenant[0] = TenantContext.get();
				TenantContext.clear();
			} catch (Exception e) {
				threadException[0] = e;
			}
		});
		thread.start();
		thread.join();

		assertThat(threadException[0]).isNull();
		assertThat(threadTenant[0]).isEqualTo("tenantThread");
		// 메인 스레드의 tenant는 변하지 않아야 함
		assertThat(TenantContext.get()).isEqualTo("tenantMain");
	}

}
