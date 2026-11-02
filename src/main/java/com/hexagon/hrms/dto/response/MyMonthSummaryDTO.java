package com.hexagon.hrms.dto.response;

import lombok.Getter;
import lombok.Setter;

// 이번 달 내 근태 집계
@Getter
@Setter
public class MyMonthSummaryDTO {
	private int workDays;        // 출근한 날
	private int lateCount;       // 지각
	private int earlyLeaveCount; // 조퇴
}
