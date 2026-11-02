package com.hexagon.hrms.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.hexagon.hrms.domain.AnnualLeave;

@Mapper
public interface AnnualLeaveMapper {
	// 사원의 해당 연도 연차 (부여 전이면 null)
	AnnualLeave select(@Param("empId") int empId, @Param("leaveYear") int leaveYear);

	// 연차 부여 (사원 등록 시 그해 연차)
	int insert(AnnualLeave annualLeave);
}
