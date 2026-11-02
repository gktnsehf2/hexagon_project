package com.hexagon.hrms.domain;

import lombok.Getter;
import lombok.Setter;

// 휴가 유형 (연차, 오전/오후 반차, 병가, 경조사, 공가)
@Getter
@Setter
public class LeaveType {
	private int leaveTypeId;
	private String leaveTypeCode; // ANNUAL / HALF_AM / HALF_PM / SICK / FAMILY_EVENT / OFFICIAL
	private String leaveTypeName;
	private int isDeduct;         // 1 이면 연차에서 차감
	private int isActive;

	// 반차는 하루 중 일부만 쉬므로 출근은 해야 함
	public boolean isHalfDay() {
		return leaveTypeCode != null && leaveTypeCode.startsWith("HALF_");
	}
}
