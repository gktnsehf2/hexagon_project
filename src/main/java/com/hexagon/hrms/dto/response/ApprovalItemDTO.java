package com.hexagon.hrms.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalItemDTO {
	private static final DateTimeFormatter MM_DD = DateTimeFormatter.ofPattern("MM/dd");

	private String approvalType;     
	private int requestId;
	private String ename;        
	private String departmentName;
	private String detail;         
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
