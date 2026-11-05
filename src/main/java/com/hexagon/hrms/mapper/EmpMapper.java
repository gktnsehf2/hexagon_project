package com.hexagon.hrms.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.hexagon.hrms.common.web.Pager;
import com.hexagon.hrms.domain.Emp;
import com.hexagon.hrms.dto.request.EmpSearchRequestDTO;

@Mapper
public interface EmpMapper {
	// 사원 1명 (조직, 직급 포함)
	Emp select(int empId);

	// 사원 목록: 조회 가능한 조직
	// deptIds 가 null 이면 전사
	List<Emp> selectList(@Param("search") EmpSearchRequestDTO search,
						 @Param("deptIds") List<Integer> deptIds,
						 @Param("pager") Pager pager);

	int selectCount(@Param("search") EmpSearchRequestDTO search,
					@Param("deptIds") List<Integer> deptIds);

	// 그해 입사자의 다음 순번 
	int selectNextSeq(@Param("year") int year);

	// 이메일 중복 확인 
	int countByEmail(@Param("email") String email, @Param("exceptEmpId") int exceptEmpId);

	// 사원 등록 )
	int insert(Emp emp);

	// 기본 정보 수정
	int update(Emp emp);

	// 부서·직급 변경 
	int updateAssignment(@Param("empId") int empId, @Param("departmentId") int departmentId, @Param("jobId") int jobId);

	int updatePhoto(@Param("empId") int empId, @Param("photoPath") String photoPath);

	// 퇴직 처리
	int updateResign(Emp emp);
}
