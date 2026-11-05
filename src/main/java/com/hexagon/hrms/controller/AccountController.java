package com.hexagon.hrms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hexagon.hrms.service.AccountManageService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AccountController {

	private final AccountManageService accountManageService;

	@GetMapping("/accounts")
	public String getList(@RequestParam(value = "status", required = false) String status,
						  @RequestParam(value = "keyword", required = false) String keyword,
						  @RequestParam(value = "page", defaultValue = "1") int page, Model model) {
		model.addAttribute("pager", accountManageService.getList(status, keyword, page));
		model.addAttribute("counts", accountManageService.getStatusCounts());
		model.addAttribute("status", AccountManageService.normalizeStatus(status));
		model.addAttribute("keyword", keyword == null ? "" : keyword.trim());
		return "account/list";
	}
}
