package com.tsh.starter.befw.lib.core.interfaces.rest.auth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.tsh.starter.befw.lib.core.apService.auth.UserService;
import com.tsh.starter.befw.lib.core.apService.auth.dto.UpdateProfileRequest;
import com.tsh.starter.befw.lib.core.apService.auth.dto.UserResponse;
import com.tsh.starter.befw.lib.core.interfaces.rest.ApiResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class BaseUserController {

	protected final UserService userService;

	@GetMapping("/me")
	public ApiResponse<UserResponse> getCurrentUser() {
		UserResponse response = userService.getCurrentUserProfile();
		postGetCurrentUser(response);
		return ApiResponse.ok(response);
	}

	@PutMapping("/me")
	public ApiResponse<UserResponse> updateProfile(@RequestBody UpdateProfileRequest request) {
		preUpdateProfile(request);
		UserResponse response = userService.updateProfile(request);
		postUpdateProfile(request, response);
		return ApiResponse.ok(response);
	}

	protected void postGetCurrentUser(UserResponse response) {
	}

	protected void preUpdateProfile(UpdateProfileRequest request) {
	}

	protected void postUpdateProfile(UpdateProfileRequest request, UserResponse response) {
	}
}
