package com.tsh.starter.befw.lib.core.messaging.solace;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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

		this.connectionInfos = infos;

		this.startSession();
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
			SolaceSessionHandler handler = new SolaceSessionHandler(new SolacePropertyHandler(model));
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
