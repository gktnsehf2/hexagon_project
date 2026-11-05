package com.hexagon.hrms.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.hexagon.hrms.domain.Emp;
import com.hexagon.hrms.domain.LeaveType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeavePlanDTO {
	private LeaveType leaveType;
	private LocalDate startDate;
	private LocalDate endDate;
	private BigDecimal days;
	private int year;
	private BigDecimal remainDays;
	private BigDecimal pendingDays;
	private Emp approver;

	public boolean isDeduct() {
		return leaveType != null && leaveType.getIsDeduct() == 1;
	}

	public BigDecimal getAfterDays() {
		if (remainDays == null) {
			return null;
		}
		BigDecimal after = remainDays.subtract(pendingDays == null ? BigDecimal.ZERO : pendingDays);
		return isDeduct() ? after.subtract(days) : after;
	}
}
