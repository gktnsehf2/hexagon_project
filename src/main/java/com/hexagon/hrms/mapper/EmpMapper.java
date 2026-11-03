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

	// 사원 목록: 조회 가능한 조직(deptIds) 안에서 검색 조건 + 페이징
	// deptIds 가 null 이면 전사
	List<Emp> selectList(@Param("search") EmpSearchRequestDTO search,
						 @Param("deptIds") List<Integer> deptIds,
						 @Param("pager") Pager pager);

	int selectCount(@Param("search") EmpSearchRequestDTO search,
					@Param("deptIds") List<Integer> deptIds);

	// 그해 입사자의 다음 순번 (사번 = 입사연도 + 순번 4자리, 예: 20260001)
	int selectNextSeq(@Param("year") int year);

	// 이메일 중복 확인 (수정할 때는 본인 제외: exceptEmpId, 등록은 0)
	int countByEmail(@Param("email") String email, @Param("exceptEmpId") int exceptEmpId);

	// 사원 등록 (생성된 emp_id 는 emp.empId 에 채워짐)
	int insert(Emp emp);

	// 기본 정보 수정 (이름, 성별, 이메일, 연락처). 조직·직급은 따로 바꾼다 (이력이 남도록)
	int update(Emp emp);

	int updatePhoto(@Param("empId") int empId, @Param("photoPath") String photoPath);

	// 퇴직 처리 (status, resign_date)
	int updateResign(Emp emp);
}
