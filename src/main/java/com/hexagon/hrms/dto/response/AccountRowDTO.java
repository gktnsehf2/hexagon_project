package com.hexagon.hrms.dto.response;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Getter;
import lombok.Setter;

/*
 계정 목록 한 줄 (S-25 계정 Role 부여 · S-26 계정 관리)
 계정 + 사원(사번·이름·부서·직급) + 보유 Role 이름을 한 번의 조회로 가져온다 (AccountMapper.selectManageList)
*/
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
	private String empStatus;      // ACTIVE / RESIGNED
	private String departmentName;
	private String jobName;
	private String roleNames;      // 사용 중인 Role 이름 (쉼표로 구분, 없으면 null)

	// ACTIVE / LOCKED / DISABLED (사용 중지가 잠김보다 우선)
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
