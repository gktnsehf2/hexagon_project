package com.hexagon.hrms.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import lombok.Getter;
import lombok.Setter;

// 일별 출퇴근 기록
@Getter
@Setter
public class Attendance {
	private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");
	private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("MM/dd (E)", Locale.KOREAN);

	private int attendanceId;
	private Emp emp;
	private LocalDate workDate;
	private LocalDateTime checkInAt;
	private LocalDateTime checkOutAt;
	private String status; 


	public String getCheckInTime() {
		return checkInAt == null ? "-" : checkInAt.format(HH_MM);
	}

	public String getCheckOutTime() {
		return checkOutAt == null ? "-" : checkOutAt.format(HH_MM);
	}

	public String getWorkDateText() {
		return workDate == null ? "" : workDate.format(DAY);
	}

	public String getStatusName() {
		if (status == null) {
			return "";
		}
		switch (status) {
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
		default:
			return status;
		}
	}
}
