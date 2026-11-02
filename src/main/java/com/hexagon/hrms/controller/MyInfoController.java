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

/*
 F-05 내 정보 (S-19)
   GET /me    본인 인사 정보 · 계정 정보 · 보유 Role 과 권한     PERMISSION_MY_INFO_READ
 수정 기능은 없음 (인사 정보 변경은 인사팀, 비밀번호 초기화는 계정 관리)
 세션의 로그인 정보는 로그인 시점 값이라, 화면에는 DB 에서 다시 읽은 최신 값을 보여준다
*/
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
