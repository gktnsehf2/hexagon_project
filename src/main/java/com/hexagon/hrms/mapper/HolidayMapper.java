package com.hexagon.hrms.mapper;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.hexagon.hrms.domain.Holiday;

@Mapper
public interface HolidayMapper {
	// 기간 안의 공휴일
	List<Holiday> selectBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
