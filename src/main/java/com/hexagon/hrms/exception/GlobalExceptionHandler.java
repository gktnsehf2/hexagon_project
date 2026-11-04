package com.hexagon.hrms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice(basePackages = "com.hexagon.hrms.controller")
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public String handle(BusinessException e, Model model) {
		model.addAttribute("errorMessage", e.getMessage());
		return "error/common-error";
	}

	// 서비스에서 조회 범위 밖의 사원 등을 막을 때 (URL 권한은 시큐리티가 먼저 막음)
	@ExceptionHandler(AccessDeniedException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public String handle(AccessDeniedException e) {
		return "error/403";
	}

	@ExceptionHandler(Exception.class)
	public String handle(Exception e, Model model) {
		log.error("처리하지 못한 예외", e);
		model.addAttribute("errorMessage", "시스템 에러가 발생했습니다. 잠시 후 다시 이용해 주세요.");
		return "error/common-error";
	}
}
