package com.hexagon.hrms.mapper;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.hexagon.hrms.domain.LeaveType;

@Mapper
public interface LeaveRequestMapper {
	// 그날 승인된 휴가의 유형 (오전 반차 + 오후 반차처럼 여러 건일 수 있음)
	List<LeaveType> selectApprovedTypesOn(@Param("empId") int empId, @Param("date") LocalDate date);
}
