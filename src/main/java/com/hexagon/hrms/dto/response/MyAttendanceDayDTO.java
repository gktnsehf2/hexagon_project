package com.hexagon.hrms.dto.response;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.hexagon.hrms.domain.Attendance;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class MyAttendanceDayDTO {
	private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("MM/dd (E)", Locale.KOREAN);

	private LocalDate workDate;
	private Attendance attendance;
	private String dayStatus;
	private String note;           
	private int workMinutes;     
	private int overtimeMinutes;  
	private boolean today;

	public String getWorkDateText() {
		return workDate == null ? "" : workDate.format(DAY);
	}

	public boolean isWeekend() {
		return workDate != null && (workDate.getDayOfWeek() == DayOfWeek.SATURDAY || workDate.getDayOfWeek() == DayOfWeek.SUNDAY);
	}

	public String getCheckInTime() {
		return attendance == null ? "-" : attendance.getCheckInTime();
	}

	public String getCheckOutTime() {
		return attendance == null ? "-" : attendance.getCheckOutTime();
	}

	public String getWorkTimeText() {
		return workMinutes <= 0 ? "-" : MyAttendanceMonthDTO.toHourText(workMinutes);
	}

	public String getOvertimeText() {
		return MyAttendanceMonthDTO.toHourText(overtimeMinutes);
	}

	public boolean isCorrectable() {
		return "NORMAL".equals(dayStatus) || "LATE".equals(dayStatus)
			|| "EARLY_LEAVE".equals(dayStatus) || "ABSENT".equals(dayStatus);
	}

	public String getStatusName() {
		if (dayStatus == null) {
			return "";
		}
		switch (dayStatus) {
		case "NORMAL":
			return "정상";
		case "LATE":
			return "지각";
		case "EARLY_LEAVE":
			return "조퇴";
		case "ABSENT":
			return "결근";
		case "ON_LEAVE":
			return "휴가";
		case "HOLIDAY":
			return "휴일";
		case "BEFORE":
			return "출근 전";
		default:
			return dayStatus;
		}
	}

	public String getStatusTone() {
		if (dayStatus == null) {
			return "is-gray";
		}
		switch (dayStatus) {
		case "NORMAL":
			return "is-green";
		case "LATE":
			return "is-orange";
		case "EARLY_LEAVE":
			return "is-purple";
		case "ABSENT":
			return "is-red";
		case "ON_LEAVE":
			return "is-blue";
		default:
			return "is-gray";
		}
	}
}
