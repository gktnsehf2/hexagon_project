package com.hexagon.hrms.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

/*
 인사발령 이력 (입사 · 승진 · 전보 · 퇴직)
 발령 전/후 조직·직급을 함께 남긴다 (입사는 "전"이 없고, 퇴직은 "후"가 없음)
*/
@Getter
@Setter
public class HrAppointment {
	private int hrAppointmentId;
	private Emp emp;                     // 대상 사원
	private String appointmentType;      // HIRE / PROMOTION / TRANSFER / RESIGN
	private Department beforeDepartment;
	private Department afterDepartment;
	private Job beforeJob;
	private Job afterJob;
	private LocalDate effectiveDate;     // 발령일
	private String reason;
	private Emp register;                // 등록한 사람
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
