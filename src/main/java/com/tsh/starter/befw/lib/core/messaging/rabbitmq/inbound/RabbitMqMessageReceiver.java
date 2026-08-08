package com.tsh.starter.befw.lib.core.messaging.rabbitmq.inbound;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;

/**
 * RabbitMQ Queue 메시지 수신 처리 인터페이스
 *
 * Tomcat과 동일한 구조로 동기 코드로 구현하세요.
 *
 * [처리 흐름]
 * 메시지 수신 → Virtual Thread 할당 → onMessage() → Service → Repository → ACK
 *
 * [구현 규칙]
 * 1. onMessage()는 Virtual Thread에서 실행됩니다.
 * 2. 비즈니스 로직을 동기 코드로 작성하세요. (Tomcat Controller와 동일)
 * 3. 정상 리턴 시 Gateway가 ACK를 전송합니다.
 * 4. Exception throw 시 NACK(requeue=false)를 전송합니다. → Queue에 DLX가 설정된 경우 Dead Letter로 이동합니다.
 * 5. 중복 수신 가능성이 있으므로 멱등성을 보장하세요.
 */
public interface RabbitMqMessageReceiver {

	/**
	 * 메시지 수신 시 Virtual Thread에서 호출됩니다.
	 * 동기 코드로 작성하세요.
	 *
	 * 정상 리턴 → Gateway ACK 전송 → Broker 다음 메시지 전송
	 * Exception → NACK(requeue=false) 전송 → DLX 설정 시 Dead Letter Queue로 이동
	 *
	 * @param body 수신된 메시지 payload
	 * @param envelope 라우팅 정보 (exchange, routingKey, deliveryTag, redeliver 여부)
	 * @param properties 메시지 속성 (headers, correlationId 등)
	 * @throws Exception 처리 실패 시 throw → NACK 유도
	 */
	void onMessage(byte[] body, Envelope envelope, AMQP.BasicProperties properties) throws Exception;

	/**
	 * Consumer 레벨 예외 발생 시 호출됩니다.
	 * 필요 시 오버라이드하세요.
	 */
	default void onException(Exception ex) {
	}

	/**
	 * 이 Receiver가 담당하는 Queue 이름을 반환합니다.
	 * RabbitMqInboundManager에서 자동 등록 시 사용됩니다.
	 */
	List<String> getQueueNames();

	/**
	 * 메시지 payload를 String으로 추출하는 유틸 메서드입니다.
	 */
	default String extractPayload(byte[] body) {
		if (body == null) {
			return null;
		}
		return new String(body, StandardCharsets.UTF_8);
	}

	/**
	 * Consumer 등록 시 QoS(prefetchCount) 설정
	 * 기본값을 사용하려면 오버라이드 불필요
	 * 커스텀이 필요한 경우 오버라이드해서 설정하세요.
	 */
	default int getPrefetchCount() {
		return 1;
	}

	/**
	 * Queue가 브로커에 없을 경우 자동 선언 여부.
	 * 운영 환경은 브로커에서 사전 정의하는 것을 권장하므로 기본값은 false 입니다.
	 * 커스텀이 필요한 경우 오버라이드해서 설정하세요.
	 */
	default boolean isAutoDeclare() {
		return false;
	}

}
