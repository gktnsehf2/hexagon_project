package com.hexagon.hrms.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AuthController {

	// 로그인 화면
	@GetMapping("/loginform")
	public String getLoginForm() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()
				&& !(authentication instanceof AnonymousAuthenticationToken)) {
			return "redirect:/dashboard";
		}
		return "secure/loginform";
	}

	@RequestMapping("/error/403")
	public String getAccessDenied() {
		return "error/403";
	}
}
