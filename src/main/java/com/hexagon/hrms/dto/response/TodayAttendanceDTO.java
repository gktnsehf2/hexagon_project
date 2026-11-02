package com.hexagon.hrms.dto.response;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import lombok.Getter;
import lombok.Setter;

/*
 대시보드 시계 카드에 보여줄 "내 오늘 근태"
 state
   BEFORE  : 출근 전
   WORKING : 근무 중 (출근 O, 퇴근 X)
   DONE    : 퇴근
   LEAVE   : 하루 휴가 (출근 불필요)
*/
@Getter
@Setter
public class TodayAttendanceDTO {
	private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

	private String state;
	private String status;        // attendance.status (NORMAL / LATE / EARLY_LEAVE ...)
	private LocalDateTime checkInAt;
	private LocalDateTime checkOutAt;
	private String leaveTypeName; // 오늘 휴가가 있으면 유형 이름 (반차 포함)
	private LocalTime workStart;  // 반차를 반영한 출근 기준 시각
	private LocalTime workEnd;    // 반차를 반영한 퇴근 기준 시각

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
