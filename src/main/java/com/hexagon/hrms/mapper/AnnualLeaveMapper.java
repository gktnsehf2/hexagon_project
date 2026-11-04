package com.hexagon.hrms.mapper;

import java.math.BigDecimal;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.hexagon.hrms.domain.AnnualLeave;

@Mapper
public interface AnnualLeaveMapper {
	// 사원의 해당 연도 연차 (부여 전이면 null)
	AnnualLeave select(@Param("empId") int empId, @Param("leaveYear") int leaveYear);

	// 휴가 승인 시 사용 일수 증가 (잔여가 모자라면 바뀌지 않고 0)
	int addUsedDays(@Param("empId") int empId, @Param("leaveYear") int leaveYear, @Param("days") BigDecimal days);

	// 연차 부여 (사원 등록 시 그해 연차)
	int insert(AnnualLeave annualLeave);
}
