package com.hexagon.hrms.common.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hexagon.hrms.exception.ErrorCode;

import lombok.Getter;

/*
 REST API 공통 응답
 - 성공: { "result":"success", "code":"OK", "message":"...", "data":{...} }
 - 실패: { "result":"fail", "code":"NOT_FOUND", "message":"..." }  (RestExceptionHandler 가 만든다)
 - 값이 null 인 항목은 JSON 에서 빠진다
*/
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

	private final String result;
	private final String code;
	private final String message;
	private final T data;

	private ApiResponse(String result, String code, String message, T data) {
		this.result = result;
		this.code = code;
		this.message = message;
		this.data = data;
	}

	public static <T> ApiResponse<T> success(String message) {
		return new ApiResponse<>("success", "OK", message, null);
	}

	public static <T> ApiResponse<T> success(String message, T data) {
		return new ApiResponse<>("success", "OK", message, data);
	}

	public static ApiResponse<Void> fail(ErrorCode errorCode, String message) {
		return new ApiResponse<>("fail", errorCode.name(), message, null);
	}
}
