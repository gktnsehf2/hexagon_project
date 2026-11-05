package com.hexagon.hrms.dto.response;

import com.hexagon.hrms.domain.Emp;

import lombok.Getter;
import lombok.Setter;

// 근태 현황
@Getter
@Setter
public class AttendanceStatusRowDTO {
	private Emp emp;
	private int workDays;       
	private int normalCount;
	private int lateCount;
	private int earlyLeaveCount;
	private int absentCount;
	private int leaveDays;        
	private int totalWorkMinutes;
	private int totalOvertimeMinutes;

	public String getTotalWorkText() {
		return MyAttendanceMonthDTO.toHourText(totalWorkMinutes);
	}

	public String getOvertimeText() {
		return totalOvertimeMinutes == 0 ? "-" : MyAttendanceMonthDTO.toHourText(totalOvertimeMinutes);
	}

	public boolean isIssue() {
		return lateCount + earlyLeaveCount + absentCount > 0;
	}
}
