package com.tsh.starter.befw.lib.core.messaging.solace;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SolaceSessionManager {

	private ConcurrentHashMap<String, String> sessionMap;

	@PostConstruct
	public void init(){
		sessionMap = new ConcurrentHashMap<>();
	}





}
