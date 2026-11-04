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

/*
 계정 관리 (S-26) fetch API  — 권한 그룹 부여·회수(S-25)는 팀원의 RestAccountRoleController
   POST /api/accounts/{id}/unlock           F-25 잠금 해제                            ACCOUNT_UNLOCK
   POST /api/accounts/{id}/reset-password   F-25 비밀번호 초기화 (P-07) → tempPassword  ACCOUNT_PASSWORD_RESET
   POST /api/accounts/{id}/enable           F-25 사용/중지 {enabled: 1 | 0}           ACCOUNT_ENABLE
 실패: 400 {result: "fail", message} ← RestExceptionHandler
*/
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
