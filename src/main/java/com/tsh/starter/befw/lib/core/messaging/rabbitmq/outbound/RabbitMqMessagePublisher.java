package com.tsh.starter.befw.lib.core.messaging.rabbitmq.outbound;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.tsh.starter.befw.lib.core.messaging.rabbitmq.config.RabbitMqPropertyHandler;
import com.tsh.starter.befw.lib.core.messaging.rabbitmq.vo.RabbitMqOutBoundMessage;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMqMessagePublisher {

	private static final String DEFAULT_EXCHANGE = "";

	// deliveryTag → correlationId (Publisher Confirm 매칭용)
	private final NavigableMap<Long, String> deliveryTagMap = new ConcurrentSkipListMap<>();
	// correlationId → 재시도 횟수 추적
	private final Map<String, AtomicInteger> retryCountMap = new ConcurrentHashMap<>();
	// correlationId → 원본 메시지 보관 (재시도용)
	private final Map<String, RabbitMqOutBoundMessage> pendingMap = new ConcurrentHashMap<>();

	private Channel channel;

	// ──────────────────────────────────────────────
	// Channel 주입 — AppStarter에서 호출
	// ──────────────────────────────────────────────

	public void setChannel(Channel channel) throws IOException {
		this.channel = channel;
		this.channel.confirmSelect();
		this.channel.addConfirmListener(
			new RabbitMqPublishCallback(this::handleAck, this::handleNack));
		log.info("[RabbitMQ] Publisher initialized.");
	}

	// ──────────────────────────────────────────────
	// 발송 API
	// ──────────────────────────────────────────────

	/**
	 * Guaranteed 메시지 — Queue 발송 (Default Exchange 사용)
	 * Publisher Confirm 기반 전달 보장, 실패 시 3회 재시도 후 Dead Letter Queue 발송
	 */
	public void publishToQueue(RabbitMqOutBoundMessage message) throws IOException {
		validateChannel();
		String correlationId = resolveCorrelationId(message);
		pendingMap.put(correlationId, message);
		retryCountMap.putIfAbsent(correlationId, new AtomicInteger(0));
		doPublishToQueue(message, correlationId);
	}

	/**
	 * Direct 메시지 — Exchange(Topic 등) 발송
	 * Fire and forget, Confirm 결과는 로그만 남기고 재시도하지 않음
	 */
	public void publishToTopic(RabbitMqOutBoundMessage message) throws IOException {

		validateChannel();

		String exchange = message.getExchange() != null ? message.getExchange() : message.getDestination();
		boolean persistent = Boolean.TRUE.equals(message.getPersistent());
		AMQP.BasicProperties properties = buildProperties(message, persistent, message.getCorrelationId());

		channel.basicPublish(exchange, message.getDestination(), properties,
			message.getPayload().getBytes(StandardCharsets.UTF_8));

		log.debug("[RabbitMQ] Direct published — exchange: {}, routingKey: {}",
			exchange, message.getDestination());
	}

	// ──────────────────────────────────────────────
	// Internal
	// ──────────────────────────────────────────────

	private void doPublishToQueue(RabbitMqOutBoundMessage message, String correlationId) throws IOException {
		AMQP.BasicProperties properties = buildProperties(message, true, correlationId);

		long deliveryTag = channel.getNextPublishSeqNo();
		deliveryTagMap.put(deliveryTag, correlationId);

		channel.basicPublish(DEFAULT_EXCHANGE, message.getDestination(), properties,
			message.getPayload().getBytes(StandardCharsets.UTF_8));

		log.debug("[RabbitMQ] Guaranteed published — queue: {}, correlationId: {}, deliveryTag: {}",
			message.getDestination(), correlationId, deliveryTag);
	}

	/**
	 * Solace의 SDTMap과 달리 RabbitMQ headers table은 Map<String,Object>를 그대로 사용할 수 있습니다.
	 * (지원 타입: String, Integer, Long, Double, Boolean, byte[], List, Map 등)
	 */
	private AMQP.BasicProperties buildProperties(RabbitMqOutBoundMessage message, boolean persistent,
		String correlationId) {

		AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder()
			.contentType("text/plain")
			.deliveryMode(persistent ? 2 : 1)
			.correlationId(correlationId)
			.headers(message.getMsgHeader());

		if (message.getTimeToLive() != null) {
			builder.expiration(String.valueOf(message.getTimeToLive()));
		}

		return builder.build();
	}

	/**
	 * ACK 수신 시 추적 정보 정리
	 */
	private void handleAck(long deliveryTag, boolean multiple) {
		resolveDeliveryTags(deliveryTag, multiple).forEach(this::clearTracking);
	}

	/**
	 * NACK 수신 시 재시도 처리
	 * 3회 초과 시 Dead Letter Queue로 발송
	 */
	private void handleNack(long deliveryTag, boolean multiple) {
		resolveDeliveryTags(deliveryTag, multiple).forEach(this::retryOrDeadLetter);
	}

	private List<String> resolveDeliveryTags(long deliveryTag, boolean multiple) {
		List<String> correlationIds = new ArrayList<>();

		if (multiple) {
			NavigableMap<Long, String> confirmed = deliveryTagMap.headMap(deliveryTag, true);
			correlationIds.addAll(confirmed.values());
			confirmed.clear();
		} else {
			String correlationId = deliveryTagMap.remove(deliveryTag);
			if (correlationId != null) {
				correlationIds.add(correlationId);
			}
		}

		return correlationIds;
	}

	private void retryOrDeadLetter(String correlationId) {
		RabbitMqOutBoundMessage original = pendingMap.get(correlationId);
		AtomicInteger retryCount = retryCountMap.get(correlationId);

		if (original == null || retryCount == null) {
			log.warn("[RabbitMQ] NACK received but no pending message — correlationId: {}", correlationId);
			return;
		}

		int count = retryCount.incrementAndGet();

		if (count <= RabbitMqPropertyHandler.OUT_BOUND_RETRY_CNT) {
			// ✅ 재시도
			log.warn("[RabbitMQ] Retrying ({}/{}) — queue: {}, correlationId: {}",
				count, RabbitMqPropertyHandler.OUT_BOUND_RETRY_CNT,
				original.getDestination(), correlationId);
			try {
				doPublishToQueue(original, correlationId);
			} catch (IOException e) {
				log.error("[RabbitMQ] Retry failed — correlationId: {}", correlationId, e);
				sendToDeadLetterQueue(original, correlationId);
			}
		} else {
			// ❌ 재시도 횟수 초과 → Dead Letter Queue
			log.error("[RabbitMQ] Exceeded retry count ({}) — sending to DLQ. correlationId: {}",
				RabbitMqPropertyHandler.OUT_BOUND_RETRY_CNT, correlationId);
			sendToDeadLetterQueue(original, correlationId);
		}
	}

	/**
	 * Dead Letter Queue 발송
	 */
	private void sendToDeadLetterQueue(RabbitMqOutBoundMessage original, String correlationId) {
		try {
			RabbitMqOutBoundMessage dlqMessage = RabbitMqOutBoundMessage.builder()
				.destination(RabbitMqPropertyHandler.OUT_BOUND_DEAD_QUEUE_NAME)
				.payload(original.getPayload())
				.persistent(true)
				.correlationId(correlationId)
				.build();

			doPublishToQueue(dlqMessage, correlationId + ".dlq");

			log.warn("[RabbitMQ] Sent to DLQ — queue: {}, correlationId: {}",
				RabbitMqPropertyHandler.OUT_BOUND_DEAD_QUEUE_NAME, correlationId);

		} catch (IOException e) {
			log.error("[RabbitMQ] DLQ publish failed — correlationId: {}", correlationId, e);
		} finally {
			clearTracking(correlationId);
		}
	}

	private void clearTracking(String correlationId) {
		pendingMap.remove(correlationId);
		retryCountMap.remove(correlationId);
	}

	private String resolveCorrelationId(RabbitMqOutBoundMessage message) {
		return message.getCorrelationId() != null
			? message.getCorrelationId()
			: UUID.randomUUID().toString();
	}

	private void validateChannel() {
		if (channel == null || !channel.isOpen()) {
			throw new IllegalStateException(
				"[RabbitMQ] Channel is not set or closed. Call setChannel() before publish.");
		}
	}

	@PreDestroy
	public void shutdown() {
		if (channel != null) {
			try {
				channel.close();
				log.info("[RabbitMQ] Publisher channel closed.");
			} catch (IOException | TimeoutException e) {
				log.warn("[RabbitMQ] Failed to close publisher channel.", e);
			}
		}
	}
}
