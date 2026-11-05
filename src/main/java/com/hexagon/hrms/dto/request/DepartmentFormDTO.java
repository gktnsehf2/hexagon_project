package com.hexagon.hrms.dto.request;

import com.hexagon.hrms.domain.Department;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class DepartmentFormDTO {
	private String departmentCode; 
	private String departmentName;
	private Integer parentId;      
	private Integer managerId;      
	private int isActive = 1;       

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
