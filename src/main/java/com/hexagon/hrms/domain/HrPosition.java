package com.hexagon.hrms.domain;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

// 직책 (회장, 사장, 부서장, 팀장). 팀원은 직책 없음(null)
@Getter
@Setter
public class HrPosition implements Serializable {
	private static final long serialVersionUID = 1L;

	private int hrPositionId;
	private String positionCode;
	private String positionName;
	private int isActive;
}
