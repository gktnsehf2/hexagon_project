package com.hexagon.hrms.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

// 캘린더에 올릴 승인된 휴가 한 건 (사원 이름·부서까지)
@Getter
@Setter
public class CalendarLeaveDTO {
	private int leaveRequestId;
	private int empId;
	private String ename;
	private String departmentName;
	private String leaveTypeCode; 
	private String leaveTypeName;
	private LocalDate startDate;
	private LocalDate endDate;
	private BigDecimal days;

	public String getShortTypeName() {
		if ("HALF_AM".equals(leaveTypeCode)) {
			return "반차-오전";
		}
		if ("HALF_PM".equals(leaveTypeCode)) {
			return "반차-오후";
		}
		return leaveTypeName;
	}
}
