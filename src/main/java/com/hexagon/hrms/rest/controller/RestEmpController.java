package com.hexagon.hrms.rest.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hexagon.hrms.domain.Emp;
import com.hexagon.hrms.security.AccountDetails;
import com.hexagon.hrms.service.EmpService;

import lombok.RequiredArgsConstructor;

/*
 F-06 퇴직 처리 (사원 상세 화면의 퇴직 처리 팝업에서 fetch 로 호출)
 요청: POST /api/employees/{empId}/resign   resignDate=2026-11-30 (폼 형식)
 성공: 200 {result: "success", message}
 실패: 400 {result: "fail", message}  ← RestExceptionHandler
 권한(PERMISSION_EMP_RESIGN)은 SecurityConfig 에서 검사
*/
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class RestEmpController {

	private final EmpService empService;

	@PostMapping("/{empId}/resign")
	public Map<String, Object> resign(@PathVariable("empId") int empId,
									  @RequestParam(value = "resignDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate resignDate,
									  @AuthenticationPrincipal AccountDetails loginUser) {
		Emp emp = empService.resign(empId, resignDate, loginUser);

		Map<String, Object> map = new HashMap<>();
		map.put("result", "success");
		map.put("message", emp.getEname() + " 사원을 " + resignDate + " 자로 퇴직 처리했습니다.");
		return map;
	}
}
