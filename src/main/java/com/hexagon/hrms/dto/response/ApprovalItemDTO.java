package com.hexagon.hrms.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Getter;
import lombok.Setter;

/*
 휴가 / 근태 정정 신청 한 건 (대시보드 "내 결재 대기", "진행 중인 내 신청", "예정된 휴가")
 세 테이블을 union 으로 한 번에 가져오기 위해 공통 모양으로 맞춘 DTO
*/
@Getter
@Setter
public class ApprovalItemDTO {
	private static final DateTimeFormatter MM_DD = DateTimeFormatter.ofPattern("MM/dd");

	private String approvalType;      // LEAVE / CORRECTION
	private int requestId;
	private String ename;             // 신청자
	private String departmentName;
	private String detail;            // 휴가 유형 이름 등
	private LocalDate startDate;
	private LocalDate endDate;
	private LocalDateTime requestDate;
	private String status;

	public String getTypeName() {
		if ("LEAVE".equals(approvalType)) {
			return "휴가";
		}
		if ("CORRECTION".equals(approvalType)) {
			return "근태 정정";
		}
		return approvalType;
	}

	// 11/12 또는 11/12~11/13
	public String getDateText() {
		if (startDate == null) {
			return "";
		}
		if (endDate == null || endDate.equals(startDate)) {
			return startDate.format(MM_DD);
		}
		return startDate.format(MM_DD) + "~" + endDate.format(MM_DD);
	}

	public String getStatusName() {
		if (status == null) {
			return "";
		}
		switch (status) {
		case "REQUESTED":
			return "승인 대기";
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
}
