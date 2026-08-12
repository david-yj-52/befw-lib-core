package com.tsh.starter.befw.lib.core.apService.auth;

import java.time.Duration;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

	private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
	private final StringRedisTemplate redisTemplate;
	@Value("${application.security.jwt.refresh-token.expiration}")
	private long refreshTokenExpiration;

	public void saveRefreshToken(String token, String userId) {
		redisTemplate.opsForValue().set(
			REFRESH_TOKEN_PREFIX + token,
			userId,
			Duration.ofMillis(refreshTokenExpiration)
		);
	}

	public Optional<String> getUserIdByToken(String token) {
		return Optional.ofNullable(redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + token));
	}

	public void deleteRefreshToken(String token) {
		redisTemplate.delete(REFRESH_TOKEN_PREFIX + token);
	}
}
