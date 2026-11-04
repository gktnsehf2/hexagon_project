package com.hexagon.hrms.dto.response;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

// 근태 현황(S-09) 조회 결과: 월 정보 + 사원별 집계 + 합계 카드
@Getter
@Setter
public class AttendanceStatusDTO {
	private YearMonth month;
	private YearMonth prevMonth; // 조회할 수 없으면 null
	private YearMonth nextMonth;
	private YearMonth minMonth;
	private YearMonth maxMonth;
	private List<AttendanceStatusRowDTO> rows = new ArrayList<>();

	public String getMonthText() {
		return month == null ? "" : month.getYear() + "년 " + month.getMonthValue() + "월";
	}

	public int getEmpCount() {
		return rows.size();
	}

	public int getLateTotal() {
		return rows.stream().mapToInt(AttendanceStatusRowDTO::getLateCount).sum();
	}

	public int getEarlyLeaveTotal() {
		return rows.stream().mapToInt(AttendanceStatusRowDTO::getEarlyLeaveCount).sum();
	}

	public int getAbsentTotal() {
		return rows.stream().mapToInt(AttendanceStatusRowDTO::getAbsentCount).sum();
	}

	public String getOvertimeTotalText() {
		return MyAttendanceMonthDTO.toHourText(rows.stream().mapToInt(AttendanceStatusRowDTO::getTotalOvertimeMinutes).sum());
	}

	// 지각·조퇴·결근이 있는 사원 수
	public long getIssueEmpCount() {
		return rows.stream().filter(AttendanceStatusRowDTO::isIssue).count();
	}
}
