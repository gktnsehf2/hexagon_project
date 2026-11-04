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
	// 사원의 특정 날짜 근태 (없으면 null)
	Attendance selectByEmpAndDate(@Param("empId") int empId, @Param("workDate") LocalDate workDate);

	// 사원의 기간 근태 (내 근태 월 조회)
	List<Attendance> selectByEmpBetween(@Param("empId") int empId, @Param("from") LocalDate from, @Param("to") LocalDate to);

	// 근태 현황 대상 재직자 (deptIds 가 null 이면 전사). 조직 트리 순 → 직급 높은 순
	List<Emp> selectStatusEmps(@Param("deptIds") Collection<Integer> deptIds, @Param("keyword") String keyword,
							   @Param("to") LocalDate to);

	// 여러 사원의 기간 근태 (근태 현황)
	List<Attendance> selectByEmpsBetween(@Param("empIds") Collection<Integer> empIds,
										 @Param("from") LocalDate from, @Param("to") LocalDate to);

	// 정정 승인 시 출퇴근 시각·상태를 한 번에 바꿈
	int updateTimes(@Param("attendanceId") int attendanceId, @Param("checkInAt") LocalDateTime checkInAt,
					@Param("checkOutAt") LocalDateTime checkOutAt, @Param("status") String status);

	// 출근 (오늘 기록이 없을 때)
	int insert(Attendance attendance);

	// 출근 (결근 등으로 오늘 기록만 먼저 있을 때)
	int updateCheckIn(Attendance attendance);

	// 퇴근
	int updateCheckOut(Attendance attendance);
}
