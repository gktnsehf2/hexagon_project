package com.hexagon.hrms.dto.response;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AccountRowDTO {
	private int accountId;
	private String loginId;
	private int enabled;
	private int failCount;
	private int isLocked;
	private LocalDateTime lockedAt;
	private LocalDateTime lastLoginAt;

	private int empId;
	private String empNo;
	private String ename;
	private String empStatus;  
	private String departmentName;
	private String jobName;
	private String roleNames;      

	public String getStatus() {
		if (enabled != 1) {
			return "DISABLED";
		}
		return isLocked == 1 ? "LOCKED" : "ACTIVE";
	}

	public String getStatusName() {
		switch (getStatus()) {
		case "DISABLED":
			return "사용 중지";
		case "LOCKED":
			return "잠김";
		default:
			return "사용";
		}
	}

	public boolean isResigned() {
		return "RESIGNED".equals(empStatus);
	}

	public String getLastLoginText() {
		return format(lastLoginAt);
	}

	public String getLockedText() {
		return format(lockedAt);
	}

	private static String format(LocalDateTime time) {
		return time == null ? "-" : time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
	}
}
