package com.hexagon.hrms.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import lombok.Getter;
import lombok.Setter;

// 휴가 신청 (1단계 결재: REQUESTED → APPROVED / REJECTED, 대기 중이면 본인이 CANCELED)
@Getter
@Setter
public class LeaveRequest {
	private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd (E)", Locale.KOREAN);
	private static final DateTimeFormatter SHORT_DAY = DateTimeFormatter.ofPattern("MM-dd (E)", Locale.KOREAN);
	private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private int leaveRequestId;
	private Emp emp;              // 신청 사원 (ID만)
	private LeaveType leaveType;
	private LocalDate startDate;
	private LocalDate endDate;
	private BigDecimal days;      // 사용 일수 (반차 0.5)
	private String reason;
	private String status;        // REQUESTED / APPROVED / REJECTED / CANCELED
	private LocalDateTime requestDate;
	private Emp approver;         // 결재자 (회장 본인 신청이면 null)
	private LocalDateTime approvedAt;
	private String rejectReason;

	// 대기 중인 건만 본인이 취소할 수 있음 (승인 후에는 연차가 이미 차감되어 취소 불가)
	public boolean isCancelable() {
		return "REQUESTED".equals(status);
	}

	// 화면 표시용 신청번호 예) LV-2026-0012
	public String getRequestNo() {
		int year = requestDate != null ? requestDate.getYear() : (startDate != null ? startDate.getYear() : 0);
		return String.format("LV-%d-%04d", year, leaveRequestId);
	}

	public String getPeriodText() {
		if (startDate == null) {
			return "";
		}
		if (endDate == null || startDate.equals(endDate)) {
			return startDate.format(DAY);
		}
		return startDate.format(DAY) + " ~ " + (startDate.getYear() == endDate.getYear() ? endDate.format(SHORT_DAY) : endDate.format(DAY));
	}

	// 1.0 → "1", 0.5 → "0.5"
	public String getDaysText() {
		return days == null ? "" : days.stripTrailingZeros().toPlainString();
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

	// 상태 배지 색 (hrms.css 의 is-*)
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

	// 휴가 유형 배지 색 (대시보드 오늘 휴가자와 같은 규칙)
	public String getTypeTone() {
		String code = leaveType == null ? null : leaveType.getLeaveTypeCode();
		if (code == null) {
			return "is-gray";
		}
		if ("ANNUAL".equals(code)) {
			return "is-blue";
		}
		if ("SICK".equals(code)) {
			return "is-red";
		}
		return code.startsWith("HALF_") ? "is-teal" : "is-purple";
	}
}
