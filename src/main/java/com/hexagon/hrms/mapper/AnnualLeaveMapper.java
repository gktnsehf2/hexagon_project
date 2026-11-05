package com.hexagon.hrms.mapper;

import java.math.BigDecimal;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.hexagon.hrms.domain.AnnualLeave;

@Mapper
public interface AnnualLeaveMapper {
	// 사원의 해당 연도 연차
	AnnualLeave select(@Param("empId") int empId, @Param("leaveYear") int leaveYear);

	// 휴가 승인 시 사용 일수 증가
	int addUsedDays(@Param("empId") int empId, @Param("leaveYear") int leaveYear, @Param("days") BigDecimal days);

	// 연차 부여 
	int insert(AnnualLeave annualLeave);
}
