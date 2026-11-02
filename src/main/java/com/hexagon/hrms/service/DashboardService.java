package com.hexagon.hrms.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexagon.hrms.dto.response.AttendanceIssueDTO;
import com.hexagon.hrms.dto.response.DashboardResponseDTO;
import com.hexagon.hrms.dto.response.TodayLeaveDTO;
import com.hexagon.hrms.mapper.AnnualLeaveMapper;
import com.hexagon.hrms.mapper.DashboardMapper;
import com.hexagon.hrms.security.AccountDetails;
import com.hexagon.hrms.security.PermissionCode;

import lombok.RequiredArgsConstructor;

/*
 메인 대시보드
 - 근태 현황 권한(ATT_READ_TEAM / DEPT / ALL)이 있으면 그 범위의 오늘 현황 (재직·출근·지각/결근·휴가)
 - 없으면 내 현황 (이번 달 출근, 잔여 연차, 초과근무, 최근 근태, 예정된 휴가)
 - 결재 권한이 있으면 "내 결재 대기", 없으면 "진행 중인 내 신청"
*/
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DashboardService {

	private final DashboardMapper dashboardMapper;
	private final AnnualLeaveMapper annualLeaveMapper;
	private final DepartmentService departmentService;
	private final AttendanceService attendanceService;
	private final Clock clock;

	public DashboardResponseDTO getDashboard(AccountDetails loginUser) {
		LocalDateTime now = LocalDateTime.now(clock);
		int empId = loginUser.getEmpId();

		DashboardResponseDTO dash = new DashboardResponseDTO();
		dash.setServerTime(clock.millis());
		dash.setMyToday(attendanceService.getMyToday(empId));

		EmpScope scope = departmentService.getScope(loginUser,
			PermissionCode.ATT_READ_ALL, PermissionCode.ATT_READ_DEPT, PermissionCode.ATT_READ_TEAM);
		if (scope.isVisible()) {
			fillTeamStatus(dash, scope, now);
		} else {
			fillMyStatus(dash, empId, now.toLocalDate());
		}

		boolean approver = loginUser.hasAuthority(PermissionCode.LEAVE_APPROVE)
			|| loginUser.hasAuthority(PermissionCode.ATT_CORRECT_APPROVE);
		dash.setApprover(approver);
		dash.setRequests(approver ? dashboardMapper.selectMyApprovals(empId) : dashboardMapper.selectMyRequests(empId));
		return dash;
	}

	// 조직 현황 (오늘)
	private void fillTeamStatus(DashboardResponseDTO dash, EmpScope scope, LocalDateTime now) {
		List<Integer> deptIds = scope.getDeptIds();
		LocalDate today = now.toLocalDate();

		dash.setScope(scope);
		dash.setActiveCount(dashboardMapper.countActiveEmp(deptIds));
		dash.setCheckedInCount(dashboardMapper.countCheckedIn(deptIds, today));

		List<TodayLeaveDTO> leaves = dashboardMapper.selectTodayLeaves(deptIds, today);
		dash.setTodayLeaves(leaves);
		dash.setLeaveSummary(makeLeaveSummary(leaves));

		List<AttendanceIssueDTO> issues = new ArrayList<>(dashboardMapper.selectLateList(deptIds, today));
		int lateCount = issues.size();
		// 출근 시각(09:00)이 지났는데 기록도 휴가도 없으면 결근으로 본다
		if (now.toLocalTime().isAfter(AttendanceService.WORK_START)) {
			issues.addAll(dashboardMapper.selectAbsentList(deptIds, today));
		}
		dash.setIssues(issues);
		dash.setLateCount(lateCount);
		dash.setAbsentCount(issues.size() - lateCount);
	}

	// 내 현황 (이번 달)
	private void fillMyStatus(DashboardResponseDTO dash, int empId, LocalDate today) {
		LocalDate monthStart = today.withDayOfMonth(1);
		LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());

		dash.setMyMonth(dashboardMapper.selectMyMonthSummary(empId, monthStart, today));
		dash.setAnnualLeave(annualLeaveMapper.select(empId, today.getYear()));
		// 초과근무 신청 기능은 없으므로 18:00 이후 퇴근한 시간을 합산해서 보여준다
		dash.setMyOvertimeHours(dashboardMapper.sumMyOvertimeHours(empId, monthStart, monthEnd));
		dash.setMyAttendances(dashboardMapper.selectMyRecentAttendances(empId, today));
		dash.setMyLeaves(dashboardMapper.selectMyUpcomingLeaves(empId, today));
	}

	// 예) 연차 2명 | 오전 반차 1명
	private String makeLeaveSummary(List<TodayLeaveDTO> leaves) {
		Map<String, Integer> counts = new LinkedHashMap<>();
		for (TodayLeaveDTO leave : leaves) {
			counts.merge(leave.getLeaveTypeName(), 1, Integer::sum);
		}
		List<String> parts = new ArrayList<>();
		for (Map.Entry<String, Integer> entry : counts.entrySet()) {
			parts.add(entry.getKey() + " " + entry.getValue() + "명");
		}
		return parts.isEmpty() ? "휴가자 없음" : String.join(" | ", parts);
	}
}
