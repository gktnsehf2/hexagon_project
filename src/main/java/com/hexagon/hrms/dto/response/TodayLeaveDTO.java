package com.hexagon.hrms.dto.response;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

// 대시보드 "오늘 휴가자" 한 줄
@Getter
@Setter
public class TodayLeaveDTO {
	private int empId;
	private String ename;
	private String departmentName;
	private String leaveTypeCode;
	private String leaveTypeName;
	private LocalDate startDate;
	private LocalDate endDate;
}
