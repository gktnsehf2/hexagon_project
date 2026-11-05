package com.hexagon.hrms.rest.controller;

import java.time.LocalDate;

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
import com.hexagon.hrms.common.web.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class RestEmpController {

	private final EmpService empService;

	@PostMapping("/{empId}/resign")
	public ApiResponse<Void> resign(@PathVariable("empId") int empId,
									  @RequestParam(value = "resignDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate resignDate,
									  @AuthenticationPrincipal AccountDetails loginUser) {
		Emp emp = empService.resign(empId, resignDate, loginUser);
		return ApiResponse.success(emp.getEname() + " 사원을 " + resignDate + " 자로 퇴직 처리했습니다.");
	}
}
