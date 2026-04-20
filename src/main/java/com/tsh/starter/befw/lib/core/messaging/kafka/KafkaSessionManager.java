package com.tsh.starter.befw.lib.core.messaging.kafka;

import java.util.List;

import com.tsh.starter.befw.lib.core.config.ApplicationProperties;
import com.tsh.starter.befw.lib.core.data.orm.gnMsgSrvConn.GnMsgSrvConnModel;
import com.tsh.starter.befw.lib.core.messaging.AbstractMessageSessionManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KafkaSessionManager extends AbstractMessageSessionManager {

	public KafkaSessionManager(List<GnMsgSrvConnModel> infos) {

		log.info("groupId: {}, service:{}, version:{}", ApplicationProperties.getApplicationModuleName(),
			ApplicationProperties.getApplicationServiceName(), ApplicationProperties.getApplicationVersion());

	}

	@Override
	protected void stopSession() {

	}

	@Override
	protected void startSession() {

	}

	@Override
	protected void checkSession() {

	}
}
