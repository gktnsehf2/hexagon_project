package com.hexagon.hrms.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.hexagon.hrms.security.AccountDetails;
import com.hexagon.hrms.service.DashboardService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MainController {

	private final DashboardService dashboardService;

	@GetMapping("/")
	public String getRoot() {
		return "redirect:/dashboard";
	}

	// @AuthenticationPrincipal: 로그인한 사용자(AccountDetails)를 바로 받기
	@GetMapping("/dashboard")
	public String getDashboard(@AuthenticationPrincipal AccountDetails loginUser, Model model) {
		model.addAttribute("loginEmp", loginUser.getAccount().getEmp());
		model.addAttribute("dash", dashboardService.getDashboard(loginUser));
		return "dashboard";
	}
}
