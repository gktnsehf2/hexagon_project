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
	private String leaveTypeCode; // ANNUAL / HALF_AM / HALF_PM / SICK / FAMILY_EVENT / OFFICIAL
	private String leaveTypeName;
	private LocalDate startDate;
	private LocalDate endDate;
	private BigDecimal days;

	// 캘린더 제목에 쓰는 짧은 유형명: 오전 반차 → 반차-오전
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
