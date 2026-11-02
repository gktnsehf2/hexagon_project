package com.hexagon.hrms.domain;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

// 계정에 Role 을 부여(GRANT)하거나 회수(REVOKE)한 이력
@Getter
@Setter
public class AccountRoleHistory {
	private int accountRoleHistoryId;
	private Account account;
	private HrRole hrRole;
	private String actionType; // GRANT / REVOKE
	private String reason;
	private Emp register;      // 처리한 사람
	private LocalDateTime createdAt;
}
