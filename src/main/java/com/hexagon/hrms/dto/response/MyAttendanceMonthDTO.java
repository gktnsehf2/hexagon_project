package com.hexagon.hrms.dto.response;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

// 내 근태 월 조회 결과
@Getter
@Setter
public class MyAttendanceMonthDTO {
	private YearMonth month;
	private YearMonth prevMonth;
	private YearMonth nextMonth;
	private YearMonth minMonth; 
	private YearMonth maxMonth; 
	private List<MyAttendanceDayDTO> days = new ArrayList<>(); 

	private int workDays;        
	private int normalCount;
	private int lateCount;
	private int earlyLeaveCount;
	private int absentCount;
	private int leaveDays;       
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

	public List<MyAttendanceDayDTO> getDaysDesc() {
		List<MyAttendanceDayDTO> list = new ArrayList<>(days);
		Collections.reverse(list);
		return list;
	}

	public static String toHourText(int minutes) {
		int h = minutes / 60;
		int m = minutes % 60;
		return m == 0 ? h + "시간" : h + "시간 " + m + "분";
	}
}
