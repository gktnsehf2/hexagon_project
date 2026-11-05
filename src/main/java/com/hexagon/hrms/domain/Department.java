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
	private Integer parentId;      
	private Department parent;  
	private Integer managerId;  
	private int isActive;

	
	private String managerName;    		// 조직장 이름
	private String managerJobName; 		// 조직장 직급
	private int memberCount;      		    // 직속 재직 인원
	private int totalCount;        				// 하위 조직까지 합친 재직 인원 

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

	// 조직장 직책 이름
	public String getManagerTitle() {
		return departmentLevel == 1 ? "회장" : (departmentLevel == 2 ? "부서장" : "팀장");
	}

	public boolean isActive() {
		return isActive == 1;
	}

	public String getPathName() {
		if (parent != null && parent.getDepartmentLevel() > 1) {
			return parent.getDepartmentName() + " > " + departmentName;
		}
		return departmentName;
	}

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
