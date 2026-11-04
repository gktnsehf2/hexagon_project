package com.hexagon.hrms.dto.request;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.Setter;

// 근태 정정 신청(S-10) 입력값 (fetch 로 폼 형식 전송). 검사는 AttendanceCorrectionService.request
@Getter
@Setter
public class AttendanceCorrectionFormDTO {
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate workDate;
	private String checkIn;   // "HH:mm" (비우면 출근은 그대로)
	private String checkOut;  // "HH:mm" (비우면 퇴근은 그대로)
	private String reason;
}
