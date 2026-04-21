package com.tsh.starter.befw.lib.core.messaging.solace.config;

import com.solacesystems.jcsmp.InvalidPropertiesException;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPSession;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SolaceSessionHandler {

	@Getter
	private SolacePropertyHandler propertyHandler;

	@Getter
	private JCSMPSession session;

	public SolaceSessionHandler(SolacePropertyHandler property) {
		this.propertyHandler = property;
	}

	public void startSession() {
		try {
			this.session = JCSMPFactory.onlyInstance().createSession(this.propertyHandler.properties);
			this.session.connect();

		} catch (InvalidPropertiesException e) {
			log.error("fail to generate session.");
			// TODO custom exception required.
			throw new RuntimeException(e);

		} catch (JCSMPException e) {
			log.error("Fail to connect session.");
			// TODO custom exception required.
			throw new RuntimeException(e);
		}
	}
}
