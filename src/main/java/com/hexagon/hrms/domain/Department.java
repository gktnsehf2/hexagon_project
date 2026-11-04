package com.hexagon.hrms.domain;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

// 조직 (1 본사 > 2 부 > 3 팀)
@Getter
@Setter
public class Department implements Serializable {
	private static final long serialVersionUID = 1L;

	private int departmentId;
	private String departmentCode;
	private String departmentName;
	private int departmentLevel;
	private Integer parentId;      // 트리 계산용 원래 FK 값 (최상위는 null)
	private Department parent;     // 상위 조직 (상세 조회 때만 채움)
	private Integer managerId;     // 조직장 사원 ID. Emp 객체로 두면 부서→조직장→부서… 로 순환되어 ID만 보관
	private int isActive;

	// 조직도 화면 전용 (selectTree 로 조회할 때만 채움)
	private String managerName;    // 조직장 이름
	private String managerJobName; // 조직장 직급
	private int memberCount;       // 직속 재직 인원
	private int totalCount;        // 하위 조직까지 합친 재직 인원 (서비스에서 계산)

	public String getLevelName() {
		switch (departmentLevel) {
		case 1:
			return "본사";
		case 2:
			return "부";
		default:
			return "팀";
		}
	}

	// 조직장 직책 이름 (Emp.getPositionName 과 같은 규칙)
	public String getManagerTitle() {
		return departmentLevel == 1 ? "회장" : (departmentLevel == 2 ? "부서장" : "팀장");
	}

	public boolean isActive() {
		return isActive == 1;
	}

	// 상세 화면 표시용 "부 > 팀" (상위 조직을 함께 조회한 경우만, 본사는 생략)
	public String getPathName() {
		if (parent != null && parent.getDepartmentLevel() > 1) {
			return parent.getDepartmentName() + " > " + departmentName;
		}
		return departmentName;
	}

	// 셀렉트 박스에서 계층이 보이도록 들여쓴 이름
	public String getIndentName() {
		if (departmentLevel <= 1) {
			return departmentName;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i < departmentLevel; i++) {
			sb.append("   ");
		}
		return sb.append("└ ").append(departmentName).toString();
	}
}
