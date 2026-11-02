package com.hexagon.hrms.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AuthController {

	// 로그인 화면 (이미 로그인했으면 대시보드로)
	@GetMapping("/loginform")
	public String getLoginForm() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()
				&& !(authentication instanceof AnonymousAuthenticationToken)) {
			return "redirect:/dashboard";
		}
		return "secure/loginform";
	}

	// 권한 없는 페이지 접근 시 (SecurityConfig 의 accessDeniedPage)
	// 시큐리티가 원래 요청 방식 그대로 forward 하므로 POST(등록·퇴직 등)도 받도록 @RequestMapping
	@RequestMapping("/error/403")
	public String getAccessDenied() {
		return "error/403";
	}
}
