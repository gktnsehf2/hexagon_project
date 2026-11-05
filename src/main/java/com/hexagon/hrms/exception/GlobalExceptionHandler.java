package com.hexagon.hrms.exception;

import javax.servlet.http.HttpServletResponse;

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
	public String handle(BusinessException e, Model model, HttpServletResponse response) {
		response.setStatus(e.getErrorCode().getStatus().value());
		model.addAttribute("errorCode", e.getErrorCode().name());
		model.addAttribute("errorMessage", e.getMessage());
		return "error/common-error";
	}

	@ExceptionHandler(AccessDeniedException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public String handle(AccessDeniedException e) {
		return "error/403";
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public String handle(Exception e, Model model) {
		log.error("처리하지 못한 예외", e);
		model.addAttribute("errorCode", ErrorCode.INTERNAL_ERROR.name());
		model.addAttribute("errorMessage", ErrorCode.INTERNAL_ERROR.getDefaultMessage());
		return "error/common-error";
	}
}
