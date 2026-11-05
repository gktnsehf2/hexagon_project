package com.hexagon.hrms.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexagon.hrms.domain.Department;
import com.hexagon.hrms.domain.Emp;
import com.hexagon.hrms.dto.request.DepartmentFormDTO;
import com.hexagon.hrms.exception.BusinessException;
import com.hexagon.hrms.exception.ErrorCode;
import com.hexagon.hrms.mapper.DepartmentMapper;
import com.hexagon.hrms.security.AccountDetails;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DepartmentService {

	private final DepartmentMapper departmentMapper;

	public List<Department> getAll() {
		return toTreeOrder(departmentMapper.selectAll());
	}

	private List<Department> toTreeOrder(List<Department> all) {
		Map<Integer, List<Department>> children = new HashMap<>();
		List<Department> roots = new ArrayList<>();
		Set<Integer> ids = new HashSet<>();
		for (Department d : all) {
			ids.add(d.getDepartmentId());
		}
		for (Department d : all) {
			if (d.getParentId() == null || !ids.contains(d.getParentId())) {
				roots.add(d);
			} else {
				children.computeIfAbsent(d.getParentId(), k -> new ArrayList<>()).add(d);
			}
		}
		List<Department> result = new ArrayList<>();
		Deque<Department> stack = new ArrayDeque<>();
		for (int i = roots.size() - 1; i >= 0; i--) {
			stack.push(roots.get(i));
		}
		while (!stack.isEmpty()) {
			Department d = stack.pop();
			result.add(d);
			List<Department> kids = children.getOrDefault(d.getDepartmentId(), Collections.emptyList());
			for (int i = kids.size() - 1; i >= 0; i--) {
				stack.push(kids.get(i));
			}
		}
		return result;
	}

	public Department get(int departmentId) {
		return departmentMapper.select(departmentId);
	}

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

	public List<Integer> getManagedIds(int empId) {
		List<Integer> ids = new ArrayList<>();
		for (Department d : departmentMapper.selectAll()) {
			if (d.getManagerId() != null && d.getManagerId() == empId) {
				ids.add(d.getDepartmentId());
			}
		}
		return ids;
	}

	public Integer getHqId(int departmentId) {
		Map<Integer, Department> map = departmentMapper.selectAll().stream()
			.collect(Collectors.toMap(Department::getDepartmentId, Function.identity()));
		Department d = map.get(departmentId);
		while (d != null && d.getDepartmentLevel() > 2) {
			d = map.get(d.getParentId());
		}
		return (d != null && d.getDepartmentLevel() == 2) ? d.getDepartmentId() : null;
	}


	public List<Department> getByIds(Collection<Integer> ids) {
		List<Department> list = new ArrayList<>();
		for (Department d : getAll()) {
			if (ids.contains(d.getDepartmentId())) {
				list.add(d);
			}
		}
		return list;
	}

	public EmpScope getScope(AccountDetails loginUser, String allCode, String deptCode, String teamCode) {
		if (loginUser.hasAuthority(allCode)) {
			return new EmpScope("전사", null);
		}

		int empId = loginUser.getEmpId();
		int myDeptId = loginUser.getAccount().getEmp().getDepartment().getDepartmentId();
		Set<Integer> roots = new LinkedHashSet<>(getManagedIds(empId));

		if (loginUser.hasAuthority(deptCode)) {
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

	private String makeLabel(Set<Integer> roots) {
		List<String> names = new ArrayList<>();
		for (Department d : getByIds(roots)) {
			names.add(d.getDepartmentName() + (d.getDepartmentLevel() < 3 ? " 전체" : ""));
		}
		return names.isEmpty() ? "없음" : String.join(", ", names);
	}
	
	public List<Department> getTree(boolean includeInactive) {
		List<Department> tree = toTreeOrder(departmentMapper.selectTree(includeInactive));
		Map<Integer, Department> byId = new HashMap<>();
		for (Department d : tree) {
			byId.put(d.getDepartmentId(), d);
			d.setTotalCount(d.getMemberCount());
		}

		for (int i = tree.size() - 1; i >= 0; i--) {
			Department d = tree.get(i);
			Department parent = d.getParentId() == null ? null : byId.get(d.getParentId());
			if (parent != null) {
				parent.setTotalCount(parent.getTotalCount() + d.getTotalCount());
			}
		}
		return tree;
	}

	public List<Emp> getMembers(int departmentId) {
		return departmentMapper.selectMembers(departmentId);
	}

	@Transactional
	public int create(DepartmentFormDTO form) {
		Department d = new Department();
		apply(d, form, true);
		d.setManagerId(null); 
		departmentMapper.insert(d);
		return d.getDepartmentId();
	}

	@Transactional
	public void update(int departmentId, DepartmentFormDTO form) {
		Department d = departmentMapper.select(departmentId);
		if (d == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 조직입니다.");
		}
		apply(d, form, false);
		Integer managerId = form.getManagerId() == null || form.getManagerId() <= 0 ? null : form.getManagerId();
		if (managerId != null) {
			boolean member = false;
			for (Emp e : departmentMapper.selectMembers(departmentId)) {
				if (e.getEmpId() == managerId) {
					member = true;
				}
			}
			if (!member) {
				throw new BusinessException("조직장은 이 조직의 직속 재직자 중에서 지정하세요.");
			}
		}
		d.setManagerId(managerId);
		
		if (form.getIsActive() == 0 && d.getIsActive() == 1) {
			if (d.getDepartmentLevel() == 1) {
				throw new BusinessException("본사는 사용 안 함으로 바꿀 수 없습니다.");
			}
			checkEmpty(departmentId);
			d.setManagerId(null);
		}
		d.setIsActive(form.getIsActive() == 0 ? 0 : 1);
		departmentMapper.update(d);
	}

	@Transactional
	public boolean delete(int departmentId) {
		Department d = departmentMapper.select(departmentId);
		if (d == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 조직입니다.");
		}
		if (d.getDepartmentLevel() == 1) {
			throw new BusinessException("본사는 삭제할 수 없습니다.");
		}
		checkEmpty(departmentId);
		if (departmentMapper.countReferences(departmentId) > 0) {
			departmentMapper.deactivate(departmentId);
			return false;
		}
		departmentMapper.delete(departmentId);
		return true;
	}

	private void checkEmpty(int departmentId) {
		int members = departmentMapper.countActiveMembers(departmentId);
		int children = departmentMapper.countActiveChildren(departmentId);
		if (members > 0 || children > 0) {
			throw new BusinessException("소속 사원 " + members + "명, 사용 중인 하위 조직 " + children
				+ "개가 있어 처리할 수 없습니다. 사원은 사원 수정에서, 하위 조직은 먼저 옮기거나 정리하세요.");
		}
	}

	private void apply(Department d, DepartmentFormDTO form, boolean isNew) {
		String code = form.getDepartmentCode() == null ? "" : form.getDepartmentCode().trim().toUpperCase();
		String name = form.getDepartmentName() == null ? "" : form.getDepartmentName().trim();
		if (!code.matches("^[A-Z0-9_]{2,20}$")) {
			throw new BusinessException("부서 코드는 영문 대문자·숫자·_ 2~20자로 입력하세요. (예: DEV3_TEAM)");
		}
		if (name.isEmpty() || name.length() > 60) {
			throw new BusinessException("부서명을 60자 이내로 입력하세요.");
		}
		int selfId = isNew ? 0 : d.getDepartmentId();
		if (departmentMapper.countByCode(code, selfId) > 0) {
			throw new BusinessException("이미 사용 중인 부서 코드입니다. (사용 안 함 조직 포함)");
		}

		Integer parentId = form.getParentId() == null || form.getParentId() <= 0 ? null : form.getParentId();
		int level;
		if (!isNew && d.getDepartmentLevel() == 1) {
			parentId = null;
			level = 1;
		} else {
			if (parentId == null) {
				throw new BusinessException("상위 조직을 선택하세요.");
			}
			Department parent = departmentMapper.select(parentId);
			if (parent == null || parent.getIsActive() != 1) {
				throw new BusinessException(ErrorCode.NOT_FOUND, "상위 조직을 찾을 수 없습니다.");
			}
			if (parent.getDepartmentLevel() >= 3) {
				throw new BusinessException("팀 아래에는 조직을 만들 수 없습니다. (본사 > 부 > 팀 3단계)");
			}
			if (!isNew && getSubtreeIdsAll(d.getDepartmentId()).contains(parentId)) {
				throw new BusinessException("자기 자신이나 하위 조직을 상위 조직으로 지정할 수 없습니다.");
			}
			level = parent.getDepartmentLevel() + 1;
			if (!isNew && level != d.getDepartmentLevel() && hasChildren(d.getDepartmentId())) {
				throw new BusinessException("하위 조직이 있는 부서는 단계(부 ↔ 팀)를 바꿀 수 없습니다.");
			}
		}
		if (departmentMapper.countSiblingName(parentId, name, selfId) > 0) {
			throw new BusinessException("같은 상위 조직 아래에 같은 이름의 조직이 있습니다.");
		}
		d.setDepartmentCode(code);
		d.setDepartmentName(name);
		d.setParentId(parentId);
		d.setDepartmentLevel(level);
	}

	private Set<Integer> getSubtreeIdsAll(int rootId) {
		Map<Integer, List<Integer>> children = new HashMap<>();
		for (Department d : departmentMapper.selectTree(true)) {
			if (d.getParentId() != null) {
				children.computeIfAbsent(d.getParentId(), k -> new ArrayList<>()).add(d.getDepartmentId());
			}
		}
		Set<Integer> result = new LinkedHashSet<>();
		Deque<Integer> queue = new ArrayDeque<>();
		queue.add(rootId);
		while (!queue.isEmpty()) {
			Integer id = queue.poll();
			if (result.add(id)) {
				queue.addAll(children.getOrDefault(id, Collections.emptyList()));
			}
		}
		return result;
	}

	private boolean hasChildren(int departmentId) {
		return getSubtreeIdsAll(departmentId).size() > 1;
	}
}
