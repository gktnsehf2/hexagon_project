package com.hexagon.hrms.exception;

import org.springframework.http.HttpStatus;

/*
 오류 코드 표준 (API 응답의 code, 화면 에러 페이지의 상태 코드)
 - 서비스는 업무 규칙 위반을 BusinessException(ErrorCode, 메시지) 로 던진다
 - RestExceptionHandler / GlobalExceptionHandler 가 코드의 HTTP 상태와 메시지로 바꿔 응답한다
 - 메시지를 따로 주지 않으면 여기 기본 메시지를 쓴다
*/
public enum ErrorCode {

	INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값을 확인해 주세요."),
	BUSINESS_RULE(HttpStatus.BAD_REQUEST, "요청을 처리할 수 없습니다."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "처리할 권한이 없는 대상입니다."),
	NOT_FOUND(HttpStatus.NOT_FOUND, "대상을 찾을 수 없습니다."),
	CONFLICT(HttpStatus.CONFLICT, "방금 다른 처리가 되었습니다. 새로고침 후 다시 확인해 주세요."),
	INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "시스템 에러가 발생했습니다. 잠시 후 다시 이용해 주세요.");

	private final HttpStatus status;
	private final String defaultMessage;

	ErrorCode(HttpStatus status, String defaultMessage) {
		this.status = status;
		this.defaultMessage = defaultMessage;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public String getDefaultMessage() {
		return defaultMessage;
	}
}
