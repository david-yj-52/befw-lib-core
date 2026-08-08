package com.tsh.starter.befw.lib.core.messaging.rabbitmq.outbound;

import java.util.function.BiConsumer;

import com.rabbitmq.client.ConfirmListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RabbitMqPublishCallback implements ConfirmListener {

	// ACK/NACK 발생 시 호출할 핸들러 — Publisher에서 재시도 로직 주입 (deliveryTag, multiple)
	private final BiConsumer<Long, Boolean> ackHandler;
	private final BiConsumer<Long, Boolean> nackHandler;

	public RabbitMqPublishCallback(BiConsumer<Long, Boolean> ackHandler, BiConsumer<Long, Boolean> nackHandler) {
		this.ackHandler = ackHandler;
		this.nackHandler = nackHandler;
	}

	@Override
	public void handleAck(long deliveryTag, boolean multiple) {
		log.debug("[RabbitMQ] Publish ACK — deliveryTag: {}, multiple: {}", deliveryTag, multiple);
		ackHandler.accept(deliveryTag, multiple);
	}

	@Override
	public void handleNack(long deliveryTag, boolean multiple) {
		log.error("[RabbitMQ] Publish NACK — deliveryTag: {}, multiple: {}", deliveryTag, multiple);
		nackHandler.accept(deliveryTag, multiple);
	}
}
