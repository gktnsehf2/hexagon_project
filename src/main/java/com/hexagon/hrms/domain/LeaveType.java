package com.hexagon.hrms.domain;

import lombok.Getter;
import lombok.Setter;

// 휴가 유형 
@Getter
@Setter
public class LeaveType {
	private int leaveTypeId;
	private String leaveTypeCode; 
	private String leaveTypeName;
	private int isDeduct;
	private int isActive;

	
}
