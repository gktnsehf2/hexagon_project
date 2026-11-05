package com.hexagon.hrms.domain;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountRoleHistory {
	private int accountRoleHistoryId;
	private Account account;
	private HrRole hrRole;
	private String actionType; 
	private String reason;
	private Emp register;      
	private LocalDateTime createdAt;
}
