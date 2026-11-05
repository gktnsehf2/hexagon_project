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
		if (now.toLocalTime().isAfter(AttendanceService.WORK_START)) {
			issues.addAll(dashboardMapper.selectAbsentList(deptIds, today));
		}
		dash.setIssues(issues);
		dash.setLateCount(lateCount);
		dash.setAbsentCount(issues.size() - lateCount);
	}

	private void fillMyStatus(DashboardResponseDTO dash, int empId, LocalDate today) {
		LocalDate monthStart = today.withDayOfMonth(1);
		LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());

		dash.setMyMonth(dashboardMapper.selectMyMonthSummary(empId, monthStart, today));
		dash.setAnnualLeave(annualLeaveMapper.select(empId, today.getYear()));
		dash.setMyOvertimeHours(dashboardMapper.sumMyOvertimeHours(empId, monthStart, monthEnd));
		dash.setMyAttendances(dashboardMapper.selectMyRecentAttendances(empId, today));
		dash.setMyLeaves(dashboardMapper.selectMyUpcomingLeaves(empId, today));
	}

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
