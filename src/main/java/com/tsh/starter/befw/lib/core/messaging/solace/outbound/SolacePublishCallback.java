package com.tsh.starter.befw.lib.core.messaging.solace.outbound;

import java.util.function.Consumer;

import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPStreamingPublishCorrelatingEventHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SolacePublishCallback implements JCSMPStreamingPublishCorrelatingEventHandler {

	// NACK 발생 시 호출할 핸들러 — Publisher에서 재시도 로직 주입
	private final Consumer<Object> nackHandler;

	public SolacePublishCallback(Consumer<Object> nackHandler) {
		this.nackHandler = nackHandler;
	}

	@Override
	public void responseReceivedEx(Object correlationKey) {
		log.debug("[Solace] Publish ACK — correlationKey: {}", correlationKey);
	}

	@Override
	public void handleErrorEx(Object correlationKey, JCSMPException ex, long timestamp) {
		log.error("[Solace] Publish NACK — correlationKey: {}, error: {}",
			correlationKey, ex.getMessage());
		nackHandler.accept(correlationKey);
	}
}