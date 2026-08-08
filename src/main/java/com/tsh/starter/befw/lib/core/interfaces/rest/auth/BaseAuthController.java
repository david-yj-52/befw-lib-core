package com.tsh.starter.befw.lib.core.interfaces.rest.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.tsh.starter.befw.lib.core.apService.auth.AuthService;
import com.tsh.starter.befw.lib.core.apService.auth.dto.LoginRequest;
import com.tsh.starter.befw.lib.core.apService.auth.dto.LoginResponse;
import com.tsh.starter.befw.lib.core.apService.auth.dto.RegisterRequest;
import com.tsh.starter.befw.lib.core.interfaces.rest.ApiResponse;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class BaseAuthController {

	protected final AuthService authService;

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<Void> register(@RequestBody RegisterRequest request) {
		preRegister(request);
		authService.register(request);
		postRegister(request);
		return ApiResponse.noContent();
	}

	@PostMapping("/login")
	public ApiResponse<LoginResponse> login(
		@RequestBody LoginRequest request,
		HttpServletResponse response
	) {
		preLogin(request);
		LoginResponse loginResponse = authService.login(request);
		setRefreshTokenCookie(response, loginResponse.getRefreshToken());

		// Remove refreshToken from body as it's in cookie
		loginResponse.setRefreshToken(null);

		postLogin(request, loginResponse);
		return ApiResponse.ok(loginResponse);
	}

	@PostMapping("/refresh")
	public ApiResponse<LoginResponse> refresh(
		HttpServletRequest request,
		HttpServletResponse response
	) {
		String refreshToken = getRefreshTokenFromCookie(request);
		LoginResponse loginResponse = authService.refresh(refreshToken);
		setRefreshTokenCookie(response, loginResponse.getRefreshToken());

		loginResponse.setRefreshToken(null);

		return ApiResponse.ok(loginResponse);
	}

	@PostMapping("/logout")
	public ApiResponse<Void> logout(
		HttpServletRequest request,
		HttpServletResponse response
	) {
		String refreshToken = getRefreshTokenFromCookie(request);
		authService.logout(refreshToken);
		clearRefreshTokenCookie(response);
		return ApiResponse.noContent();
	}

	protected void preRegister(RegisterRequest request) {
	}

	protected void postRegister(RegisterRequest request) {
	}

	protected void preLogin(LoginRequest request) {
	}

	protected void postLogin(LoginRequest request, LoginResponse response) {
	}

	protected void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
		Cookie cookie = new Cookie("refreshToken", refreshToken);
		cookie.setHttpOnly(true);
		cookie.setSecure(false); // Set to true in production
		cookie.setPath("/");
		cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
		response.addCookie(cookie);
	}

	protected void clearRefreshTokenCookie(HttpServletResponse response) {
		Cookie cookie = new Cookie("refreshToken", null);
		cookie.setHttpOnly(true);
		cookie.setSecure(false);
		cookie.setPath("/");
		cookie.setMaxAge(0);
		response.addCookie(cookie);
	}

	protected String getRefreshTokenFromCookie(HttpServletRequest request) {
		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if ("refreshToken".equals(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}
		throw new IllegalArgumentException("Refresh token not found");
	}
}
