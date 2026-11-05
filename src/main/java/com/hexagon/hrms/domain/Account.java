package com.hexagon.hrms.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Account implements Serializable { 
	private static final long serialVersionUID = 1L;

	private int accountId;
	private Emp emp; 
	private String loginId;
	private String password;
	private int enabled;
	private int failCount;
	private int isLocked;
	private LocalDateTime lockedAt;
	private LocalDateTime createdAt;
	private LocalDateTime lastLoginAt;


	private List<HrRole> roles;

	public String getStatusName() {
		if (enabled != 1) {
			return "사용 중지";
		}
		return isLocked == 1 ? "잠김" : "사용";
	}

	public String getLastLoginText() {
		return format(lastLoginAt);
	}

	public String getCreatedText() {
		return format(createdAt);
	}

	private static String format(LocalDateTime time) {
		return time == null ? "-" : time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
	}
}
