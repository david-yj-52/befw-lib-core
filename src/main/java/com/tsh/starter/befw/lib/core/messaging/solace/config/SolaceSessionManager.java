package com.tsh.starter.befw.lib.core.messaging.solace.config;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.solacesystems.jcsmp.JCSMPSession;
import com.tsh.starter.befw.lib.core.config.ApplicationProperties;
import com.tsh.starter.befw.lib.core.data.constant.UseYn;
import com.tsh.starter.befw.lib.core.data.orm.gnMsgSrvConn.GnMsgSrvConnModel;
import com.tsh.starter.befw.lib.core.messaging.AbstractMessageSessionManager;
import com.tsh.starter.befw.lib.core.messaging.MessagingConfManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SolaceSessionManager extends AbstractMessageSessionManager {

	private List<GnMsgSrvConnModel> connectionInfos;
	private ConcurrentHashMap<String, SolaceSessionHandler> handlerMap;

	public SolaceSessionManager(List<GnMsgSrvConnModel> infos) {

		log.info("groupId: {}, service:{}, version:{}", ApplicationProperties.getApplicationModuleName(),
			ApplicationProperties.getApplicationServiceName(), ApplicationProperties.getApplicationVersion());

		this.handlerMap = new ConcurrentHashMap<>();
		this.connectionInfos = infos;

		this.startSession();
	}

	public JCSMPSession getSession(String key) {
		return this.handlerMap.get(key).getSession();
	}

	public SolaceSessionHandler getHandler(String key) {
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

		boolean isDefaultClosed = this.handlerMap.get(MessagingConfManager.DEFAULT_KEY).getSession().isClosed();
		log.info("isDefaultClosed: {}", isDefaultClosed);

	}

	private void generateHandler() {
		log.info("generate connection vo.");

		for (GnMsgSrvConnModel model : connectionInfos) {

			String key = this.generateSessionKey(model);

			log.info("generated key: {}, model:{}", key, model.toString()); // ← 키 찍어보기
			SolaceSessionHandler handler = new SolaceSessionHandler(new SolacePropertyHandler(model));
			handler.startSession();
			this.handlerMap.put(key, handler);

		}
	}

	private String generateSessionKey(GnMsgSrvConnModel model) {
		if (UseYn.Y.equals(model.getDefaultYn())) {
			return MessagingConfManager.DEFAULT_KEY;
		} else {
			return model.getEnv() + "|" + model.getDomain();
		}

	}
}
