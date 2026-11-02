package com.hexagon.hrms.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexagon.hrms.domain.Department;
import com.hexagon.hrms.mapper.DepartmentMapper;
import com.hexagon.hrms.security.AccountDetails;

import lombok.RequiredArgsConstructor;

/*
 조직 트리 계산 (본사 > 부 > 팀)
 조직이 24개뿐이라 전체를 가져와 자바에서 하위 조직을 계산한다
*/
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DepartmentService {

	private final DepartmentMapper departmentMapper;

	public List<Department> getAll() {
		return departmentMapper.selectAll();
	}

	public Department get(int departmentId) {
		return departmentMapper.select(departmentId);
	}

	// roots 와 그 아래 모든 하위 조직의 ID
	// 조직 이동(F-07)으로 부모 ID가 자식보다 커질 수 있으므로 ID 순서에 기대지 않고 너비 우선 탐색
	public List<Integer> getSubtreeIds(Collection<Integer> roots) {
		Map<Integer, List<Integer>> children = new HashMap<>();
		for (Department d : departmentMapper.selectAll()) {
			if (d.getParentId() != null) {
				children.computeIfAbsent(d.getParentId(), k -> new ArrayList<>()).add(d.getDepartmentId());
			}
		}
		Set<Integer> result = new LinkedHashSet<>();
		Deque<Integer> queue = new ArrayDeque<>(roots);
		while (!queue.isEmpty()) {
			Integer id = queue.poll();
			if (result.add(id)) {
				queue.addAll(children.getOrDefault(id, Collections.emptyList()));
			}
		}
		return new ArrayList<>(result);
	}

	// 사원이 조직장으로 있는 조직 ID 목록
	public List<Integer> getManagedIds(int empId) {
		List<Integer> ids = new ArrayList<>();
		for (Department d : departmentMapper.selectAll()) {
			if (d.getManagerId() != null && d.getManagerId() == empId) {
				ids.add(d.getDepartmentId());
			}
		}
		return ids;
	}

	// 소속 조직이 속한 부(2단계) ID. 본사(1단계) 소속이면 null
	public Integer getHqId(int departmentId) {
		Map<Integer, Department> map = departmentMapper.selectAll().stream()
			.collect(Collectors.toMap(Department::getDepartmentId, Function.identity()));
		Department d = map.get(departmentId);
		while (d != null && d.getDepartmentLevel() > 2) {
			d = map.get(d.getParentId());
		}
		return (d != null && d.getDepartmentLevel() == 2) ? d.getDepartmentId() : null;
	}

	// ID 목록 → 조직 목록 (트리 순서)
	public List<Department> getByIds(Collection<Integer> ids) {
		List<Department> list = new ArrayList<>();
		for (Department d : departmentMapper.selectAll()) {
			if (ids.contains(d.getDepartmentId())) {
				list.add(d);
			}
		}
		return list;
	}

	/*
	 로그인한 사람이 볼 수 있는 조직 범위 (사원 목록, 근태·휴가 현황에서 같은 규칙을 씀)
	 화면마다 권한 코드만 다르므로 전사 / 부서 / 팀 권한 코드를 받아서 계산한다
	   예) 사원 목록: EMP_READ_ALL, EMP_READ_DEPT, EMP_READ_TEAM
	       근태 현황: ATT_READ_ALL, ATT_READ_DEPT, ATT_READ_TEAM
	   전사 : 모든 조직
	   부서 : 본인이 조직장인 조직과 그 하위 (이사 → 부 전체, 회장 → 본사 이하 전사)
	          조직장이 아니면 소속 부 전체 (사장처럼 본사 소속이면 전사)
	   팀   : 소속 팀 + 본인이 조직장인 팀
	 여러 권한이 있으면 가장 넓은 범위를 쓴다
	*/
	public EmpScope getScope(AccountDetails loginUser, String allCode, String deptCode, String teamCode) {
		if (loginUser.hasAuthority(allCode)) {
			return new EmpScope("전사", null);
		}

		int empId = loginUser.getEmpId();
		int myDeptId = loginUser.getAccount().getEmp().getDepartment().getDepartmentId();
		Set<Integer> roots = new LinkedHashSet<>(getManagedIds(empId));

		if (loginUser.hasAuthority(deptCode)) {
			// 조직장이면 "소속 조직" 대신 "관리하는 조직"을 기준으로 한다
			if (roots.isEmpty()) {
				Integer hqId = getHqId(myDeptId);
				roots.add(hqId != null ? hqId : myDeptId);
			}
		} else if (loginUser.hasAuthority(teamCode)) {
			roots.add(myDeptId);
		} else {
			return new EmpScope("없음", Collections.emptyList());
		}

		return new EmpScope(makeLabel(roots), getSubtreeIds(roots));
	}

	// 화면 표시용 범위 이름 (예: "개발부 전체", "개발1팀")
	private String makeLabel(Set<Integer> roots) {
		List<String> names = new ArrayList<>();
		for (Department d : getByIds(roots)) {
			names.add(d.getDepartmentName() + (d.getDepartmentLevel() < 3 ? " 전체" : ""));
		}
		return names.isEmpty() ? "없음" : String.join(", ", names);
	}
}
