package com.tsh.starter.befw.lib.core.messaging.rabbitmq.inbound;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

import org.springframework.stereotype.Component;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMqInboundGateway {

	// Queue명 → 전용 Consumer Channel 관리
	private final Map<String, Channel> channelMap = new ConcurrentHashMap<>();
	// Queue명 → consumerTag 관리 (unregister 시 basicCancel 용도)
	private final Map<String, String> consumerTagMap = new ConcurrentHashMap<>();

	@Setter
	private Connection connection;

	/**
	 * Queue를 등록하고 Consumer를 시작합니다.
	 * RabbitMqMessageReceiver.getQueueNames()으로 Queue명을 가져옵니다.
	 */
	public void register(RabbitMqMessageReceiver receiver) throws IOException {

		// ✅ Connection 체크
		if (connection == null || !connection.isOpen()) {
			throw new IllegalStateException(
				"[RabbitMQ] Connection is not set or closed. Call setConnection() before register().");
		}

		List<String> queueNames = receiver.getQueueNames();

		if (queueNames == null || queueNames.isEmpty()) {
			log.warn("[RabbitMQ] No queues to register — receiver: {}",
				receiver.getClass().getSimpleName());
			return;
		}

		for (String queueName : queueNames) {
			this.registerSingleQueue(queueName, receiver);
		}

	}

	/**
	 * Queue 등록을 해제하고 Consumer를 종료합니다.
	 */
	public void unregister(String queueName) {
		Channel channel = channelMap.remove(queueName);
		String consumerTag = consumerTagMap.remove(queueName);

		if (channel != null) {
			this.closeChannel(queueName, channel, consumerTag);
			log.info("[RabbitMQ] Unregistered — queue: {}", queueName);
		}
	}

	// ──────────────────────────────────────────────
	// Internal
	// ──────────────────────────────────────────────

	private void registerSingleQueue(String queueName,
		RabbitMqMessageReceiver receiver) throws IOException {
		if (channelMap.containsKey(queueName)) {
			log.warn("[RabbitMQ] Already registered — queue: {}", queueName);
			return;
		}

		Channel channel = connection.createChannel();
		channel.basicQos(receiver.getPrefetchCount());

		if (receiver.isAutoDeclare()) {
			channel.queueDeclare(queueName, true, false, false, null);
		}

		String consumerTag = channel.basicConsume(queueName, false,
			buildConsumer(channel, queueName, receiver));

		channelMap.put(queueName, channel);
		consumerTagMap.put(queueName, consumerTag);

		log.info("[RabbitMQ] Registered — queue: {}, receiver: {}",
			queueName, receiver.getClass().getSimpleName());
	}

	private DefaultConsumer buildConsumer(Channel channel, String queueName,
		RabbitMqMessageReceiver receiver) {
		return new DefaultConsumer(channel) {

			@Override
			public void handleDelivery(String consumerTag, Envelope envelope,
				AMQP.BasicProperties properties, byte[] body) {
				// ✅ RabbitMQ dispatch 스레드 즉시 반환
				// ✅ Queue 전용 Virtual Thread 생성 → 비동기 처리
				Thread.ofVirtual()
					.name("rabbitmq-vt-" + queueName)
					.start(() -> {
						long deliveryTag = envelope.getDeliveryTag();
						try {
							log.debug("[RabbitMQ] onReceive — queue: {}, redelivered: {}",
								queueName, envelope.isRedeliver());

							receiver.onMessage(body, envelope, properties); // 비즈니스 로직 완료 대기

							channel.basicAck(deliveryTag, false); // ✅ 처리 완료 후 ACK → Broker 다음 메시지 전송

							log.debug("[RabbitMQ] ACK sent — queue: {}", queueName);

						} catch (Exception e) {
							log.error("[RabbitMQ] Processing failed — queue: {}, redelivered: {}",
								queueName, envelope.isRedeliver(), e);
							try {
								// ✅ requeue 없이 NACK → Queue에 DLX가 설정된 경우 Dead Letter로 이동
								channel.basicNack(deliveryTag, false, false);
							} catch (IOException ioException) {
								log.error("[RabbitMQ] Failed to send NACK — queue: {}", queueName, ioException);
							}
							receiver.onException(e);
						}
					});
			}

			@Override
			public void handleCancel(String consumerTag) {
				log.error("[RabbitMQ] Consumer cancelled — queue: {}, consumerTag: {}", queueName, consumerTag);
				receiver.onException(new IOException("Consumer cancelled: " + queueName));
			}
		};
	}

	private void closeChannel(String queueName, Channel channel, String consumerTag) {
		try {
			if (consumerTag != null && channel.isOpen()) {
				channel.basicCancel(consumerTag);
			}
			channel.close();
		} catch (IOException | TimeoutException e) {
			log.warn("[RabbitMQ] Failed to close channel — queue: {}", queueName, e);
		}
	}

	@PreDestroy
	public void shutdown() {
		channelMap.forEach((queueName, channel) ->
			this.closeChannel(queueName, channel, consumerTagMap.get(queueName)));
		channelMap.clear();
		consumerTagMap.clear();
		log.info("[RabbitMQ] All flows closed.");
	}
}
