package com.tsh.starter.befw.lib.core.apService.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.tsh.starter.befw.lib.core.apService.auth.dto.LoginRequest;
import com.tsh.starter.befw.lib.core.apService.auth.dto.LoginResponse;
import com.tsh.starter.befw.lib.core.apService.auth.dto.RegisterRequest;
import com.tsh.starter.befw.lib.core.data.orm.usersAndPermissions.gsUser.GsUserModel;
import com.tsh.starter.befw.lib.core.data.orm.usersAndPermissions.gsUser.GsUserRepo;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private GsUserRepo userRepo;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private JwtService jwtService;
	@Mock
	private RefreshTokenService refreshTokenService;
	@Mock
	private AuthenticationManager authenticationManager;
	@Mock
	private UserDetailsService userDetailsService;

	@InjectMocks
	private AuthService authService;

	@Test
	void testRegister_Success() {
		RegisterRequest request = RegisterRequest.builder()
			.email("test@example.com")
			.name("Test User")
			.password("password")
			.build();

		when(userRepo.findByEmail(any())).thenReturn(Optional.empty());
		when(passwordEncoder.encode(any())).thenReturn("encodedPassword");

		authService.register(request);

		verify(userRepo).save(any(GsUserModel.class));
	}

	@Test
	void testRegister_AlreadyExists() {
		RegisterRequest request = RegisterRequest.builder()
			.email("test@example.com")
			.build();

		when(userRepo.findByEmail(any())).thenReturn(Optional.of(new GsUserModel()));

		assertThrows(IllegalArgumentException.class, () -> authService.register(request));
	}

	@Test
	void testLogin_Success() {
		LoginRequest request = LoginRequest.builder()
			.email("test@example.com")
			.password("password")
			.build();

		when(userDetailsService.loadUserByUsername(any())).thenReturn(
			User.builder().username("test@example.com").password("pwd").authorities("ROLE_USER").build()
		);
		when(userRepo.findByEmail(any())).thenReturn(
			Optional.of(GsUserModel.builder().objId("user123").email("test@example.com").build()));
		when(jwtService.generateAccessToken(any())).thenReturn("accessToken");
		when(jwtService.generateRefreshToken()).thenReturn("refreshToken");

		LoginResponse response = authService.login(request);

		assertNotNull(response);
		assertNotNull(response.getAccessToken());
		verify(refreshTokenService).saveRefreshToken(any(), any());
	}
}
