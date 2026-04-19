package com.tsh.starter.befw.lib.core.messaging.solace;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.tsh.starter.befw.lib.core.data.constant.MessagingSolutionType;
import com.tsh.starter.befw.lib.core.data.orm.gnMsgSrvConn.GnMsgSrvConnAccess;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SolaceSessionManager {

	public static final MessagingSolutionType solType = MessagingSolutionType.Solace;

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

	private ConcurrentHashMap<String, SolaceSessionHandler> sessionMap;

	@PostConstruct
	public void init() {

		log.info("groupId: {}, service:{}, version:{}", applicationModuleName, applicationServiceName,
			applicationVersion);
		sessionMap = new ConcurrentHashMap<>();
	}

}
