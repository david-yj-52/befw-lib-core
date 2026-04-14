package com.tsh.starter.befw.lib.core.data.orm.common.error;

import com.tsh.starter.befw.lib.core.data.orm.common.exception.TenantMissingException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.OptimisticLockingFailureException;

@Slf4j
@Component
public class JpaExceptionHandler {

	private static final int MAX_RETRY = 2;
	private static final long MAX_TOTAL_MS = 100L;

	public DataErrorResponse handle(Exception e, String entityName, Object params, String query) {
		if (e instanceof OptimisticLockingFailureException) {
			return handleWithRetry((OptimisticLockingFailureException) e, entityName, params, query);
		}

		DataErrorCode code = resolveCode(e);
		log.error("[JPA ERROR] entityName={} | errorCode={} | params={} | query={} | message={}",
			entityName, code, params, query, e.getMessage());
		return DataErrorResponse.of(code, e.getMessage(), entityName, params, query);
	}

	private DataErrorResponse handleWithRetry(
		OptimisticLockingFailureException originalException,
		String entityName, Object params, String query
	) {
		long startTime = System.currentTimeMillis();
		Exception lastException = originalException;

		for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
			long elapsed = System.currentTimeMillis() - startTime;
			if (elapsed >= MAX_TOTAL_MS) {
				break;
			}

			long remaining = MAX_TOTAL_MS - elapsed;
			long sleepMs = remaining / (MAX_RETRY - attempt + 1);

			if (sleepMs > 0) {
				try {
					Thread.sleep(sleepMs);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					break;
				}
			}

			// 재시도 로직은 호출자가 수행하므로 여기서는 재시도 신호만 기록
			log.warn("[JPA RETRY] attempt={} | entityName={} | elapsed={}ms",
				attempt, entityName, System.currentTimeMillis() - startTime);
		}

		log.error("[JPA ERROR] entityName={} | errorCode={} | params={} | query={} | message={}",
			entityName, DataErrorCode.LOCK_CONFLICT, params, query, lastException.getMessage());
		return DataErrorResponse.of(DataErrorCode.LOCK_CONFLICT, lastException.getMessage(), entityName, params, query);
	}

	private DataErrorCode resolveCode(Exception e) {
		if (e instanceof EntityNotFoundException) {
			return DataErrorCode.NOT_FOUND;
		}
		if (e instanceof DataIntegrityViolationException) {
			return DataErrorCode.UK_DUPLICATE;
		}
		if (e instanceof ConstraintViolationException) {
			return DataErrorCode.INVALID_REQUEST;
		}
		if (e instanceof OptimisticLockingFailureException) {
			return DataErrorCode.LOCK_CONFLICT;
		}
		if (e instanceof JpaSystemException) {
			return DataErrorCode.DB_UNAVAILABLE;
		}
		if (e instanceof TenantMissingException) {
			return DataErrorCode.TENANT_MISSING;
		}
		return DataErrorCode.UNKNOWN;
	}

}
