package com.hexagon.hrms.domain;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnnualLeave {
	private int annualLeaveId;
	private Emp emp;
	private int leaveYear;
	private BigDecimal grantedDays;
	private BigDecimal usedDays;

	public BigDecimal getRemainDays() {
		if (grantedDays == null || usedDays == null) {
			return BigDecimal.ZERO;
		}
		return grantedDays.subtract(usedDays);
	}
}
