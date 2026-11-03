package com.hexagon.hrms.dto.response;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

// 내 근태 월 조회 결과 (요약 카드 + 일별 기록)
@Getter
@Setter
public class MyAttendanceMonthDTO {
	private YearMonth month;
	private YearMonth prevMonth; // 조회할 수 없으면 null
	private YearMonth nextMonth;
	private YearMonth minMonth;  // 조회 가능한 첫 달 (입사월·근태 시스템 도입월 중 늦은 달)
	private YearMonth maxMonth;  // 이번 달
	private List<MyAttendanceDayDTO> days = new ArrayList<>(); // 날짜 순

	private int workDays;        // 출근한 날
	private int normalCount;
	private int lateCount;
	private int earlyLeaveCount;
	private int absentCount;
	private int leaveDays;       // 하루 휴가로 쉰 날 (반차 제외)
	private int totalWorkMinutes;
	private int totalOvertimeMinutes;

	public String getMonthText() {
		return month == null ? "" : month.getYear() + "년 " + month.getMonthValue() + "월";
	}

	public String getTotalWorkText() {
		return toHourText(totalWorkMinutes);
	}

	public String getTotalOvertimeText() {
		return toHourText(totalOvertimeMinutes);
	}

	// 화면은 최근 날짜가 위로
	public List<MyAttendanceDayDTO> getDaysDesc() {
		List<MyAttendanceDayDTO> list = new ArrayList<>(days);
		Collections.reverse(list);
		return list;
	}

	// 510 → "8시간 30분", 480 → "8시간", 0 → "0시간"
	public static String toHourText(int minutes) {
		int h = minutes / 60;
		int m = minutes % 60;
		return m == 0 ? h + "시간" : h + "시간 " + m + "분";
	}
}
