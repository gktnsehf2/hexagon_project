package com.hexagon.hrms.dto.response;

import java.math.BigDecimal;
import java.util.List;

import com.hexagon.hrms.domain.AnnualLeave;
import com.hexagon.hrms.domain.Attendance;
import com.hexagon.hrms.service.EmpScope;

import lombok.Getter;
import lombok.Setter;

/*
 메인 대시보드 화면 데이터
 근태 현황 권한(ATT_READ_TEAM / DEPT / ALL)이 있으면 "조직 현황", 없으면 "내 현황"을 채운다
*/
@Getter
@Setter
public class DashboardResponseDTO {

	// 공통
	private long serverTime;              // 시계를 서버 시각에 맞추기 위한 값 (1970-01-01 기준 ms)
	private TodayAttendanceDTO myToday;   // 내 오늘 출퇴근

	// 조직 현황 (scope 가 null 이면 "내 현황" 화면)
	private EmpScope scope;
	private int activeCount;                  // 재직 인원
	private int checkedInCount;               // 오늘 출근
	private int lateCount;
	private int absentCount;
	private List<AttendanceIssueDTO> issues;  // 지각 + 결근
	private List<TodayLeaveDTO> todayLeaves;
	private String leaveSummary;              // 예) 연차 1명 | 오전 반차 1명

	// 내 현황
	private MyMonthSummaryDTO myMonth;
	private AnnualLeave annualLeave;          // 올해 연차 (없으면 null)
	private BigDecimal myOvertimeHours;       // 이번 달 초과근무 (18:00 이후 퇴근 시간 합계)
	private List<Attendance> myAttendances;   // 최근 근태
	private List<ApprovalItemDTO> myLeaves;   // 예정된 휴가

	// 결재
	private boolean approver;                 // 결재 권한이 있으면 "내 결재 대기", 없으면 "진행 중인 내 신청"
	private List<ApprovalItemDTO> requests;

	public boolean isTeamView() {
		return scope != null;
	}
}
