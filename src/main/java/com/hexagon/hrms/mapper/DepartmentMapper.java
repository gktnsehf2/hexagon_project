package com.hexagon.hrms.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.hexagon.hrms.domain.Department;

@Mapper
public interface DepartmentMapper {
	// 조직 1개 (상위 조직 포함)
	Department select(int departmentId);

	// 전체 조직 (트리 순서). 조직이 24개뿐이라 범위 계산은 자바에서 함
	List<Department> selectAll();

	// 조직장이 다른 부서로 옮기면 원래 부서의 조직장 지정 해제 (그 사이 다른 사람으로 바뀌었으면 그대로)
	int clearManager(@Param("departmentId") int departmentId, @Param("empId") int empId);
}
