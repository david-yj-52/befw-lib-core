package com.tsh.starter.befw.lib.core.apService.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

	private JwtService jwtService;

	@BeforeEach
	void setUp() {
		jwtService = new JwtService();
		ReflectionTestUtils.setField(jwtService, "secretKey", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
		ReflectionTestUtils.setField(jwtService, "jwtExpiration", 900000L);
	}

	@Test
	void testGenerateAndValidateToken() {
		UserDetails userDetails = User.builder()
			.username("test@example.com")
			.password("password")
			.authorities("ROLE_USER")
			.build();

		String token = jwtService.generateAccessToken(userDetails);
		assertNotNull(token);

		String extractedEmail = jwtService.extractEmail(token);
		assertEquals("test@example.com", extractedEmail);

		assertTrue(jwtService.isTokenValid(token, userDetails));
	}
}
