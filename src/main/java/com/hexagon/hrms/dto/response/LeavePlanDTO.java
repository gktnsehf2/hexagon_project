package com.hexagon.hrms.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.hexagon.hrms.domain.Emp;
import com.hexagon.hrms.domain.LeaveType;

import lombok.Getter;
import lombok.Setter;

/*
 휴가 신청 계산 결과 (화면 미리보기 + 실제 신청에서 같이 사용)
 - days        : 주말·공휴일을 뺀 사용 일수 (반차 0.5)
 - remainDays  : 그해 잔여 연차 (부여 - 사용), pendingDays: 결재 대기 중인 연차·반차 일수
 - approver    : 자동 지정된 결재자 (회장 본인 신청이면 null → 바로 승인)
*/
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

	// 이번 신청이 승인되면 남는 연차 (대기 중인 건도 승인된다고 보고 계산)
	public BigDecimal getAfterDays() {
		if (remainDays == null) {
			return null;
		}
		BigDecimal after = remainDays.subtract(pendingDays == null ? BigDecimal.ZERO : pendingDays);
		return isDeduct() ? after.subtract(days) : after;
	}
}
