package com.hexagon.hrms.domain;

import java.io.Serializable;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

// 사원 인사정보 (로그인 정보는 Account에 있음)
@Getter
@Setter
public class Emp implements Serializable {
	private static final long serialVersionUID = 1L;

	private int empId;
	private String empNo;
	private String ename;
	private String gender;         // 성별 M / F (V3 에서 추가, 기존 사원은 null)
	private String email;
	private String phone;
	private LocalDate hiredate;
	private String photoPath;      // 증명사진 파일 경로 (V3 에서 추가, 없으면 null)
	private LocalDate resignDate;
	private Department department; // 소속 조직
	private Job job;               // 직급
	private HrPosition hrPosition; // 직책 (없으면 null)
	private String status;         // ACTIVE / RESIGNED

	public boolean isActive() {
		return "ACTIVE".equals(status);
	}

	public String getStatusName() {
		return isActive() ? "재직" : "퇴직";
	}

	// 화면 표시용 (성별이 비어 있으면 '-')
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
