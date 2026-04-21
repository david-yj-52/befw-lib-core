package com.tsh.starter.befw.lib.core.messaging;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.solacesystems.jcsmp.JCSMPSession;
import com.tsh.starter.befw.lib.core.config.ApplicationProperties;
import com.tsh.starter.befw.lib.core.data.constant.MessagingSolutionType;
import com.tsh.starter.befw.lib.core.data.orm.gnMsgSrvConn.GnMsgSrvConnAccess;
import com.tsh.starter.befw.lib.core.data.orm.gnMsgSrvConn.GnMsgSrvConnModel;
import com.tsh.starter.befw.lib.core.messaging.kafka.KafkaSessionManager;
import com.tsh.starter.befw.lib.core.messaging.solace.config.SolaceSessionHandler;
import com.tsh.starter.befw.lib.core.messaging.solace.config.SolaceSessionManager;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MessagingConfManager {

	public static final String DEFAULT_KEY = "DEFAULT";

	@Autowired
	GnMsgSrvConnAccess gnMsgSrvConnAccess;

	@Getter
	SolaceSessionManager solaceSessionManager;

	@Getter
	KafkaSessionManager kafkaSessionManager;

	@PostConstruct
	public void init() {

		log.info("groupId: {}, service:{}, version:{}", ApplicationProperties.getApplicationModuleName(),
			ApplicationProperties.getApplicationServiceName(), ApplicationProperties.getApplicationVersion());

		List<GnMsgSrvConnModel> msgServerInfos = this.fetchMsgServerList();

		if (msgServerInfos == null || msgServerInfos.isEmpty()) {
			throw new NullPointerException("Not found messaging server info");
		}

		this.setKafkaManage(msgServerInfos);
		this.setSolaceManage(msgServerInfos);

	}

	public JCSMPSession getSolaceDefaultSession() {
		if (solaceSessionManager == null) {
			throw new NullPointerException("Solace Manager not initialized.");
		}
		;
		return solaceSessionManager.getSession(DEFAULT_KEY);
	}

	public SolaceSessionHandler getSolaceDefaultHandler() {
		if (solaceSessionManager == null) {
			throw new NullPointerException("Solace Manager not initialized.");
		}
		;
		SolaceSessionHandler handler = this.solaceSessionManager.getHandler(DEFAULT_KEY);
		return handler;
	}

	private List<GnMsgSrvConnModel> fetchMsgServerList() {

		List<GnMsgSrvConnModel> infos = this.gnMsgSrvConnAccess.findByTenantAndEnv(
			ApplicationProperties.getApplicationTenant(), ApplicationProperties.getApplicationEnv());

		return infos;

	}

	private void setSolaceManage(List<GnMsgSrvConnModel> infos) {

		List<GnMsgSrvConnModel> solaceList = infos.stream()
			.filter(m -> m.getSolNm() == MessagingSolutionType.Solace)
			.toList();

		this.solaceSessionManager = new SolaceSessionManager(solaceList);
	}

	private void setKafkaManage(List<GnMsgSrvConnModel> infos) {
		List<GnMsgSrvConnModel> kafkaList = infos.stream()
			.filter(m -> m.getSolNm() == MessagingSolutionType.Kafka)
			.toList();
		this.kafkaSessionManager = new KafkaSessionManager(kafkaList);
	}
}
