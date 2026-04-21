package com.tsh.starter.befw.lib.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Getter
@Component
public class MessagingProperties {

	@Getter
	@Value("${application.messaging.enable.solace.enable}")
	public String solaceEnable;

	@Getter
	@Value("${application.messaging.enable.solace.pub}")
	public String solacePubEnable;

	@Getter
	@Value("${application.messaging.enable.solace.sub}")
	public String solaceSubEnable;

	@Getter
	@Value("${application.messaging.enable.kafka.enable}")
	public String kafkaEnable;

	@Getter
	@Value("${application.messaging.enable.kafka.pub}")
	public String kafkaPubEnable;

	@Getter
	@Value("${application.messaging.enable.kafka.sub}")
	public String kafkaSubEnable;

}
