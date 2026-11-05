package com.hexagon.hrms.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.hexagon.hrms.domain.Department;
import com.hexagon.hrms.domain.Emp;

@Mapper
public interface DepartmentMapper {
	// 조직 1개 (상위 조직 포함)
	Department select(int departmentId);

	// 전체 조직
	List<Department> selectAll();

	// 조직도: 사용 안 함 포함 여부 선택, 조직장 이름·직속 재직 인원까지
	List<Department> selectTree(@Param("includeInactive") boolean includeInactive);

	// 조직의 직속 재직자 (직급 높은 순)
	List<Emp> selectMembers(int departmentId);

	// 코드 중복 (exceptId 는 수정 중인 자기 자신)
	int countByCode(@Param("departmentCode") String departmentCode, @Param("exceptId") int exceptId);

	// 같은 상위 조직 아래 같은 이름
	int countSiblingName(@Param("parentId") Integer parentId, @Param("departmentName") String departmentName,
						 @Param("exceptId") int exceptId);

	// 삭제·사용 안 함 전에 확인
	int countActiveMembers(int departmentId);

	int countActiveChildren(int departmentId);

	// 사원(퇴직자 포함)·인사 이력·하위 조직에서 한 번이라도 쓰였는지 (쓰였으면 삭제 대신 사용 안 함)
	int countReferences(int departmentId);

	int insert(Department department);

	int update(Department department);

	int delete(int departmentId);

	int deactivate(int departmentId);

	// 조직장이 다른 부서로 옮기면 원래 부서의 조직장 지정 해제 (그 사이 다른 사람으로 바뀌었으면 그대로)
	int clearManager(@Param("departmentId") int departmentId, @Param("empId") int empId);
}
