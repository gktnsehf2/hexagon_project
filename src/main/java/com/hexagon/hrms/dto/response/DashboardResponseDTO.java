package com.hexagon.hrms.dto.response;

import java.math.BigDecimal;
import java.util.List;

import com.hexagon.hrms.domain.AnnualLeave;
import com.hexagon.hrms.domain.Attendance;
import com.hexagon.hrms.service.EmpScope;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class DashboardResponseDTO {

	// 공통
	private long serverTime;              
	private TodayAttendanceDTO myToday;

	private EmpScope scope;
	private int activeCount;            
	private int checkedInCount;               
	private int lateCount;
	private int absentCount;
	private List<AttendanceIssueDTO> issues;  
	private List<TodayLeaveDTO> todayLeaves;
	private String leaveSummary;          

	// 내 현황
	private MyMonthSummaryDTO myMonth;
	private AnnualLeave annualLeave;          
	private BigDecimal myOvertimeHours;     
	private List<Attendance> myAttendances;   
	private List<ApprovalItemDTO> myLeaves;   
	
	// 결재
	private boolean approver;                
	private List<ApprovalItemDTO> requests;

	public boolean isTeamView() {
		return scope != null;
	}
}
