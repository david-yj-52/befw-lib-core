package com.tsh.starter.befw.lib.core.messaging.solace.inbound;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.ConsumerFlowProperties;
import com.solacesystems.jcsmp.EndpointProperties;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.Queue;
import com.solacesystems.jcsmp.TextMessage;

/**
 * Solace Queue 메시지 수신 처리 인터페이스
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
 * 4. Exception throw 시 ACK를 하지 않습니다. → Solace가 재전송합니다.
 * 5. 중복 수신 가능성이 있으므로 멱등성을 보장하세요.
 */
public interface SolaceMessageReceiver {

	/**
	 * 메시지 수신 시 Virtual Thread에서 호출됩니다.
	 * 동기 코드로 작성하세요.
	 *
	 * 정상 리턴 → Gateway ACK 전송 → Broker 다음 메시지 전송
	 * Exception → ACK 없음 → Broker 재전송
	 *
	 * @param message 수신된 메시지
	 * @throws Exception 처리 실패 시 throw → Solace 재전송 유도
	 */
	void onMessage(BytesXMLMessage message) throws Exception;

	/**
	 * FlowReceiver 레벨 예외 발생 시 호출됩니다.
	 * 필요 시 오버라이드하세요.
	 */
	default void onException(JCSMPException ex) {
	}

	/**
	 * 이 Receiver가 담당하는 Queue 이름을 반환합니다.
	 * SolaceInboundConfig에서 자동 등록 시 사용됩니다.
	 */
	List<String> getQueueNames();

	/**
	 * 메시지 payload를 String으로 추출하는 유틸 메서드입니다.
	 */
	default String extractPayload(BytesXMLMessage message) {
		// 1. TextMessage 처리 (JMS 호환 텍스트 메시지)
		if (message instanceof TextMessage textMessage) {
			return textMessage.getText();
		}

		// 2. Attachment 영역 확인 (가장 일반적)
		if (message.hasAttachment()) {
			ByteBuffer buf = message.getAttachmentByteBuffer();
			if (buf != null && buf.hasRemaining()) {
				byte[] bytes = new byte[buf.remaining()];
				buf.get(bytes);
				return new String(bytes, StandardCharsets.UTF_8);
			}
		}

		// 3. XML Content 영역 확인 (fallback) → getBytes() 사용
		if (message.hasContent()) {
			byte[] xmlBytes = message.getBytes();
			if (xmlBytes != null && xmlBytes.length > 0) {
				return new String(xmlBytes, StandardCharsets.UTF_8);
			}
		}

		return null;
	}

	/**
	 * ConsumerFlowProperties 설정
	 * 기본값을 사용하려면 오버라이드 불필요
	 * 커스텀이 필요한 경우 오버라이드해서 설정하세요.
	 */
	default ConsumerFlowProperties getFlowProperties(String queueName) {
		Queue queue = JCSMPFactory.onlyInstance().createQueue(queueName);
		ConsumerFlowProperties props = new ConsumerFlowProperties();
		props.setEndpoint(queue);
		props.setAckMode(JCSMPProperties.SUPPORTED_MESSAGE_ACK_CLIENT);
		props.setTransportWindowSize(1);
		return props;
	}

	/**
	 * EndpointProperties 설정
	 * 기본값을 사용하려면 오버라이드 불필요
	 * 커스텀이 필요한 경우 오버라이드해서 설정하세요.
	 */
	default EndpointProperties getEndpointProperties() {
		EndpointProperties props = new EndpointProperties();
		props.setPermission(EndpointProperties.PERMISSION_CONSUME);
		props.setAccessType(EndpointProperties.ACCESSTYPE_NONEXCLUSIVE);
		return props;
	}

}