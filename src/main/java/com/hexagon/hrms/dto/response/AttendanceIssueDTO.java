package com.hexagon.hrms.dto.response;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Getter;
import lombok.Setter;

// 대시보드 "근태 이상자" 한 줄 (지각 또는 결근)
@Getter
@Setter
public class AttendanceIssueDTO {
	private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

	private int empId;
	private String ename;
	private String departmentName;
	private String issueType;         // LATE / ABSENT
	private LocalDateTime checkInAt;  // 지각이면 출근 시각

	public String getIssueText() {
		if ("LATE".equals(issueType)) {
			return "지각 " + (checkInAt == null ? "" : checkInAt.format(HH_MM));
		}
		return "결근";
	}
}
