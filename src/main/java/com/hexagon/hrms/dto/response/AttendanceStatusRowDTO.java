package com.hexagon.hrms.dto.response;

import com.hexagon.hrms.domain.Emp;

import lombok.Getter;
import lombok.Setter;

// 근태 현황(S-09) 사원 한 줄 (한 달 집계)
@Getter
@Setter
public class AttendanceStatusRowDTO {
	private Emp emp;
	private int workDays;           // 출근한 날
	private int normalCount;
	private int lateCount;
	private int earlyLeaveCount;
	private int absentCount;
	private int leaveDays;          // 하루 휴가로 쉰 날
	private int totalWorkMinutes;
	private int totalOvertimeMinutes;

	public String getTotalWorkText() {
		return MyAttendanceMonthDTO.toHourText(totalWorkMinutes);
	}

	public String getOvertimeText() {
		return totalOvertimeMinutes == 0 ? "-" : MyAttendanceMonthDTO.toHourText(totalOvertimeMinutes);
	}

	// 지각·조퇴·결근이 하나라도 있으면 목록에서 강조
	public boolean isIssue() {
		return lateCount + earlyLeaveCount + absentCount > 0;
	}
}
