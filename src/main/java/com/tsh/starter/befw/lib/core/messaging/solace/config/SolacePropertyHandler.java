package com.tsh.starter.befw.lib.core.messaging.solace.config;

import com.solacesystems.jcsmp.JCSMPChannelProperties;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.tsh.starter.befw.lib.core.apService.util.DateTimeUtil;
import com.tsh.starter.befw.lib.core.apService.util.ServerNameUtil;
import com.tsh.starter.befw.lib.core.config.ApplicationProperties;
import com.tsh.starter.befw.lib.core.data.orm.gnMsgSrvConn.GnMsgSrvConnModel;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SolacePropertyHandler {

	public static final String SEMP_PREFIX = "SEMP_";

	public static final int DEFAULT_CH_CONNECT_TIME_OUT = 1000;
	public static final int DEFAULT_CH_CONNECT_RETRIES = 1;
	public static final int DEFAULT_CH_CONNECT_RETRIES_PER_HOST = 1;
	public static final int DEFAULT_CH_RE_CONNECT_RETRIES = 1;
	public static final int DEFAULT_CH_RE_CONNECT_RETRY_IN_MILLIS = 1000;

	public static final int DEFAULT_SEMP_PORT = 9999;

	@Getter
	String clientName;
	@Getter
	String sempClientName;

	@Getter
	String sempUrl;

	@Getter
	GnMsgSrvConnModel model;

	Object solaceModel;    // TODO 추후 별도 테이블로 정의

	@Getter
	JCSMPProperties properties;

	@Getter
	JCSMPChannelProperties channelProperties;

	public SolacePropertyHandler(GnMsgSrvConnModel model) {

		// TODO solaceModel은 Solace 전용 설정을 별도 테이블로 조회한 응답을 전달해야함. 추후 개발 현재는 DEFAULT로
		this(model, null);
	}

	public SolacePropertyHandler(GnMsgSrvConnModel model, Object solaceModel) {
		this.model = model;
		this.buildJcsmpProperties();
		this.sempUrl = "http://" + model.getHost() + ":" + DEFAULT_SEMP_PORT;

	}

	private void buildJcsmpProperties() {

		JCSMPProperties p = new JCSMPProperties();

		p.setProperty(JCSMPProperties.HOST, model.getHost() + ":" + model.getPort());
		p.setProperty(JCSMPProperties.VPN_NAME, model.getDomain());
		p.setProperty(JCSMPProperties.USERNAME, model.getConnUser());
		p.setProperty(JCSMPProperties.PASSWORD, model.getPwd());
		p.setProperty(JCSMPProperties.CLIENT_NAME, this.generateClientName());

		// TODO 추후 DB 기준 정보화
		p.setProperty(JCSMPProperties.MESSAGE_ACK_MODE, JCSMPProperties.SUPPORTED_MESSAGE_ACK_CLIENT);
		p.setProperty(JCSMPProperties.CLIENT_CHANNEL_PROPERTIES, this.buildChannelProperties());

		this.properties = p;
	}

	private JCSMPChannelProperties buildChannelProperties() {
		JCSMPChannelProperties cp = new JCSMPChannelProperties();

		if (solaceModel == null) {
			cp.setConnectTimeoutInMillis(DEFAULT_CH_CONNECT_TIME_OUT);
			cp.setConnectRetries(DEFAULT_CH_CONNECT_RETRIES);
			cp.setConnectRetriesPerHost(DEFAULT_CH_CONNECT_RETRIES_PER_HOST);
			cp.setReconnectRetries(DEFAULT_CH_RE_CONNECT_RETRIES);
			cp.setReconnectRetryWaitInMillis(DEFAULT_CH_RE_CONNECT_RETRY_IN_MILLIS);
		}
		this.channelProperties = cp;

		return cp;
	}

	private String generateClientName() {

		String format = "%s-%s-%s-%s-%s";
		String hostName = ServerNameUtil.getHostName();

		String name = String.format(format, ApplicationProperties.getApplicationTenant(),
			ApplicationProperties.getApplicationServiceName(), ApplicationProperties.getApplicationVersion(),
			hostName, DateTimeUtil.getServerSecondTime());
		log.info("clientName: {}", clientName);

		clientName = name;
		sempClientName = SEMP_PREFIX + clientName;

		return name;

	}

}
