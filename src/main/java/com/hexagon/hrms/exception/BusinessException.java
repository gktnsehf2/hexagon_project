package com.hexagon.hrms.exception;

public class BusinessException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private final ErrorCode errorCode;

	public BusinessException(String message) {
		this(ErrorCode.BUSINESS_RULE, message);
	}

	public BusinessException(ErrorCode errorCode) {
		this(errorCode, errorCode.getDefaultMessage());
	}

	public BusinessException(ErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}
}
