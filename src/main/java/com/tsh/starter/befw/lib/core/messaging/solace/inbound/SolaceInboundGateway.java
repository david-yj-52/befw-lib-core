package com.tsh.starter.befw.lib.core.messaging.solace.inbound;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.ConsumerFlowProperties;
import com.solacesystems.jcsmp.EndpointProperties;
import com.solacesystems.jcsmp.FlowReceiver;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.XMLMessageListener;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SolaceInboundGateway {

	// Queue명 → FlowReceiver 관리
	private final Map<String, FlowReceiver> flowMap = new ConcurrentHashMap<>();

	@Setter
	private JCSMPSession session;

	/**
	 * Queue를 등록하고 FlowReceiver를 시작합니다.
	 * SolaceMessageReceiver.getQueueName()으로 Queue명을 가져옵니다.
	 */
	public void register(SolaceMessageReceiver receiver) throws JCSMPException {

		// ✅ #3 — Session 체크
		if (session == null || session.isClosed()) {
			throw new IllegalStateException(
				"[Solace] Session is not set or closed. Call setSession() before register().");
		}

		List<String> queueNames = receiver.getQueueNames();

		if (queueNames == null || queueNames.isEmpty()) {
			log.warn("[Solace] No queues to register — receiver: {}",
				receiver.getClass().getSimpleName());
			return;
		}

		for (String queueName : queueNames) {
			this.registerSingleQueue(queueName, receiver);
		}

	}

	/**
	 * Queue 등록을 해제하고 FlowReceiver를 종료합니다.
	 */
	public void unregister(String queueName) {
		FlowReceiver flow = flowMap.remove(queueName);
		if (flow != null) {
			flow.close();
			log.info("[Solace] Unregistered — queue: {}", queueName);
		}
	}

	// ──────────────────────────────────────────────
	// Internal
	// ──────────────────────────────────────────────

	private void registerSingleQueue(String queueName,
		SolaceMessageReceiver receiver) throws JCSMPException {
		if (flowMap.containsKey(queueName)) {
			log.warn("[Solace] Already registered — queue: {}", queueName);
			return;
		}

		ConsumerFlowProperties flowProps = receiver.getFlowProperties(queueName);
		EndpointProperties endpointProps = receiver.getEndpointProperties();

		FlowReceiver flow = session.createFlow(
			buildListener(queueName, receiver),
			flowProps,
			endpointProps
		);

		flow.start();
		flowMap.put(queueName, flow);

		log.info("[Solace] Registered — queue: {}, receiver: {}",
			queueName, receiver.getClass().getSimpleName());
	}

	private XMLMessageListener buildListener(String queueName, SolaceMessageReceiver receiver) {
		return new XMLMessageListener() {

			@Override
			public void onReceive(BytesXMLMessage message) {
				// ✅ JCSMP dispatch 스레드 즉시 반환
				// ✅ Queue 전용 Virtual Thread 생성 → 비동기 처리
				Thread.ofVirtual()
					.name("solace-vt-" + queueName)
					.start(() -> {
						try {
							log.debug("[Solace] onReceive — queue: {}, redelivered: {}",
								queueName, message.getRedelivered());

							receiver.onMessage(message); // 비즈니스 로직 완료 대기

							message.ackMessage(); // ✅ 처리 완료 후 ACK → Broker 다음 메시지 전송

							log.debug("[Solace] ACK sent — queue: {}", queueName);

						} catch (Exception e) {
							log.error("[Solace] Processing failed — queue: {}, redelivered: {}",
								queueName, message.getRedelivered(), e);
							message.ackMessage(); // ✅ 처리 완료 후 ACK → Broker 다음 메시지 전송
							receiver.onException((JCSMPException)
								(e instanceof JCSMPException ? e
									: new JCSMPException(e.getMessage())));
						}
					});
			}

			@Override
			public void onException(JCSMPException ex) {
				log.error("[Solace] FlowReceiver exception — queue: {}", queueName, ex);
				receiver.onException(ex);
			}
		};
	}

	@PreDestroy
	public void shutdown() {
		flowMap.forEach((queueName, flow) -> {
			flow.close();
			log.info("[Solace] Flow closed — queue: {}", queueName);
		});
		flowMap.clear();
	}
}