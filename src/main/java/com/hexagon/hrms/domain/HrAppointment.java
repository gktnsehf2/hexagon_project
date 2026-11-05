package com.hexagon.hrms.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class HrAppointment {
	private int hrAppointmentId;
	private Emp emp;         				              // 대상 사원
	private String appointmentType;    
	private Department beforeDepartment;
	private Department afterDepartment;
	private Job beforeJob;
	private Job afterJob;
	private LocalDate effectiveDate;    				 // 발령일
	private String reason;
	private Emp register;               					 // 등록한 사람
	private LocalDateTime createdAt;

	// 화면 표시용 발령 구분
	public String getTypeName() {
		if (appointmentType == null) {
			return "";
		}
		switch (appointmentType) {
		case "HIRE":
			return "입사";
		case "PROMOTION":
			return "승진";
		case "TRANSFER":
			return "전보";
		case "RESIGN":
			return "퇴직";
		default:
			return appointmentType;
		}
	}
}
