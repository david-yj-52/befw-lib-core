package com.tsh.starter.befw.lib.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class ApplicationProperties {

	// static holder
	private static String APPLICATION_TENANT;
	private static String APPLICATION_MODULE_NAME;
	private static String APPLICATION_VERSION;
	private static String APPLICATION_SERVICE_NAME;
	private static String APPLICATION_ENV;

	@Value("${application.tenant}")
	private String applicationTenant;

	@Value("${application.module-name}")
	private String applicationModuleName;

	@Value("${application.version}")
	private String applicationVersion;

	@Value("${application.service-name}")
	private String applicationServiceName;

	@Value("${application.env}")
	private String applicationEnv;

	public static String getApplicationTenant() {
		return APPLICATION_TENANT;
	}

	;

	public static String getApplicationModuleName() {
		return APPLICATION_MODULE_NAME;
	}

	;

	public static String getApplicationVersion() {
		return APPLICATION_VERSION;
	}

	;

	public static String getApplicationServiceName() {
		return APPLICATION_SERVICE_NAME;
	}

	;

	public static String getApplicationEnv() {
		return APPLICATION_ENV;
	}

	;

	@PostConstruct
	public void init() {
		APPLICATION_TENANT = this.applicationTenant;
		APPLICATION_MODULE_NAME = this.applicationModuleName;
		APPLICATION_VERSION = this.applicationVersion;
		APPLICATION_SERVICE_NAME = this.applicationServiceName;
		APPLICATION_ENV = this.applicationEnv;

	}
}
