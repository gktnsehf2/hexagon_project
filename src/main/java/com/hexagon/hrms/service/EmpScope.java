package com.hexagon.hrms.service;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

/*
 로그인한 사람이 볼 수 있는 사원 범위 (사원 목록, 근태·휴가 현황 공통)
 deptIds 가 null 이면 전사, 빈 목록이면 볼 수 있는 조직 없음
*/
@Getter
@AllArgsConstructor
public class EmpScope {
	private final String label;          // 화면 표시용 (예: "개발부 전체")
	private final List<Integer> deptIds; // 조회 가능한 조직 ID (null = 전사)

	public boolean isAll() {
		return deptIds == null;
	}

	// 볼 수 있는 조직이 하나라도 있는지
	public boolean isVisible() {
		return deptIds == null || !deptIds.isEmpty();
	}

	// 이 조직 소속 사원을 볼 수 있는지 (사원 상세·수정·퇴직)
	public boolean contains(int departmentId) {
		return deptIds == null || deptIds.contains(departmentId);
	}
}
