package com.hexagon.hrms.dto.request;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.Setter;

/*
 휴가 신청 화면(S-13) 입력값 (fetch 로 폼 형식 전송)
 검사는 LeaveService.plan 에서 (미리보기와 신청이 같은 규칙을 쓰도록)
*/
@Getter
@Setter
public class LeaveFormRequestDTO {
	private Integer leaveTypeId;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate startDate;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate endDate;     // 반차는 무시하고 시작일과 같게 처리

	private String reason;
}
