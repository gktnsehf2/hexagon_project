package com.hexagon.hrms.dto.response;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

// 전체 휴가 현황 상단 카드 (상태 조건은 빼고 나머지 검색 조건으로 집계)
@Getter
@Setter
public class LeaveStatusSummaryDTO {
	private int totalCount;
	private int requestedCount;
	private int approvedCount;
	private int rejectedCount;
	private int canceledCount;
	private BigDecimal approvedDays; // 승인된 휴가의 사용 일수 합

	public String getApprovedDaysText() {
		return approvedDays == null ? "0" : approvedDays.stripTrailingZeros().toPlainString();
	}
}
