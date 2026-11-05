package com.hexagon.hrms.dto.response;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class TodayAttendanceDTO {
	private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

	private String state;
	private String status;        /
	private LocalDateTime checkInAt;
	private LocalDateTime checkOutAt;
	private String leaveTypeName;
	private LocalTime workStart; 
	private LocalTime workEnd;   

	public boolean isCanCheckIn() {
		return "BEFORE".equals(state);
	}

	public boolean isCanCheckOut() {
		return "WORKING".equals(state);
	}

	public String getCheckInTime() {
		return checkInAt == null ? "" : checkInAt.format(HH_MM);
	}

	public String getCheckOutTime() {
		return checkOutAt == null ? "" : checkOutAt.format(HH_MM);
	}

	public String getWorkStartText() {
		return workStart == null ? "" : workStart.format(HH_MM);
	}

	public String getWorkEndText() {
		return workEnd == null ? "" : workEnd.format(HH_MM);
	}
}
