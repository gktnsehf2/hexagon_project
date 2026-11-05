package com.hexagon.hrms.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import lombok.Getter;
import lombok.Setter;

// 근태 정정 신청 
@Getter
@Setter
public class AttendanceCorrection {
	private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd (E)", Locale.KOREAN);
	private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");
	private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private int attendanceCorrectionId;
	private Emp emp;                       							 // 신청 사원
	private LocalDate workDate;         						     // 정정할 근무일
	private LocalDateTime requestCheckInAt; 				 // 정정 요청 출근 
	private LocalDateTime requestCheckOutAt;				 // 정정 요청 퇴근
	private String reason;
	private String status;                
	private LocalDateTime requestDate;
	private Emp approver;                  							 // 결재자 
	private LocalDateTime approvedAt;
	private String rejectReason;

	// 화면 전용: 그날의 현재 근태 기록 
	private LocalDateTime currentCheckInAt;
	private LocalDateTime currentCheckOutAt;

	public boolean isCancelable() {
		return "REQUESTED".equals(status);
	}

	public String getRequestNo() {
		int year = requestDate != null ? requestDate.getYear() : (workDate != null ? workDate.getYear() : 0);
		return String.format("AC-%d-%04d", year, attendanceCorrectionId);
	}

	public String getWorkDateText() {
		return workDate == null ? "" : workDate.format(DAY);
	}

	public String getRequestInTime() {
		return requestCheckInAt == null ? "-" : requestCheckInAt.format(HH_MM);
	}

	public String getRequestOutTime() {
		return requestCheckOutAt == null ? "-" : requestCheckOutAt.format(HH_MM);
	}

	public String getCurrentInTime() {
		return currentCheckInAt == null ? "-" : currentCheckInAt.format(HH_MM);
	}

	public String getCurrentOutTime() {
		return currentCheckOutAt == null ? "-" : currentCheckOutAt.format(HH_MM);
	}

	public String getRequestDateText() {
		return requestDate == null ? "" : requestDate.format(DATE_TIME);
	}

	public String getApprovedAtText() {
		return approvedAt == null ? "" : approvedAt.format(DATE_TIME);
	}

	public String getStatusName() {
		if (status == null) {
			return "";
		}
		switch (status) {
		case "REQUESTED":
			return "대기";
		case "APPROVED":
			return "승인";
		case "REJECTED":
			return "반려";
		case "CANCELED":
			return "취소";
		default:
			return status;
		}
	}

	public String getStatusTone() {
		if (status == null) {
			return "is-gray";
		}
		switch (status) {
		case "REQUESTED":
			return "is-orange";
		case "APPROVED":
			return "is-green";
		case "REJECTED":
			return "is-red";
		default:
			return "is-gray";
		}
	}
}
