package com.hexagon.hrms.mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.hexagon.hrms.domain.Attendance;
import com.hexagon.hrms.dto.response.ApprovalItemDTO;
import com.hexagon.hrms.dto.response.AttendanceIssueDTO;
import com.hexagon.hrms.dto.response.MyMonthSummaryDTO;
import com.hexagon.hrms.dto.response.TodayLeaveDTO;

 //대시보드 집계 전용 매퍼

@Mapper
public interface DashboardMapper {

	/*----------------------------------------------
	 조직 현황
	----------------------------------------------*/
	int countActiveEmp(@Param("deptIds") List<Integer> deptIds);

	int countCheckedIn(@Param("deptIds") List<Integer> deptIds, @Param("today") LocalDate today);

	List<TodayLeaveDTO> selectTodayLeaves(@Param("deptIds") List<Integer> deptIds, @Param("today") LocalDate today);

	List<AttendanceIssueDTO> selectLateList(@Param("deptIds") List<Integer> deptIds, @Param("today") LocalDate today);

	// 출근 기록도 휴가도 없는 재직자
	List<AttendanceIssueDTO> selectAbsentList(@Param("deptIds") List<Integer> deptIds, @Param("today") LocalDate today);

	/*----------------------------------------------
	 결재
	----------------------------------------------*/
	// 내가 결재자인 휴가 / 근태 정정 (대기 건)
	List<ApprovalItemDTO> selectMyApprovals(@Param("empId") int empId);

	// 내가 올렸고 아직 결재가 끝나지 않은 신청
	List<ApprovalItemDTO> selectMyRequests(@Param("empId") int empId);

	/*----------------------------------------------
	 내 현황
	----------------------------------------------*/
	MyMonthSummaryDTO selectMyMonthSummary(@Param("empId") int empId,
										   @Param("from") LocalDate from,
										   @Param("to") LocalDate to);

	BigDecimal sumMyOvertimeHours(@Param("empId") int empId,
								  @Param("from") LocalDate from,
								  @Param("to") LocalDate to);

	List<Attendance> selectMyRecentAttendances(@Param("empId") int empId, @Param("today") LocalDate today);

	List<ApprovalItemDTO> selectMyUpcomingLeaves(@Param("empId") int empId, @Param("today") LocalDate today);
}
