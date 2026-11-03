package com.hexagon.hrms.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexagon.hrms.common.web.Pager;
import com.hexagon.hrms.domain.AnnualLeave;
import com.hexagon.hrms.domain.Department;
import com.hexagon.hrms.domain.Emp;
import com.hexagon.hrms.domain.Holiday;
import com.hexagon.hrms.domain.LeaveRequest;
import com.hexagon.hrms.domain.LeaveType;
import com.hexagon.hrms.dto.request.LeaveFormRequestDTO;
import com.hexagon.hrms.dto.request.LeaveSearchRequestDTO;
import com.hexagon.hrms.dto.response.LeaveStatusSummaryDTO;
import com.hexagon.hrms.dto.response.LeavePlanDTO;
import com.hexagon.hrms.exception.BusinessException;
import com.hexagon.hrms.mapper.AnnualLeaveMapper;
import com.hexagon.hrms.mapper.EmpMapper;
import com.hexagon.hrms.mapper.HolidayMapper;
import com.hexagon.hrms.mapper.LeaveRequestMapper;
import com.hexagon.hrms.mapper.LeaveTypeMapper;
import com.hexagon.hrms.security.AccountDetails;
import com.hexagon.hrms.security.PermissionCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 휴가
   F-18 휴가 신청 (S-13)
     - 사용 일수 = 기간 중 주말·공휴일을 뺀 날 수 (반차는 하루만, 0.5일)
     - 오늘 이전 날짜, 연도를 넘는 기간, 이미 대기·승인된 휴가와 겹치는 기간은 신청 불가 (같은 날 오전·오후 반차는 가능)
     - 연차·반차는 (잔여 연차 - 결재 대기 중인 연차) 안에서만 신청
     - 결재자 자동 지정: 소속 부서 조직장, 본인이 조직장이면 상위 부서 조직장 (회장은 결재자가 없어 바로 승인)
   F-21 전체 휴가 현황 (S-23): 휴가 기간이 조회 기간과 겹치는 신청 (기본: 이번 달), 조직은 하위 조직까지
   F-13 내 휴가 내역·연차 현황 (S-14)
   F-14 휴가 신청 취소: 본인이 신청한 '대기' 건만 취소 (승인된 건은 연차가 이미 차감되어 취소 불가)
   F-20 휴가 결재 (S-15): 신청 때 정해진 결재자(approver_id)만 대기 건을 승인·반려
     - 승인: 상태 APPROVED + 연차·반차면 annual_leave.used_days 증가 (잔여가 모자라면 승인 불가)
     - 반려: 상태 REJECTED + 반려 사유 필수
     - 승인된 휴가는 휴가 캘린더·내 근태·대시보드가 조회할 때 바로 반영됨
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
	private final LeaveTypeMapper leaveTypeMapper;
	private final HolidayMapper holidayMapper;
	private final EmpMapper empMapper;
	private final DepartmentService departmentService;
	private final Clock clock;

	public LocalDate today() {
		return LocalDate.now(clock);
	}

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

	// 전체 휴가 현황: 기간이 비었거나 거꾸로면 이번 달로 맞춤 (화면에 맞춘 값이 그대로 보이도록 search 를 고침)
	public void normalize(LeaveSearchRequestDTO search) {
		LocalDate today = LocalDate.now(clock);
		if (search.getFrom() == null) {
			search.setFrom(today.withDayOfMonth(1));
		}
		if (search.getTo() == null) {
			search.setTo(search.getFrom().withDayOfMonth(search.getFrom().lengthOfMonth()));
		}
		if (search.getTo().isBefore(search.getFrom())) {
			LocalDate from = search.getTo();
			search.setTo(search.getFrom());
			search.setFrom(from);
		}
	}

	@Transactional(readOnly = true)
	public Pager getStatusPage(LeaveSearchRequestDTO search) {
		normalize(search);
		List<Integer> deptIds = toDeptIds(search);
		Pager pager = new Pager(search.getCurrentPage(), leaveRequestMapper.selectStatusCount(search, deptIds));
		pager.setList(leaveRequestMapper.selectStatusList(search, deptIds, pager));
		return pager;
	}

	@Transactional(readOnly = true)
	public LeaveStatusSummaryDTO getStatusSummary(LeaveSearchRequestDTO search) {
		normalize(search);
		return leaveRequestMapper.selectStatusSummary(search, toDeptIds(search));
	}

	// 엑셀(CSV) 다운로드용: 페이지 없이 전부
	@Transactional(readOnly = true)
	public List<LeaveRequest> getStatusAll(LeaveSearchRequestDTO search) {
		normalize(search);
		return leaveRequestMapper.selectStatusList(search, toDeptIds(search), null);
	}

	private List<Integer> toDeptIds(LeaveSearchRequestDTO search) {
		if (search.getDepartmentId() == null || search.getDepartmentId() <= 0) {
			return null; // 전사
		}
		return departmentService.getSubtreeIds(Collections.singletonList(search.getDepartmentId()));
	}

	// 신청 화면 선택지
	@Transactional(readOnly = true)
	public List<LeaveType> getActiveTypes() {
		return leaveTypeMapper.selectActive();
	}

	// 그해 결재 대기 중인 연차·반차 일수
	@Transactional(readOnly = true)
	public BigDecimal getPendingDeductDays(int empId, int year) {
		return leaveRequestMapper.sumPendingDeductDays(empId, year);
	}

	// 결재자 자동 지정: 소속 조직의 조직장, 본인이 조직장이면 위로 올라가며 찾음 (없으면 null = 회장)
	@Transactional(readOnly = true)
	public Emp findApprover(int empId) {
		Emp me = empMapper.select(empId);
		if (me == null || me.getDepartment() == null) {
			return null;
		}
		Map<Integer, Department> depts = new HashMap<>();
		for (Department d : departmentService.getAll()) {
			depts.put(d.getDepartmentId(), d);
		}
		Department dept = depts.get(me.getDepartment().getDepartmentId());
		while (dept != null) {
			Integer managerId = dept.getManagerId();
			if (managerId != null && managerId != empId) {
				return empMapper.select(managerId);
			}
			dept = dept.getParentId() == null ? null : depts.get(dept.getParentId());
		}
		return null;
	}

	/*
	 신청 내용 검사 + 일수·잔여·결재자 계산 (미리보기와 신청에서 같이 사용)
	 규칙에 맞지 않으면 BusinessException (화면에 그 문구가 그대로 보임)
	*/
	@Transactional(readOnly = true)
	public LeavePlanDTO plan(int empId, LeaveFormRequestDTO form) {
		LeaveType type = form.getLeaveTypeId() == null ? null : leaveTypeMapper.select(form.getLeaveTypeId());
		if (type == null || type.getIsActive() != 1) {
			throw new BusinessException("휴가 유형을 선택하세요.");
		}
		LocalDate start = form.getStartDate();
		LocalDate end = type.isHalfDay() ? start : form.getEndDate();
		if (start == null || end == null) {
			throw new BusinessException(type.isHalfDay() ? "반차 날짜를 선택하세요." : "시작일과 종료일을 선택하세요.");
		}
		if (end.isBefore(start)) {
			throw new BusinessException("종료일이 시작일보다 빠릅니다.");
		}
		if (start.isBefore(LocalDate.now(clock))) {
			throw new BusinessException("오늘 이전 날짜는 신청할 수 없습니다.");
		}
		if (start.getYear() != end.getYear()) {
			throw new BusinessException("연도가 바뀌는 휴가는 연도별로 나눠서 신청하세요.");
		}

		// 사용 일수: 주말·공휴일 제외
		Set<LocalDate> holidays = new HashSet<>();
		for (Holiday holiday : holidayMapper.selectBetween(start, end)) {
			holidays.add(holiday.getHolidayDate());
		}
		int workDays = 0;
		for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
			if (d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY && !holidays.contains(d)) {
				workDays++;
			}
		}
		if (workDays == 0) {
			throw new BusinessException("선택한 기간에 근무일이 없습니다. (주말·공휴일만 있음)");
		}
		BigDecimal days = type.isHalfDay() ? new BigDecimal("0.5") : BigDecimal.valueOf(workDays);

		// 겹치는 휴가: 같은 날 오전 반차 + 오후 반차만 허용
		for (LeaveRequest other : leaveRequestMapper.selectActiveOverlaps(empId, start, end)) {
			boolean otherHalf = other.getLeaveType().isHalfDay();
			boolean otherHalfDifferent = otherHalf && !other.getLeaveType().getLeaveTypeCode().equals(type.getLeaveTypeCode());
			if (!(type.isHalfDay() && otherHalfDifferent)) {
				throw new BusinessException(other.getPeriodText() + "에 이미 " + other.getStatusName() + " 상태인 "
					+ other.getLeaveType().getLeaveTypeName() + " 신청(" + other.getRequestNo() + ")이 있습니다.");
			}
		}

		LeavePlanDTO plan = new LeavePlanDTO();
		plan.setLeaveType(type);
		plan.setStartDate(start);
		plan.setEndDate(end);
		plan.setDays(days);
		plan.setYear(start.getYear());
		AnnualLeave annual = annualLeaveMapper.select(empId, start.getYear());
		plan.setRemainDays(annual == null ? null : annual.getRemainDays());
		plan.setPendingDays(leaveRequestMapper.sumPendingDeductDays(empId, start.getYear()));
		plan.setApprover(findApprover(empId));

		if (plan.isDeduct()) {
			if (annual == null) {
				throw new BusinessException(start.getYear() + "년 연차가 아직 부여되지 않아 연차·반차를 신청할 수 없습니다.");
			}
			if (plan.getAfterDays().signum() < 0) {
				throw new BusinessException("잔여 연차가 부족합니다. (잔여 " + strip(plan.getRemainDays())
					+ "일, 결재 대기 " + strip(plan.getPendingDays()) + "일, 이번 신청 " + strip(days) + "일)");
			}
		}
		return plan;
	}

	// 휴가 신청 저장 (결재자가 없으면 = 회장 본인 신청이면 바로 승인하고 연차 차감)
	public LeaveRequest request(AccountDetails loginUser, LeaveFormRequestDTO form) {
		if (!loginUser.hasAuthority(PermissionCode.LEAVE_REQUEST)) {
			throw new BusinessException("휴가 신청 권한이 없습니다.");
		}
		String reason = form.getReason() == null ? "" : form.getReason().trim();
		if (reason.length() > 200) {
			throw new BusinessException("사유는 200자까지 입력할 수 있습니다.");
		}
		int empId = loginUser.getEmpId();
		LeavePlanDTO plan = plan(empId, form);
		LocalDateTime now = LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS);

		LeaveRequest leave = new LeaveRequest();
		Emp emp = new Emp();
		emp.setEmpId(empId);
		leave.setEmp(emp);
		leave.setLeaveType(plan.getLeaveType());
		leave.setStartDate(plan.getStartDate());
		leave.setEndDate(plan.getEndDate());
		leave.setDays(plan.getDays());
		leave.setReason(reason.isEmpty() ? null : reason);
		leave.setRequestDate(now);
		leave.setApprover(plan.getApprover());
		if (plan.getApprover() == null) {
			leave.setStatus("APPROVED");
			leave.setApprovedAt(now);
		}
		leaveRequestMapper.insert(leave);

		if (plan.getApprover() == null && plan.isDeduct()
				&& annualLeaveMapper.addUsedDays(empId, plan.getYear(), plan.getDays()) == 0) {
			throw new BusinessException("잔여 연차가 부족합니다.");
		}
		if (leave.getStatus() == null) {
			leave.setStatus("REQUESTED");
		}
		log.info("휴가 신청 {} empId={} {} {}일 → {}", leave.getRequestNo(), empId, plan.getLeaveType().getLeaveTypeCode(),
			plan.getDays(), plan.getApprover() == null ? "결재자 없음(바로 승인)" : "결재자 empId=" + plan.getApprover().getEmpId());
		return leave;
	}

	private static String strip(BigDecimal value) {
		return value == null ? "0" : value.stripTrailingZeros().toPlainString();
	}

	// 내 결재 목록 (done = false 대기 / true 처리 완료)
	@Transactional(readOnly = true)
	public List<LeaveRequest> getApprovals(int approverId, boolean done) {
		return leaveRequestMapper.selectApprovalList(approverId, done);
	}

	@Transactional(readOnly = true)
	public int countApprovals(int approverId, boolean done) {
		return leaveRequestMapper.countApprovals(approverId, done);
	}

	// 승인
	public LeaveRequest approve(int leaveRequestId, AccountDetails loginUser) {
		LeaveRequest leave = getMyApproval(leaveRequestId, loginUser);
		LocalDateTime now = LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS);

		if (leaveRequestMapper.approve(leaveRequestId, loginUser.getEmpId(), now) == 0) {
			throw new BusinessException("방금 다른 처리가 되어 승인할 수 없습니다. 새로고침 후 확인해 주세요.");
		}
		// 연차·반차는 신청 시작일이 속한 해의 연차에서 차감 (실패하면 위의 승인도 rollback)
		if (leave.isDeduct()) {
			int year = leave.getStartDate().getYear();
			if (annualLeaveMapper.addUsedDays(leave.getEmp().getEmpId(), year, leave.getDays()) == 0) {
				throw new BusinessException("신청자의 " + year + "년 잔여 연차가 부족하거나 연차 부여 내역이 없어 승인할 수 없습니다.");
			}
		}
		leave.setStatus("APPROVED");
		leave.setApprovedAt(now);
		log.info("휴가 승인 {} by empId={}", leave.getRequestNo(), loginUser.getEmpId());
		return leave;
	}

	// 반려 (사유 필수)
	public LeaveRequest reject(int leaveRequestId, String rejectReason, AccountDetails loginUser) {
		String reason = rejectReason == null ? "" : rejectReason.trim();
		if (reason.isEmpty()) {
			throw new BusinessException("반려 사유를 입력하세요.");
		}
		if (reason.length() > 200) {
			throw new BusinessException("반려 사유는 200자까지 입력할 수 있습니다.");
		}
		LeaveRequest leave = getMyApproval(leaveRequestId, loginUser);
		LocalDateTime now = LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS);
		if (leaveRequestMapper.reject(leaveRequestId, loginUser.getEmpId(), reason, now) == 0) {
			throw new BusinessException("방금 다른 처리가 되어 반려할 수 없습니다. 새로고침 후 확인해 주세요.");
		}
		leave.setStatus("REJECTED");
		leave.setRejectReason(reason);
		log.info("휴가 반려 {} by empId={}", leave.getRequestNo(), loginUser.getEmpId());
		return leave;
	}

	// 내가 결재자인 대기 건인지 확인 (URL 권한과 별개로 서비스에서도 검사)
	private LeaveRequest getMyApproval(int leaveRequestId, AccountDetails loginUser) {
		if (!loginUser.hasAuthority(PermissionCode.LEAVE_APPROVE)) {
			throw new BusinessException("휴가 결재 권한이 없습니다.");
		}
		LeaveRequest leave = leaveRequestMapper.select(leaveRequestId);
		if (leave == null || leave.getApprover() == null || leave.getApprover().getEmpId() != loginUser.getEmpId()) {
			throw new BusinessException("내가 결재자로 지정된 휴가만 처리할 수 있습니다.");
		}
		if (!leave.isCancelable()) {
			throw new BusinessException("이미 " + leave.getStatusName() + " 처리된 휴가입니다.");
		}
		return leave;
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
