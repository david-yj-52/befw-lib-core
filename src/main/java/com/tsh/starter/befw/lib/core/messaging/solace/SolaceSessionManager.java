package com.tsh.starter.befw.lib.core.messaging.solace;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.tsh.starter.befw.lib.core.data.orm.gnMsgSrvConn.GnMsgSrvConnAccess;
import com.tsh.starter.befw.lib.core.data.orm.gnMsgSrvConn.GnMsgSrvConnModel;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SolaceSessionManager {

	@Value("${application.}")

	@Autowired
	GnMsgSrvConnAccess gnMsgSrvConnAccess;

	private ConcurrentHashMap<String, String> sessionMap;

	@PostConstruct
	public void init() {
		sessionMap = new ConcurrentHashMap<>();
	}


	private GnMsgSrvConnModel

}
