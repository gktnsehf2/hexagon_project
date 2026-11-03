package com.hexagon.hrms.mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.hexagon.hrms.common.web.Pager;
import com.hexagon.hrms.domain.LeaveRequest;
import com.hexagon.hrms.domain.LeaveType;
import com.hexagon.hrms.dto.request.LeaveSearchRequestDTO;
import com.hexagon.hrms.dto.response.LeaveStatusSummaryDTO;

@Mapper
public interface LeaveRequestMapper {
	// 그날 승인된 휴가의 유형 (오전 반차 + 오후 반차처럼 여러 건일 수 있음)
	List<LeaveType> selectApprovedTypesOn(@Param("empId") int empId, @Param("date") LocalDate date);

	// 기간과 겹치는 승인된 휴가 (내 근태 월 조회)
	List<LeaveRequest> selectApprovedBetween(@Param("empId") int empId, @Param("from") LocalDate from, @Param("to") LocalDate to);

	// 내 휴가 신청 내역 (기간과 겹치는 건, 최근 신청 순)
	List<LeaveRequest> selectMyList(@Param("empId") int empId, @Param("from") LocalDate from, @Param("to") LocalDate to);

	// 기간과 겹치는 대기·승인 휴가 (중복 신청 막기)
	List<LeaveRequest> selectActiveOverlaps(@Param("empId") int empId, @Param("from") LocalDate from, @Param("to") LocalDate to);

	// 그해 결재 대기 중인 연차·반차 일수 합 (잔여 연차 초과 신청 막기)
	BigDecimal sumPendingDeductDays(@Param("empId") int empId, @Param("year") int year);

	// 신청 (status 가 null 이면 REQUESTED)
	int insert(LeaveRequest leaveRequest);

	// 전체 휴가 현황 (deptIds 가 null 이면 전사, pager 가 null 이면 전부 = 엑셀)
	List<LeaveRequest> selectStatusList(@Param("search") LeaveSearchRequestDTO search,
										@Param("deptIds") Collection<Integer> deptIds, @Param("pager") Pager pager);

	int selectStatusCount(@Param("search") LeaveSearchRequestDTO search, @Param("deptIds") Collection<Integer> deptIds);

	// 상태별 건수 (상태 조건은 무시)
	LeaveStatusSummaryDTO selectStatusSummary(@Param("search") LeaveSearchRequestDTO search,
											  @Param("deptIds") Collection<Integer> deptIds);

	// 한 건 (없으면 null)
	LeaveRequest select(int leaveRequestId);

	// 내가 결재자인 휴가 (done = false 면 대기 중, true 면 내가 승인·반려한 건 최근 순 100건)
	List<LeaveRequest> selectApprovalList(@Param("approverId") int approverId, @Param("done") boolean done);

	int countApprovals(@Param("approverId") int approverId, @Param("done") boolean done);

	// 승인 / 반려 (지정된 결재자 + 대기 중인 건만 바뀜 → 0 이면 이미 처리됨)
	int approve(@Param("leaveRequestId") int leaveRequestId, @Param("approverId") int approverId, @Param("now") LocalDateTime now);

	int reject(@Param("leaveRequestId") int leaveRequestId, @Param("approverId") int approverId,
			   @Param("rejectReason") String rejectReason, @Param("now") LocalDateTime now);

	// 본인 신청 취소 (대기 중인 건만 바뀜 → 0 이면 이미 처리됨)
	int cancel(@Param("leaveRequestId") int leaveRequestId, @Param("empId") int empId);
}
