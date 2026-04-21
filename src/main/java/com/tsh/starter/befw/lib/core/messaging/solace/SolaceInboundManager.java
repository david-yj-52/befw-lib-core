package com.tsh.starter.befw.lib.core.messaging.solace;

import java.util.List;

import org.springframework.context.annotation.Configuration;

import com.solacesystems.jcsmp.JCSMPException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SolaceInboundManager {

	private final SolaceInboundGateway gateway;

	/**
	 * Spring이 SolaceMessageReceiver 구현체를 자동으로 모두 주입합니다.
	 * 새로운 Receiver를 추가할 때 이 파일을 수정할 필요가 없습니다.
	 * @Component 선언만 하면 자동 등록됩니다.
	 */
	private final List<SolaceMessageReceiver> receivers;

	// @PostConstruct
	public void registerAll() {
		receivers.forEach(receiver -> {
			try {
				gateway.register(receiver);
			} catch (JCSMPException e) {
				log.error("[Solace] Failed to register receiver — queues: {}, receiver: {}",
					receiver.getQueueNames(),
					receiver.getClass().getSimpleName(), e);
				throw new IllegalStateException(
					"Solace receiver registration failed: " + receiver.getQueueNames(), e);
			}
		});
	}
}