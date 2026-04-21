package com.tsh.starter.befw.lib.core.messaging.solace.outbound;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import com.solacesystems.jcsmp.DeliveryMode;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.Queue;
import com.solacesystems.jcsmp.SDTMap;
import com.solacesystems.jcsmp.TextMessage;
import com.solacesystems.jcsmp.Topic;
import com.solacesystems.jcsmp.XMLMessageProducer;
import com.tsh.starter.befw.lib.core.messaging.solace.config.SolacePropertyHandler;
import com.tsh.starter.befw.lib.core.messaging.solace.vo.SolaceOutBoundMessage;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SolaceMessagePublisher {

	// correlationId → 재시도 횟수 추적
	private final Map<String, AtomicInteger> retryCountMap = new ConcurrentHashMap<>();
	// correlationId → 원본 메시지 보관 (재시도용)
	private final Map<String, SolaceOutBoundMessage> pendingMap = new ConcurrentHashMap<>();
	private JCSMPSession session;
	private XMLMessageProducer producer;

	// ──────────────────────────────────────────────
	// Session 주입 — AppStarter에서 호출
	// ──────────────────────────────────────────────

	public void setSession(JCSMPSession session) throws JCSMPException {
		this.session = session;
		this.producer = session.getMessageProducer(
			new SolacePublishCallback(this::handleNack)
		);
		log.info("[Solace] Publisher initialized.");
	}

	// ──────────────────────────────────────────────
	// 발송 API
	// ──────────────────────────────────────────────

	/**
	 * Guaranteed 메시지 — Queue 발송
	 * ACK 기반 전달 보장, 실패 시 3회 재시도 후 Dead Letter Queue 발송
	 */
	public void publishToQueue(SolaceOutBoundMessage message) throws JCSMPException {
		validateSession();
		String correlationId = resolveCorrelationId(message);
		pendingMap.put(correlationId, message);
		retryCountMap.put(correlationId, new AtomicInteger(0));
		doPublishToQueue(message, correlationId);
	}

	/**
	 * Direct 메시지 — Topic 발송
	 * Fire and forget, ACK 없음
	 */
	public void publishToTopic(SolaceOutBoundMessage message) throws JCSMPException {

		validateSession();
		TextMessage textMessage = buildTextMessage(message);
		textMessage.setProperties(this.buildSdtMap(message.getMsgHeader()));
		Topic topic = JCSMPFactory.onlyInstance().createTopic(message.getDestination());
		producer.send(textMessage, topic);
		log.debug("[Solace] Direct published — topic: {}", message.getDestination());
	}

	// ──────────────────────────────────────────────
	// Internal
	// ──────────────────────────────────────────────

	/**
	 * Map<String, Object> → SDTMap 변환
	 * 지원 타입: String, Integer, Long, Double, Boolean, Float, byte[]
	 */
	private SDTMap buildSdtMap(Map<String, Object> msgProp) throws JCSMPException {
		SDTMap sdtMap = JCSMPFactory.onlyInstance().createMap();

		for (Map.Entry<String, Object> entry : msgProp.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();

			if (value instanceof String v)
				sdtMap.putString(key, v);
			else if (value instanceof Integer v)
				sdtMap.putInteger(key, v);
			else if (value instanceof Long v)
				sdtMap.putLong(key, v);
			else if (value instanceof Double v)
				sdtMap.putDouble(key, v);
			else if (value instanceof Boolean v)
				sdtMap.putBoolean(key, v);
			else if (value instanceof Float v)
				sdtMap.putFloat(key, v);
			else if (value instanceof byte[] v)
				sdtMap.putBytes(key, v);
			else if (value == null)
				sdtMap.putString(key, null);
			else {
				// 지원하지 않는 타입은 toString() 으로 변환
				log.warn("[Solace] Unsupported SDTMap value type: {} — key: {}, converting to String",
					value.getClass().getSimpleName(), key);
				sdtMap.putString(key, value.toString());
			}
		}
		return sdtMap;
	}

	private void doPublishToQueue(SolaceOutBoundMessage message,
		String correlationId) throws JCSMPException {
		TextMessage textMessage = buildTextMessage(message);
		textMessage.setCorrelationKey(correlationId); // ACK 콜백에서 식별용
		textMessage.setDeliveryMode(DeliveryMode.PERSISTENT);

		textMessage.setProperties(this.buildSdtMap(message.getMsgHeader()));
		if (message.getTimeToLive() != null) {
			textMessage.setTimeToLive(message.getTimeToLive());
		}

		Queue queue = JCSMPFactory.onlyInstance().createQueue(message.getDestination());
		producer.send(textMessage, queue);

		log.debug("[Solace] Guaranteed published — queue: {}, correlationId: {}",
			message.getDestination(), correlationId);
	}

	/**
	 * NACK 수신 시 재시도 처리
	 * 3회 초과 시 Dead Letter Queue로 발송
	 */
	private void handleNack(Object correlationKey) {
		String correlationId = correlationKey.toString();
		SolaceOutBoundMessage original = pendingMap.get(correlationId);
		AtomicInteger retryCount = retryCountMap.get(correlationId);

		if (original == null || retryCount == null) {
			log.warn("[Solace] NACK received but no pending message — correlationId: {}", correlationId);
			return;
		}

		int count = retryCount.incrementAndGet();

		if (count <= SolacePropertyHandler.OUT_BOUND_RETRY_CNT) {
			// ✅ 재시도
			log.warn("[Solace] Retrying ({}/{}) — queue: {}, correlationId: {}",
				count, SolacePropertyHandler.OUT_BOUND_RETRY_CNT,
				original.getDestination(), correlationId);
			try {
				doPublishToQueue(original, correlationId);
			} catch (JCSMPException e) {
				log.error("[Solace] Retry failed — correlationId: {}", correlationId, e);
				sendToDeadLetterQueue(original, correlationId);
			}
		} else {
			// ❌ 재시도 횟수 초과 → Dead Letter Queue
			log.error("[Solace] Exceeded retry count ({}) — sending to DLQ. correlationId: {}",
				SolacePropertyHandler.OUT_BOUND_RETRY_CNT, correlationId);
			sendToDeadLetterQueue(original, correlationId);
		}
	}

	/**
	 * Dead Letter Queue 발송
	 */
	private void sendToDeadLetterQueue(SolaceOutBoundMessage original,
		String correlationId) {
		try {
			SolaceOutBoundMessage dlqMessage = SolaceOutBoundMessage.builder()
				.destination(SolacePropertyHandler.OUT_BOUND_DEAD_QUEUE_NAME)
				.payload(original.getPayload())
				.deliveryMode(DeliveryMode.PERSISTENT)
				.correlationId(correlationId)
				.build();

			doPublishToQueue(dlqMessage, correlationId + ".dlq");

			log.warn("[Solace] Sent to DLQ — queue: {}, correlationId: {}",
				SolacePropertyHandler.OUT_BOUND_DEAD_QUEUE_NAME, correlationId);

		} catch (JCSMPException e) {
			log.error("[Solace] DLQ publish failed — correlationId: {}", correlationId, e);
		} finally {
			// 추적 맵에서 제거
			pendingMap.remove(correlationId);
			retryCountMap.remove(correlationId);
		}
	}

	private TextMessage buildTextMessage(SolaceOutBoundMessage message) {
		TextMessage textMessage = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
		textMessage.setText(message.getPayload());
		textMessage.setDeliveryMode(message.getDeliveryMode() != null
			? message.getDeliveryMode()
			: DeliveryMode.PERSISTENT);
		return textMessage;
	}

	private String resolveCorrelationId(SolaceOutBoundMessage message) {
		return message.getCorrelationId() != null
			? message.getCorrelationId()
			: java.util.UUID.randomUUID().toString();
	}

	private void validateSession() {
		if (session == null || session.isClosed()) {
			throw new IllegalStateException(
				"[Solace] Session is not set or closed. Call setSession() before publish.");
		}
	}

	@PreDestroy
	public void shutdown() {
		if (producer != null) {
			producer.close();
			log.info("[Solace] Producer closed.");
		}
	}
}