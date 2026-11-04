package com.hexagon.hrms.dto.request;

import com.hexagon.hrms.domain.Department;

import lombok.Getter;
import lombok.Setter;

/*
 부서 등록 / 수정 화면(S-07) 입력값
 단계(본사 1 / 부 2 / 팀 3)는 따로 받지 않고 상위 조직의 단계 + 1 로 정함
 검사는 DepartmentService.create / update
*/
@Getter
@Setter
public class DepartmentFormDTO {
	private String departmentCode;  // 영문 대문자·숫자·_ (예: DEV3_TEAM)
	private String departmentName;
	private Integer parentId;       // 상위 조직 (본사만 null)
	private Integer managerId;      // 조직장 (직속 재직자 중, 비우면 없음)
	private int isActive = 1;       // 수정에서만 사용 (0 = 사용 안 함)

	public static DepartmentFormDTO from(Department d) {
		DepartmentFormDTO form = new DepartmentFormDTO();
		form.setDepartmentCode(d.getDepartmentCode());
		form.setDepartmentName(d.getDepartmentName());
		form.setParentId(d.getParentId());
		form.setManagerId(d.getManagerId());
		form.setIsActive(d.getIsActive());
		return form;
	}
}
