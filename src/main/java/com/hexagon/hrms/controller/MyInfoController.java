package com.hexagon.hrms.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.hexagon.hrms.domain.Account;
import com.hexagon.hrms.domain.Emp;
import com.hexagon.hrms.security.AccountDetails;
import com.hexagon.hrms.service.AccountService;
import com.hexagon.hrms.service.EmpService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MyInfoController {

	private final EmpService empService;

	@GetMapping("/me")
	public String getMyInfo(@AuthenticationPrincipal AccountDetails loginUser, Model model) {
		Emp emp = empService.getMe(loginUser);
		Account account = empService.getAccount(emp.getEmpId());

		model.addAttribute("emp", emp);
		model.addAttribute("tenure", empService.getTenure(emp));
		model.addAttribute("annualLeave", empService.getThisYearLeave(emp.getEmpId()));
		model.addAttribute("account", account);
		model.addAttribute("permissions", empService.getPermissions(account)); // 모든 Role 의 권한 합계
		model.addAttribute("maxFailCount", AccountService.MAX_FAIL_COUNT);
		return "emp/me";
	}
}
