package com.tsh.starter.befw.lib.core.messaging;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Connection;
import com.solacesystems.jcsmp.JCSMPSession;
import com.tsh.starter.befw.lib.core.config.ApplicationProperties;
import com.tsh.starter.befw.lib.core.config.MessagingProperties;
import com.tsh.starter.befw.lib.core.data.constant.MessagingSolutionType;
import com.tsh.starter.befw.lib.core.data.orm.msgServiceConn.gsMsgSrvConn.GsMsgSrvConnAccess;
import com.tsh.starter.befw.lib.core.data.orm.msgServiceConn.gsMsgSrvConn.GsMsgSrvConnModel;
import com.tsh.starter.befw.lib.core.messaging.kafka.KafkaSessionManager;
import com.tsh.starter.befw.lib.core.messaging.rabbitmq.config.RabbitMqSessionHandler;
import com.tsh.starter.befw.lib.core.messaging.rabbitmq.config.RabbitMqSessionManager;
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
	GsMsgSrvConnAccess gsMsgSrvConnAccess;

	@Getter
	SolaceSessionManager solaceSessionManager;

	@Getter
	KafkaSessionManager kafkaSessionManager;

	@Getter
	RabbitMqSessionManager rabbitMqSessionManager;

	@Autowired
	MessagingProperties messagingProperties;

	@PostConstruct
	public void init() {

		log.info("groupId: {}, service:{}, version:{}", ApplicationProperties.getApplicationModuleName(),
			ApplicationProperties.getApplicationServiceName(), ApplicationProperties.getApplicationVersion());

		boolean solaceEnabled = Boolean.parseBoolean(messagingProperties.getSolaceEnable());
		boolean kafkaEnabled = Boolean.parseBoolean(messagingProperties.getKafkaEnable());
		boolean rabbitMqEnabled = Boolean.parseBoolean(messagingProperties.getRabbitMqEnable());

		// NOTE 기존에는 solaceEnable 플래그 하나로 kafka까지 함께 초기화되던 구조였습니다.
		// rabbitmq를 solace 없이 단독으로 사용할 수 있도록 활성화된 솔루션이 하나라도 있으면 조회하도록 수정했습니다.
		if (solaceEnabled || kafkaEnabled || rabbitMqEnabled) {
			List<GsMsgSrvConnModel> msgServerInfos = this.fetchMsgServerList();

			if (msgServerInfos == null || msgServerInfos.isEmpty()) {
				throw new NullPointerException("Not found messaging server info");
			}

			if (kafkaEnabled) {
				this.setKafkaManage(msgServerInfos);
			}
			if (solaceEnabled) {
				this.setSolaceManage(msgServerInfos);
			}
			if (rabbitMqEnabled) {
				this.setRabbitMqManage(msgServerInfos);
			}
		}

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

	public Connection getRabbitMqDefaultConnection() {
		if (rabbitMqSessionManager == null) {
			throw new NullPointerException("RabbitMq Manager not initialized.");
		}
		return rabbitMqSessionManager.getConnection(DEFAULT_KEY);
	}

	public RabbitMqSessionHandler getRabbitMqDefaultHandler() {
		if (rabbitMqSessionManager == null) {
			throw new NullPointerException("RabbitMq Manager not initialized.");
		}
		return this.rabbitMqSessionManager.getHandler(DEFAULT_KEY);
	}

	private List<GsMsgSrvConnModel> fetchMsgServerList() {

		List<GsMsgSrvConnModel> infos = this.gsMsgSrvConnAccess.findByTenantAndEnv(
			ApplicationProperties.getApplicationTenant(), ApplicationProperties.getApplicationEnv());

		return infos;

	}

	private void setSolaceManage(List<GsMsgSrvConnModel> infos) {

		List<GsMsgSrvConnModel> solaceList = infos.stream()
			.filter(m -> m.getSolNm() == MessagingSolutionType.Solace)
			.toList();

		this.solaceSessionManager = new SolaceSessionManager(solaceList);
	}

	private void setKafkaManage(List<GsMsgSrvConnModel> infos) {
		List<GsMsgSrvConnModel> kafkaList = infos.stream()
			.filter(m -> m.getSolNm() == MessagingSolutionType.Kafka)
			.toList();
		this.kafkaSessionManager = new KafkaSessionManager(kafkaList);
	}

	private void setRabbitMqManage(List<GsMsgSrvConnModel> infos) {

		List<GsMsgSrvConnModel> rabbitMqList = infos.stream()
			.filter(m -> m.getSolNm() == MessagingSolutionType.RabbitMq)
			.toList();

		this.rabbitMqSessionManager = new RabbitMqSessionManager(rabbitMqList);
	}
}
