package com.hexagon.hrms.security;

public final class PermissionCode {

	private PermissionCode() {
	}

	// 보안
	public static final String ACCOUNT_UNLOCK = "PERMISSION_ACCOUNT_UNLOCK";
	public static final String ACCOUNT_PASSWORD_RESET = "PERMISSION_ACCOUNT_PASSWORD_RESET";
	public static final String ACCOUNT_ENABLE = "PERMISSION_ACCOUNT_ENABLE";

	// 인사
	public static final String MY_INFO_READ = "PERMISSION_MY_INFO_READ";
	public static final String EMP_READ_TEAM = "PERMISSION_EMP_READ_TEAM";
	public static final String EMP_READ_DEPT = "PERMISSION_EMP_READ_DEPT";
	public static final String EMP_READ_ALL = "PERMISSION_EMP_READ_ALL";
	public static final String EMP_CREATE = "PERMISSION_EMP_CREATE";
	public static final String EMP_UPDATE = "PERMISSION_EMP_UPDATE";
	public static final String EMP_RESIGN = "PERMISSION_EMP_RESIGN";
	public static final String DEPT_CREATE = "PERMISSION_DEPT_CREATE";
	public static final String DEPT_UPDATE = "PERMISSION_DEPT_UPDATE";
	public static final String DEPT_DELETE = "PERMISSION_DEPT_DELETE";

	// 근태
	public static final String ATT_CHECK = "PERMISSION_ATT_CHECK";
	public static final String MY_ATT_READ = "PERMISSION_MY_ATT_READ";
	public static final String ATT_READ_TEAM = "PERMISSION_ATT_READ_TEAM";
	public static final String ATT_READ_DEPT = "PERMISSION_ATT_READ_DEPT";
	public static final String ATT_READ_ALL = "PERMISSION_ATT_READ_ALL";
	public static final String ATT_CORRECT_REQUEST = "PERMISSION_ATT_CORRECT_REQUEST";
	public static final String ATT_CORRECT_APPROVE = "PERMISSION_ATT_CORRECT_APPROVE";

	// 휴가 (결재는 1단계)
	public static final String LEAVE_REQUEST = "PERMISSION_LEAVE_REQUEST";
	public static final String LEAVE_CANCEL = "PERMISSION_LEAVE_CANCEL";
	public static final String MY_LEAVE_READ = "PERMISSION_MY_LEAVE_READ";
	public static final String LEAVE_READ_ALL = "PERMISSION_LEAVE_READ_ALL";
	public static final String LEAVE_APPROVE = "PERMISSION_LEAVE_APPROVE";

	// 권한 관리
	public static final String PERM_CREATE = "PERMISSION_PERM_CREATE";
	public static final String PERM_UPDATE = "PERMISSION_PERM_UPDATE";
	public static final String PERM_DELETE = "PERMISSION_PERM_DELETE";
	public static final String ROLE_CREATE = "PERMISSION_ROLE_CREATE";
	public static final String ROLE_UPDATE = "PERMISSION_ROLE_UPDATE";
	public static final String ROLE_DELETE = "PERMISSION_ROLE_DELETE";
	public static final String ROLE_ASSIGN = "PERMISSION_ROLE_ASSIGN";
}
