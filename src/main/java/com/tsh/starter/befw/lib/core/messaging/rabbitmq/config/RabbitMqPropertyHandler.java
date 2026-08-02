package com.tsh.starter.befw.lib.core.messaging.rabbitmq.config;

import com.rabbitmq.client.ConnectionFactory;
import com.tsh.starter.befw.lib.core.apService.util.DateTimeUtil;
import com.tsh.starter.befw.lib.core.apService.util.ServerNameUtil;
import com.tsh.starter.befw.lib.core.config.ApplicationProperties;
import com.tsh.starter.befw.lib.core.data.orm.msgServiceConn.gsMsgSrvConn.GsMsgSrvConnModel;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RabbitMqPropertyHandler {

	public static final int DEFAULT_CONNECT_TIMEOUT_MS = 1000;
	public static final int DEFAULT_HANDSHAKE_TIMEOUT_MS = 10000;
	public static final int DEFAULT_REQUESTED_HEARTBEAT_SEC = 30;
	public static final long DEFAULT_NETWORK_RECOVERY_INTERVAL_MS = 5000;

	// TODO 발송 관련 RabbitMQ 전용 property 이동 필요
	public static String OUT_BOUND_DEAD_QUEUE_NAME = "befw.dead.letter.queue";

	// TODO 발송 관련 RabbitMQ 전용 property 이동 필요
	public static int OUT_BOUND_RETRY_CNT = 3;

	@Getter
	String clientName;

	@Getter
	GsMsgSrvConnModel model;

	Object rabbitMqModel;    // TODO 추후 별도 테이블로 정의

	@Getter
	ConnectionFactory connectionFactory;

	public RabbitMqPropertyHandler(GsMsgSrvConnModel model) {

		// TODO rabbitMqModel은 RabbitMQ 전용 설정을 별도 테이블로 조회한 응답을 전달해야함. 추후 개발 현재는 DEFAULT로
		this(model, null);
	}

	public RabbitMqPropertyHandler(GsMsgSrvConnModel model, Object rabbitMqModel) {
		this.model = model;
		this.rabbitMqModel = rabbitMqModel;
		this.generateClientName();
		this.buildConnectionFactory();
	}

	private void buildConnectionFactory() {

		ConnectionFactory factory = new ConnectionFactory();

		factory.setHost(model.getHost());
		factory.setPort(model.getPort());
		factory.setVirtualHost(model.getDomain());
		factory.setUsername(model.getConnUser());
		factory.setPassword(model.getPwd());

		// TODO 추후 DB 기준 정보화
		if (rabbitMqModel == null) {
			factory.setConnectionTimeout(DEFAULT_CONNECT_TIMEOUT_MS);
			factory.setHandshakeTimeout(DEFAULT_HANDSHAKE_TIMEOUT_MS);
			factory.setRequestedHeartbeat(DEFAULT_REQUESTED_HEARTBEAT_SEC);
			factory.setAutomaticRecoveryEnabled(true);
			factory.setTopologyRecoveryEnabled(true);
			factory.setNetworkRecoveryInterval(DEFAULT_NETWORK_RECOVERY_INTERVAL_MS);
		}

		this.connectionFactory = factory;
	}

	private String generateClientName() {

		String format = "%s-%s-%s-%s-%s";
		String hostName = ServerNameUtil.getHostName();

		String name = String.format(format, ApplicationProperties.getApplicationTenant(),
			ApplicationProperties.getApplicationServiceName(), ApplicationProperties.getApplicationVersion(),
			hostName, DateTimeUtil.getServerSecondTime());

		clientName = name;
		log.info("clientName: {}", clientName);

		return name;
	}
}
