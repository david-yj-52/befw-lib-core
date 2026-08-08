package com.tsh.starter.befw.lib.core.apService.auth;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tsh.starter.befw.lib.core.apService.auth.dto.UpdateProfileRequest;
import com.tsh.starter.befw.lib.core.apService.auth.dto.UserResponse;
import com.tsh.starter.befw.lib.core.data.orm.usersAndPermissions.gsUser.GsUserModel;
import com.tsh.starter.befw.lib.core.data.orm.usersAndPermissions.gsUser.GsUserRepo;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final GsUserRepo userRepo;

	public UserResponse getCurrentUserProfile() {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		GsUserModel user = userRepo.findByEmail(email)
			.orElseThrow(() -> new EntityNotFoundException("User not found: " + email));

		return UserResponse.builder()
			.email(user.getEmail())
			.name(user.getUserNm())
			.avatarUrl(user.getAvatarUrl())
			.build();
	}

	@Transactional
	public UserResponse updateProfile(UpdateProfileRequest request) {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		GsUserModel user = userRepo.findByEmail(email)
			.orElseThrow(() -> new EntityNotFoundException("User not found: " + email));

		if (request.getName() != null) {
			user.setUserNm(request.getName());
		}
		if (request.getAvatarUrl() != null) {
			user.setAvatarUrl(request.getAvatarUrl());
		}

		userRepo.save(user);

		return UserResponse.builder()
			.email(user.getEmail())
			.name(user.getUserNm())
			.avatarUrl(user.getAvatarUrl())
			.build();
	}
}
