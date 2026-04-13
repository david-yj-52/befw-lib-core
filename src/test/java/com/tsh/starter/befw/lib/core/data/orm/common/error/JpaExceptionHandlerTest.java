package com.tsh.starter.befw.lib.core.data.orm.common.error;

import com.tsh.starter.befw.lib.core.data.orm.common.exception.TenantMissingException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.orm.jpa.JpaSystemException;

import static org.assertj.core.api.Assertions.assertThat;

class JpaExceptionHandlerTest {

	private JpaExceptionHandler handler;

	@BeforeEach
	void setUp() {
		handler = new JpaExceptionHandler();
	}

	@Test
	void handle_EntityNotFoundException_returns_NOT_FOUND() {
		EntityNotFoundException e = new EntityNotFoundException("not found");
		DataErrorResponse response = handler.handle(e, "TestEntity", "id-1", "findById");

		assertThat(response.getErrorCode()).isEqualTo(DataErrorCode.NOT_FOUND);
		assertThat(response.getEntity()).isEqualTo("TestEntity");
		assertThat(response.getParams()).isEqualTo("id-1");
		assertThat(response.getQuery()).isEqualTo("findById");
		assertThat(response.getOccurredAt()).isNotNull();
	}

	@Test
	void handle_DataIntegrityViolationException_returns_UK_DUPLICATE() {
		DataIntegrityViolationException e = new DataIntegrityViolationException("duplicate key");
		DataErrorResponse response = handler.handle(e, "TestEntity", null, "create");

		assertThat(response.getErrorCode()).isEqualTo(DataErrorCode.UK_DUPLICATE);
	}

	@Test
	void handle_ConstraintViolationException_returns_INVALID_REQUEST() {
		ConstraintViolationException e = new ConstraintViolationException("constraint", null);
		DataErrorResponse response = handler.handle(e, "TestEntity", null, "create");

		assertThat(response.getErrorCode()).isEqualTo(DataErrorCode.INVALID_REQUEST);
	}

	@Test
	void handle_JpaSystemException_returns_DB_UNAVAILABLE() {
		JpaSystemException e = new JpaSystemException(new RuntimeException("db error"));
		DataErrorResponse response = handler.handle(e, "TestEntity", null, "findAll");

		assertThat(response.getErrorCode()).isEqualTo(DataErrorCode.DB_UNAVAILABLE);
	}

	@Test
	void handle_TenantMissingException_returns_TENANT_MISSING() {
		TenantMissingException e = new TenantMissingException();
		DataErrorResponse response = handler.handle(e, "TestEntity", null, "findAll");

		assertThat(response.getErrorCode()).isEqualTo(DataErrorCode.TENANT_MISSING);
	}

	@Test
	void handle_UnknownException_returns_UNKNOWN() {
		RuntimeException e = new RuntimeException("unexpected");
		DataErrorResponse response = handler.handle(e, "TestEntity", null, "findAll");

		assertThat(response.getErrorCode()).isEqualTo(DataErrorCode.UNKNOWN);
	}

	@Test
	void handle_OptimisticLockingFailureException_returns_LOCK_CONFLICT_after_retry() {
		OptimisticLockingFailureException e = new OptimisticLockingFailureException("lock conflict");
		long start = System.currentTimeMillis();

		DataErrorResponse response = handler.handle(e, "TestEntity", null, "update");

		long elapsed = System.currentTimeMillis() - start;
		assertThat(response.getErrorCode()).isEqualTo(DataErrorCode.LOCK_CONFLICT);
		// 100ms 이내에 처리되어야 함
		assertThat(elapsed).isLessThan(200L);
	}

	@Test
	void handle_response_contains_all_fields() {
		EntityNotFoundException e = new EntityNotFoundException("not found");
		DataErrorResponse response = handler.handle(e, "MyEntity", "param-1", "findById");

		assertThat(response.getErrorCode()).isEqualTo(DataErrorCode.NOT_FOUND);
		assertThat(response.getMessage()).isNotNull();
		assertThat(response.getEntity()).isEqualTo("MyEntity");
		assertThat(response.getParams()).isEqualTo("param-1");
		assertThat(response.getQuery()).isEqualTo("findById");
		assertThat(response.getOccurredAt()).isNotNull();
	}

}
