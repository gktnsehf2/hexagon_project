package com.hexagon.hrms.dto.response;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttendanceIssueDTO {
	private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

	private int empId;
	private String ename;
	private String departmentName;
	private String issueType;         
	private LocalDateTime checkInAt;  

	public String getIssueText() {
		if ("LATE".equals(issueType)) {
			return "지각 " + (checkInAt == null ? "" : checkInAt.format(HH_MM));
		}
		return "결근";
	}
}
