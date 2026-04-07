package com.tsh.starter.befw.lib.core.messaging.solace;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.tsh.starter.befw.lib.core.data.orm.gnMsgSrvConn.GnMsgSrvConnAccess;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SolaceSessionManager {

	@Autowired
	GnMsgSrvConnAccess gnMsgSrvConnAccess;

	@Value("${application.module-name}")
	private String applicationModuleName;
	@Value("${application.version}")
	private String applicationVersion;
	@Value("${application.service-name}")
	private String applicationServiceName;
	@Value("${application.tenant}")
	private String tenant;

	private ConcurrentHashMap<String, String> sessionMap;

	@PostConstruct
	public void init() {

		log.info("groupId: {}, service:{}, version:{}", applicationModuleName, applicationServiceName,
			applicationVersion);
		System.out.println("groupId: " + applicationModuleName + "serviceName" + applicationServiceName + "  version: "
			+ applicationVersion);
		sessionMap = new ConcurrentHashMap<>();
	}

}
