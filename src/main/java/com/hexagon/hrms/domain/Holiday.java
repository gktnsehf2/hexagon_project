package com.hexagon.hrms.domain;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

// 공휴일 (주말과 함께 결근 계산에서 제외)
@Getter
@Setter
public class Holiday {
	private LocalDate holidayDate;
	private String holidayName;
}
