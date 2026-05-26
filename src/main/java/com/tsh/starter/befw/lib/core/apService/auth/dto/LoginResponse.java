package com.tsh.starter.befw.lib.core.apService.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
	private String accessToken;
	private String refreshToken;
	private String tokenType;
	private long expiresIn;
}
