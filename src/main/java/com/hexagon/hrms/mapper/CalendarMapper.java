package com.hexagon.hrms.mapper;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.hexagon.hrms.dto.response.CalendarLeaveDTO;

@Mapper
public interface CalendarMapper {
	// 기간과 겹치는 전 직원의 승인된 휴가
	List<CalendarLeaveDTO> selectApprovedLeaves(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
