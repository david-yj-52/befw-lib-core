package com.tsh.starter.befw.lib.core.data.orm.common.error;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DataErrorResponse {

	private DataErrorCode errorCode;
	private String message;
	private String entity;
	private Object params;
	private String query;
	private LocalDateTime occurredAt;

	public static DataErrorResponse of(
		DataErrorCode errorCode,
		String message,
		String entity,
		Object params,
		String query
	) {
		return DataErrorResponse.builder()
			.errorCode(errorCode)
			.message(message)
			.entity(entity)
			.params(params)
			.query(query)
			.occurredAt(LocalDateTime.now())
			.build();
	}

}
