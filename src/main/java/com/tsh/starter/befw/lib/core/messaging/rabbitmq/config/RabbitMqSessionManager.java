package com.tsh.starter.befw.lib.core.messaging.rabbitmq.config;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.rabbitmq.client.Connection;
import com.tsh.starter.befw.lib.core.config.ApplicationProperties;
import com.tsh.starter.befw.lib.core.data.constant.UseYn;
import com.tsh.starter.befw.lib.core.data.orm.msgServiceConn.gsMsgSrvConn.GsMsgSrvConnModel;
import com.tsh.starter.befw.lib.core.messaging.AbstractMessageSessionManager;
import com.tsh.starter.befw.lib.core.messaging.MessagingConfManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RabbitMqSessionManager extends AbstractMessageSessionManager {

	private List<GsMsgSrvConnModel> connectionInfos;
	private ConcurrentHashMap<String, RabbitMqSessionHandler> handlerMap;

	public RabbitMqSessionManager(List<GsMsgSrvConnModel> infos) {

		log.info("groupId: {}, service:{}, version:{}", ApplicationProperties.getApplicationModuleName(),
			ApplicationProperties.getApplicationServiceName(), ApplicationProperties.getApplicationVersion());

		this.handlerMap = new ConcurrentHashMap<>();
		this.connectionInfos = infos;

		this.startSession();
	}

	public Connection getConnection(String key) {
		return this.handlerMap.get(key).getConnection();
	}

	public RabbitMqSessionHandler getHandler(String key) {
		return this.handlerMap.get(key);
	}

	@Override
	protected void startSession() {
		log.info("start session generate.");
		this.generateHandler();

	}

	@Override
	protected void stopSession() {

	}

	@Override
	protected void checkSession() {

		boolean isDefaultClosed = this.handlerMap.get(MessagingConfManager.DEFAULT_KEY).isClosed();
		log.info("isDefaultClosed: {}", isDefaultClosed);

	}

	private void generateHandler() {
		log.info("generate connection vo.");

		for (GsMsgSrvConnModel model : connectionInfos) {

			String key = this.generateSessionKey(model);

			log.info("generated key: {}, model:{}", key, model.toString());
			RabbitMqSessionHandler handler = new RabbitMqSessionHandler(new RabbitMqPropertyHandler(model));
			handler.startSession();
			this.handlerMap.put(key, handler);

		}
	}

	private String generateSessionKey(GsMsgSrvConnModel model) {
		if (UseYn.Y.equals(model.getDefaultYn())) {
			return MessagingConfManager.DEFAULT_KEY;
		} else {
			return model.getEnv() + "|" + model.getDomain();
		}

	}
}
