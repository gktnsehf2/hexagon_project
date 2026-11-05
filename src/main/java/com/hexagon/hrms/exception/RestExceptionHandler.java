package com.hexagon.hrms.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.hexagon.hrms.common.web.ApiResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice(basePackages = "com.hexagon.hrms.rest.controller")
public class RestExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse<Void>> handle(BusinessException e) {
		return fail(e.getErrorCode(), e.getMessage());
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiResponse<Void>> handle(AccessDeniedException e) {
		return fail(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.getDefaultMessage());
	}

	@ExceptionHandler({ MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class,
		HttpMessageNotReadableException.class, BindException.class })
	public ResponseEntity<ApiResponse<Void>> handleInvalid(Exception e) {
		return fail(ErrorCode.INVALID_INPUT, ErrorCode.INVALID_INPUT.getDefaultMessage());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handle(Exception e) {
		log.error("처리하지 못한 예외 (API)", e);
		return fail(ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.getDefaultMessage());
	}

	private ResponseEntity<ApiResponse<Void>> fail(ErrorCode code, String message) {
		return ResponseEntity.status(code.getStatus()).body(ApiResponse.fail(code, message));
	}
}
