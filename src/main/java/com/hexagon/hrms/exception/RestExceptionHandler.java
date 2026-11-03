package com.hexagon.hrms.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice(basePackages = "com.hexagon.hrms.rest.controller")
public class RestExceptionHandler {

	// 업무 규칙 위반 (이미 출근함 등) → 400 + 안내 문구
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<Map<String, Object>> handle(BusinessException e) {
		return fail(HttpStatus.BAD_REQUEST, e.getMessage());
	}

	// 조회 범위 밖의 사원 등 → 403
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<Map<String, Object>> handle(AccessDeniedException e) {
		return fail(HttpStatus.FORBIDDEN, "처리할 권한이 없는 대상입니다.");
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handle(Exception e) {
		log.error("처리하지 못한 예외 (API)", e);
		return fail(HttpStatus.INTERNAL_SERVER_ERROR, "시스템 에러가 발생했습니다. 잠시 후 다시 이용해 주세요.");
	}

	private ResponseEntity<Map<String, Object>> fail(HttpStatus status, String message) {
		Map<String, Object> body = new HashMap<>();
		body.put("result", "fail");
		body.put("message", message);
		return ResponseEntity.status(status).body(body);
	}
}
