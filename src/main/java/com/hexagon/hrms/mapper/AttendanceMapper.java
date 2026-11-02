package com.hexagon.hrms.mapper;

import java.time.LocalDate;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.hexagon.hrms.domain.Attendance;

@Mapper
public interface AttendanceMapper {
	// 사원의 특정 날짜 근태 (없으면 null)
	Attendance selectByEmpAndDate(@Param("empId") int empId, @Param("workDate") LocalDate workDate);

	// 출근 (오늘 기록이 없을 때)
	int insert(Attendance attendance);

	// 출근 (결근 등으로 오늘 기록만 먼저 있을 때)
	int updateCheckIn(Attendance attendance);

	// 퇴근
	int updateCheckOut(Attendance attendance);
}
