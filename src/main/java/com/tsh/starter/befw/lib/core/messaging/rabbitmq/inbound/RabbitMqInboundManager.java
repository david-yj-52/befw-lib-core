package com.tsh.starter.befw.lib.core.messaging.rabbitmq.inbound;

import java.io.IOException;
import java.util.List;

import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RabbitMqInboundManager {

	private final RabbitMqInboundGateway gateway;

	/**
	 * Spring이 RabbitMqMessageReceiver 구현체를 자동으로 모두 주입합니다.
	 * 새로운 Receiver를 추가할 때 이 파일을 수정할 필요가 없습니다.
	 * @Component 선언만 하면 자동 등록됩니다.
	 */
	private final List<RabbitMqMessageReceiver> receivers;

	// @PostConstruct
	public void registerAll() {
		receivers.forEach(receiver -> {
			try {
				gateway.register(receiver);
			} catch (IOException e) {
				log.error("[RabbitMQ] Failed to register receiver — queues: {}, receiver: {}",
					receiver.getQueueNames(),
					receiver.getClass().getSimpleName(), e);
				throw new IllegalStateException(
					"RabbitMQ receiver registration failed: " + receiver.getQueueNames(), e);
			}
		});
	}
}
