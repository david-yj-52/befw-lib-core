package com.tsh.starter.befw.lib.core.apService.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tsh.starter.befw.lib.core.apService.auth.dto.LoginRequest;
import com.tsh.starter.befw.lib.core.apService.auth.dto.LoginResponse;
import com.tsh.starter.befw.lib.core.apService.auth.dto.RegisterRequest;
import com.tsh.starter.befw.lib.core.config.ApplicationProperties;
import com.tsh.starter.befw.lib.core.data.constant.UseStatCd;
import com.tsh.starter.befw.lib.core.data.orm.usersAndPermissions.gsUser.GsUserModel;
import com.tsh.starter.befw.lib.core.data.orm.usersAndPermissions.gsUser.GsUserRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final GsUserRepo userRepo;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final RefreshTokenService refreshTokenService;
	private final AuthenticationManager authenticationManager;
	private final UserDetailsService userDetailsService;

	@Value("${application.security.jwt.expiration}")
	private long jwtExpiration;

	@Transactional
	public void register(RegisterRequest request) {
		if (userRepo.findByEmail(request.getEmail()).isPresent()) {
			throw new IllegalArgumentException("Email already exists: " + request.getEmail());
		}

		GsUserModel user = GsUserModel.builder()
			.email(request.getEmail())
			.userNm(request.getName())
			.pwdHash(passwordEncoder.encode(request.getPassword()))
			.srvId(ApplicationProperties.getApplicationServiceName())
			.tenant(ApplicationProperties.getApplicationTenant())
			.traceId("INTERNAL-REG") // Placeholder
			.useStatCd(UseStatCd.Usable)
			.evtNm("Register")
			.prevEvntNm("None")
			.build();

		userRepo.save(user);
	}

	public LoginResponse login(LoginRequest request) {
		authenticationManager.authenticate(
			new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
		);

		UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
		GsUserModel user = userRepo.findByEmail(request.getEmail()).orElseThrow();

		String accessToken = jwtService.generateAccessToken(userDetails);
		String refreshToken = jwtService.generateRefreshToken();

		refreshTokenService.saveRefreshToken(refreshToken, user.getObjId());

		return LoginResponse.builder()
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.tokenType("Bearer")
			.expiresIn(jwtExpiration / 1000)
			.build();
	}

	public LoginResponse refresh(String refreshToken) {
		String userId = refreshTokenService.getUserIdByToken(refreshToken)
			.orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

		GsUserModel user = userRepo.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("User not found"));

		// Rotation: Delete old, create new
		refreshTokenService.deleteRefreshToken(refreshToken);
		String newRefreshToken = jwtService.generateRefreshToken();
		refreshTokenService.saveRefreshToken(newRefreshToken, user.getObjId());

		UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
		String accessToken = jwtService.generateAccessToken(userDetails);

		return LoginResponse.builder()
			.accessToken(accessToken)
			.refreshToken(newRefreshToken)
			.tokenType("Bearer")
			.expiresIn(jwtExpiration / 1000)
			.build();
	}

	public void logout(String refreshToken) {
		refreshTokenService.deleteRefreshToken(refreshToken);
	}
}
