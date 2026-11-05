package com.hexagon.hrms.rest.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hexagon.hrms.security.AccountDetails;
import com.hexagon.hrms.service.AccountManageService;
import com.hexagon.hrms.common.web.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class RestAccountManageController {

	private final AccountManageService accountManageService;

	@PostMapping("/{id}/unlock")
	public ApiResponse<Void> unlock(@PathVariable("id") int id) {
		return ApiResponse.success(accountManageService.unlock(id));
	}

	@PostMapping("/{id}/reset-password")
	public ApiResponse<Map<String, Object>> resetPassword(@PathVariable("id") int id,
											 @AuthenticationPrincipal AccountDetails loginUser) {
		Map<String, Object> data = new HashMap<>();
		data.put("tempPassword", accountManageService.resetPassword(loginUser, id));
		return ApiResponse.success("비밀번호를 초기화했습니다. 임시 비밀번호를 본인에게 전달하세요.", data);
	}

	@PostMapping("/{id}/enable")
	public ApiResponse<Void> enable(@PathVariable("id") int id, @RequestBody Map<String, Integer> body,
									  @AuthenticationPrincipal AccountDetails loginUser) {
		Integer enabled = body.get("enabled");
		return ApiResponse.success(accountManageService.setEnabled(loginUser, id, enabled == null ? -1 : enabled));
	}
}
