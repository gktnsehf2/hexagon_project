package com.hexagon.hrms.dto.response;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeaveStatusSummaryDTO {
	private int totalCount;
	private int requestedCount;
	private int approvedCount;
	private int rejectedCount;
	private int canceledCount;
	private BigDecimal approvedDays; 

	public String getApprovedDaysText() {
		return approvedDays == null ? "0" : approvedDays.stripTrailingZeros().toPlainString();
	}
}
