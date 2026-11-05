package com.hexagon.hrms.dto.request;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.Setter;

// 전체 휴가 현황
@Getter
@Setter
public class LeaveSearchRequestDTO {
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate from;         
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate to;
	private Integer departmentId;   
	private Integer leaveTypeId;
	private String status;          
	private String keyword;          
	private int currentPage = 1;
}
