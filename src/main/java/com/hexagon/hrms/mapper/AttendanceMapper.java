package com.hexagon.hrms.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.hexagon.hrms.domain.Attendance;
import com.hexagon.hrms.domain.Emp;

@Mapper
public interface AttendanceMapper {
	// 사원의 특정 날짜 근태)
	Attendance selectByEmpAndDate(@Param("empId") int empId, @Param("workDate") LocalDate workDate);

	// 사원의 기간 근태
	List<Attendance> selectByEmpBetween(@Param("empId") int empId, @Param("from") LocalDate from, @Param("to") LocalDate to);

	// 근태 현황 대상 재직자 
	List<Emp> selectStatusEmps(@Param("deptIds") Collection<Integer> deptIds, @Param("keyword") String keyword,
							   @Param("to") LocalDate to);

	// 여러 사원의 기간 근태
	List<Attendance> selectByEmpsBetween(@Param("empIds") Collection<Integer> empIds,
										 @Param("from") LocalDate from, @Param("to") LocalDate to);

	// 정정 승인 시 출퇴근 시각·상태를 한 번에 바꿈
	int updateTimes(@Param("attendanceId") int attendanceId, @Param("checkInAt") LocalDateTime checkInAt,
					@Param("checkOutAt") LocalDateTime checkOutAt, @Param("status") String status);

	// 출근 
	int insert(Attendance attendance);

	// 출근 
	int updateCheckIn(Attendance attendance);

	// 퇴근
	int updateCheckOut(Attendance attendance);
}
