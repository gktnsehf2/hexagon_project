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

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class RestAccountManageController {

	private final AccountManageService accountManageService;

	@PostMapping("/{id}/unlock")
	public Map<String, Object> unlock(@PathVariable("id") int id) {
		return success(accountManageService.unlock(id));
	}

	@PostMapping("/{id}/reset-password")
	public Map<String, Object> resetPassword(@PathVariable("id") int id,
											 @AuthenticationPrincipal AccountDetails loginUser) {
		Map<String, Object> map = success("비밀번호를 초기화했습니다. 임시 비밀번호를 본인에게 전달하세요.");
		map.put("tempPassword", accountManageService.resetPassword(loginUser, id));
		return map;
	}

	@PostMapping("/{id}/enable")
	public Map<String, Object> enable(@PathVariable("id") int id, @RequestBody Map<String, Integer> body,
									  @AuthenticationPrincipal AccountDetails loginUser) {
		Integer enabled = body.get("enabled");
		return success(accountManageService.setEnabled(loginUser, id, enabled == null ? -1 : enabled));
	}

	private Map<String, Object> success(String message) {
		Map<String, Object> map = new HashMap<>();
		map.put("result", "success");
		map.put("message", message);
		return map;
	}
}
