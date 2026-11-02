package com.hexagon.hrms.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexagon.hrms.domain.AnnualLeave;
import com.hexagon.hrms.domain.LeaveRequest;
import com.hexagon.hrms.exception.BusinessException;
import com.hexagon.hrms.mapper.AnnualLeaveMapper;
import com.hexagon.hrms.mapper.LeaveRequestMapper;
import com.hexagon.hrms.security.AccountDetails;
import com.hexagon.hrms.security.PermissionCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 휴가
   F-13 내 휴가 내역·연차 현황 (S-14)
   F-14 휴가 신청 취소: 본인이 신청한 '대기' 건만 취소 (승인된 건은 연차가 이미 차감되어 취소 불가)
*/
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class LeaveService {

	// 상태 필터 순서 (화면 탭)
	public static final String[] STATUSES = { "REQUESTED", "APPROVED", "REJECTED", "CANCELED" };

	private final LeaveRequestMapper leaveRequestMapper;
	private final AnnualLeaveMapper annualLeaveMapper;
	private final Clock clock;

	public int thisYear() {
		return LocalDate.now(clock).getYear();
	}

	// 해당 연도 연차 (부여 전이면 null)
	@Transactional(readOnly = true)
	public AnnualLeave getAnnualLeave(int empId, int year) {
		return annualLeaveMapper.select(empId, year);
	}

	// 해당 연도와 겹치는 내 휴가 신청 전부 (최근 신청 순)
	@Transactional(readOnly = true)
	public List<LeaveRequest> getMyLeaves(int empId, int year) {
		return leaveRequestMapper.selectMyList(empId, LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));
	}

	// 상태로 거르기 (status 가 비어 있으면 전체)
	public List<LeaveRequest> filter(List<LeaveRequest> leaves, String status) {
		if (status == null || status.isEmpty()) {
			return leaves;
		}
		List<LeaveRequest> list = new ArrayList<>();
		for (LeaveRequest leave : leaves) {
			if (status.equals(leave.getStatus())) {
				list.add(leave);
			}
		}
		return list;
	}

	// 상태별 건수 (탭에 표시)
	public Map<String, Integer> countByStatus(List<LeaveRequest> leaves) {
		Map<String, Integer> map = new LinkedHashMap<>();
		for (String status : STATUSES) {
			map.put(status, 0);
		}
		for (LeaveRequest leave : leaves) {
			map.merge(leave.getStatus(), 1, Integer::sum);
		}
		return map;
	}

	// 결재 대기 중인 연차 차감 일수 (승인되면 잔여 연차에서 빠질 예정인 일수)
	public BigDecimal sumPendingDeductDays(List<LeaveRequest> leaves) {
		BigDecimal sum = BigDecimal.ZERO;
		for (LeaveRequest leave : leaves) {
			if (leave.isCancelable() && leave.getLeaveType().getIsDeduct() == 1) {
				sum = sum.add(leave.getDays());
			}
		}
		return sum;
	}

	// 휴가 신청 취소 (본인 + 대기 중인 건만)
	public LeaveRequest cancel(int leaveRequestId, AccountDetails loginUser) {
		if (!loginUser.hasAuthority(PermissionCode.LEAVE_CANCEL)) {
			throw new BusinessException("휴가 취소 권한이 없습니다.");
		}
		LeaveRequest leave = leaveRequestMapper.select(leaveRequestId);
		if (leave == null || leave.getEmp().getEmpId() != loginUser.getEmpId()) {
			throw new BusinessException("본인이 신청한 휴가만 취소할 수 있습니다.");
		}
		if (!leave.isCancelable()) {
			throw new BusinessException("이미 " + leave.getStatusName() + " 처리된 휴가는 취소할 수 없습니다.");
		}
		// 조회와 취소 사이에 결재자가 먼저 처리한 경우 0 건
		if (leaveRequestMapper.cancel(leaveRequestId, loginUser.getEmpId()) == 0) {
			throw new BusinessException("방금 결재가 처리되어 취소할 수 없습니다. 새로고침 후 확인해 주세요.");
		}
		leave.setStatus("CANCELED");
		log.debug("휴가 취소 empId={} leaveRequestId={}", loginUser.getEmpId(), leaveRequestId);
		return leave;
	}
}
