package com.hexagon.hrms.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.hexagon.hrms.common.web.Pager;
import com.hexagon.hrms.domain.Emp;
import com.hexagon.hrms.dto.request.EmpSearchRequestDTO;

@Mapper
public interface EmpMapper {
	// 사원 1명 (조직, 직급, 직책 포함)
	Emp select(int empId);

	// 사원 목록: 조회 가능한 조직(deptIds) 안에서 검색 조건 + 페이징
	// deptIds 가 null 이면 전사
	List<Emp> selectList(@Param("search") EmpSearchRequestDTO search,
						 @Param("deptIds") List<Integer> deptIds,
						 @Param("pager") Pager pager);

	int selectCount(@Param("search") EmpSearchRequestDTO search,
					@Param("deptIds") List<Integer> deptIds);

	// 사번 순번 (사번 = EMP + 입사연도 + 순번 3자리, 기존 사원은 순번 = emp_id)
	int selectNextSeq();

	// 이메일 중복 확인 (수정할 때는 본인 제외: exceptEmpId, 등록은 0)
	int countByEmail(@Param("email") String email, @Param("exceptEmpId") int exceptEmpId);

	// 사원 등록 (생성된 emp_id 는 emp.empId 에 채워짐)
	int insert(Emp emp);

	// 기본 정보 수정 (이름, 성별, 이메일, 연락처). 조직·직급·직책은 인사 발령으로만 바꾼다
	int update(Emp emp);

	int updatePhoto(@Param("empId") int empId, @Param("photoPath") String photoPath);

	// 퇴직 처리 (status, resign_date)
	int updateResign(Emp emp);
}
