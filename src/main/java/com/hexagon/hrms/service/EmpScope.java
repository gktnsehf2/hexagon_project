package com.hexagon.hrms.service;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class EmpScope {
	private final String label;         
	private final List<Integer> deptIds;

	public boolean isAll() {
		return deptIds == null;
	}

	public boolean isVisible() {
		return deptIds == null || !deptIds.isEmpty();
	}

	public boolean contains(int departmentId) {
		return deptIds == null || deptIds.contains(departmentId);
	}
}
