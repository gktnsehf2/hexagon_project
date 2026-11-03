package com.hexagon.hrms.mapper;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.hexagon.hrms.domain.LeaveRequest;
import com.hexagon.hrms.domain.LeaveType;

@Mapper
public interface LeaveRequestMapper {
	// 그날 승인된 휴가의 유형 (오전 반차 + 오후 반차처럼 여러 건일 수 있음)
	List<LeaveType> selectApprovedTypesOn(@Param("empId") int empId, @Param("date") LocalDate date);

	// 기간과 겹치는 승인된 휴가 (내 근태 월 조회)
	List<LeaveRequest> selectApprovedBetween(@Param("empId") int empId, @Param("from") LocalDate from, @Param("to") LocalDate to);

	// 내 휴가 신청 내역 (기간과 겹치는 건, 최근 신청 순)
	List<LeaveRequest> selectMyList(@Param("empId") int empId, @Param("from") LocalDate from, @Param("to") LocalDate to);

	// 한 건 (없으면 null)
	LeaveRequest select(int leaveRequestId);

	// 본인 신청 취소 (대기 중인 건만 바뀜 → 0 이면 이미 처리됨)
	int cancel(@Param("leaveRequestId") int leaveRequestId, @Param("empId") int empId);
}
