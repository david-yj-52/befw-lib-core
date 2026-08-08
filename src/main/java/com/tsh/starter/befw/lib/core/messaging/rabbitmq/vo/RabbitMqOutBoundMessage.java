package com.tsh.starter.befw.lib.core.messaging.rabbitmq.vo;

import java.util.Map;

import com.tsh.starter.befw.lib.core.spec.constant.ApMessageList;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@NoArgsConstructor
public class RabbitMqOutBoundMessage {
	Map<String, Object> msgHeader;
	ApMessageList eventNm;
	private String destination;      // publishToQueue: Queue명 / publishToTopic: Routing Key
	private String exchange;         // publishToTopic 전용 Exchange명. 미지정 시 destination을 Exchange명으로 사용
	private String payload;          // 메시지 내용 (JSON String)
	private Boolean persistent;      // true: PERSISTENT(deliveryMode=2), false/null: NON-PERSISTENT(deliveryMode=1)
	private Integer timeToLive;      // TTL (ms), null이면 무제한
	private String correlationId;    // 요청 추적용 ID
}
