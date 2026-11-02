package com.hexagon.hrms.dto.response;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.hexagon.hrms.domain.Attendance;

import lombok.Getter;
import lombok.Setter;

/*
 내 근태 화면의 하루 한 줄
 근태 기록(attendance)이 없는 날도 결근·휴가·공휴일로 채워서 보여준다
 dayStatus : NORMAL / LATE / EARLY_LEAVE / ABSENT / ON_LEAVE / HOLIDAY / BEFORE(오늘 출근 전)
*/
@Getter
@Setter
public class MyAttendanceDayDTO {
	private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("MM/dd (E)", Locale.KOREAN);

	private LocalDate workDate;
	private Attendance attendance; // 기록이 없으면 null
	private String dayStatus;
	private String note;           // 비고 (휴가 유형, 공휴일 이름, 퇴근 기록 없음 등)
	private int workMinutes;       // 근무 시간(분), 점심 1시간 제외
	private int overtimeMinutes;   // 18:00 이후 근무(분), 30분 단위 내림
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

	// 정정 신청 대상: 출근 기록이 있거나 결근인 날 (휴가·공휴일·출근 전은 제외)
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
