package com.hexagon.hrms.domain;

import java.io.Serializable;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Emp implements Serializable {
	private static final long serialVersionUID = 1L;

	private int empId;
	private String empNo;
	private String ename;
	private String gender;        				 // 성별 M / F
	private String email;
	private String phone;
	private LocalDate hiredate;
	private String photoPath;    			     // 증명사진 파일 경로)
	private LocalDate resignDate;
	private Department department;		// 소속 조직
	private Job job;           					    // 직급
	private String status;         				// ACTIVE / RESIGNED

	public boolean isActive() {
		return "ACTIVE".equals(status);
	}

	public String getStatusName() {
		return isActive() ? "재직" : "퇴직";
	}

	public String getPositionName() {
		if (department == null || department.getManagerId() == null || department.getManagerId().intValue() != empId) {
			return null;
		}
		switch (department.getDepartmentLevel()) {
		case 1:
			return "회장";
		case 2:
			return "부서장";
		default:
			return "팀장";
		}
	}

	public String getGenderName() {
		if ("M".equals(gender)) {
			return "남";
		}
		if ("F".equals(gender)) {
			return "여";
		}
		return "-";
	}
}
