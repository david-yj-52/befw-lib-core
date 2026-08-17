package com.tsh.starter.befw.lib.core.exception;


import com.tsh.starter.befw.lib.core.lifecycle.RequestLifeCycleVo;

public class ApplicationException extends RuntimeException {

	private final RequestLifeCycleVo requestLifeCycleVo;

	public ApplicationException(String message, RequestLifeCycleVo requestLifeCycleVo) {
		super(message);
		this.requestLifeCycleVo = requestLifeCycleVo;
	}
}
