package com.hexagon.hrms.dto.request;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.Setter;

// 전체 휴가 현황(S-23) 검색 조건 (파라미터 이름 = 필드 이름). 기간이 비면 이번 달
@Getter
@Setter
public class LeaveSearchRequestDTO {
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate from;          // 휴가 기간이 이 범위와 겹치는 신청
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate to;
	private Integer departmentId;    // 선택한 조직과 그 하위 조직
	private Integer leaveTypeId;
	private String status;           // REQUESTED / APPROVED / REJECTED / CANCELED, 비면 전체
	private String keyword;          // 이름 또는 사번
	private int currentPage = 1;
}
