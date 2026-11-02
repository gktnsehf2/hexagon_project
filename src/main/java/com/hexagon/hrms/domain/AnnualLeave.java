package com.hexagon.hrms.domain;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

// 연도별 연차 부여·사용 일수 (반차가 있어 0.5 단위)
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
