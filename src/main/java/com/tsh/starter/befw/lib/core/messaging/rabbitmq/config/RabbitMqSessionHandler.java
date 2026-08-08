package com.tsh.starter.befw.lib.core.messaging.rabbitmq.config;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RabbitMqSessionHandler {

	@Getter
	private RabbitMqPropertyHandler propertyHandler;

	@Getter
	private Connection connection;

	@Getter
	private Channel channel;

	public RabbitMqSessionHandler(RabbitMqPropertyHandler property) {
		this.propertyHandler = property;
	}

	public void startSession() {
		try {
			this.connection = this.propertyHandler.getConnectionFactory()
				.newConnection(this.propertyHandler.getClientName());
			this.channel = this.connection.createChannel();
			this.channel.confirmSelect(); // Publisher Confirm 활성화

		} catch (IOException e) {
			log.error("Fail to generate session.");
			// TODO custom exception required.
			throw new RuntimeException(e);

		} catch (TimeoutException e) {
			log.error("Fail to connect session.");
			// TODO custom exception required.
			throw new RuntimeException(e);
		}
	}

	public boolean isClosed() {
		return this.connection == null || !this.connection.isOpen();
	}
}
