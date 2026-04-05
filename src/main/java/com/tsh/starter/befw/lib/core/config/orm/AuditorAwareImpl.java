package com.tsh.starter.befw.lib.core.config.orm;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<String> {

	// TODO user 정보를 어떻게 넣을지 고민 필요
	@Override
	public Optional<String> getCurrentAuditor() {
		// 현재 로그인한 사용자 정보를 반환
		// SecurityContextHolder를 사용하면 Spring Security와 연동 가능
		return Optional.of("system");        // 임시로 고정값
		// return Optional.of(SecurityContextHolder.getContext().getAuthentication().getName());
	}
}