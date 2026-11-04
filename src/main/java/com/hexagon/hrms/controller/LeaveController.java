package com.hexagon.hrms.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hexagon.hrms.domain.Emp;
import com.hexagon.hrms.domain.LeaveRequest;
import com.hexagon.hrms.dto.request.LeaveSearchRequestDTO;
import com.hexagon.hrms.security.AccountDetails;
import com.hexagon.hrms.service.DepartmentService;
import com.hexagon.hrms.service.EmpService;
import com.hexagon.hrms.service.LeaveService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/leaves")
public class LeaveController {

	private static final int YEAR_OPTIONS = 5; // 연도 선택 최근 5년까지

	private final LeaveService leaveService;
	private final EmpService empService;
	private final DepartmentService departmentService;

	@GetMapping("/my")
	public String getMyLeaves(@RequestParam(value = "year", required = false) Integer year,
			@RequestParam(value = "status", required = false) String status,
			@AuthenticationPrincipal AccountDetails loginUser, Model model) {
		Emp me = empService.getMe(loginUser);
		int thisYear = leaveService.thisYear();
		int firstYear = Math.max(me.getHiredate().getYear(), thisYear - YEAR_OPTIONS + 1);
		if (year == null || year > thisYear || year < firstYear) {
			year = thisYear;
		}

		List<LeaveRequest> all = leaveService.getMyLeaves(me.getEmpId(), year);
		List<Integer> years = new ArrayList<>();
		for (int y = thisYear; y >= firstYear; y--) {
			years.add(y);
		}

		model.addAttribute("year", year);
		model.addAttribute("years", years);
		model.addAttribute("status", status == null ? "" : status);
		model.addAttribute("annualLeave", leaveService.getAnnualLeave(me.getEmpId(), year));
		model.addAttribute("pendingDays", leaveService.sumPendingDeductDays(all));
		model.addAttribute("totalCount", all.size());
		model.addAttribute("statusCounts", leaveService.countByStatus(all));
		model.addAttribute("leaves", leaveService.filter(all, status));
		return "leave/my";
	}

	@GetMapping("/new")
	public String requestForm(@AuthenticationPrincipal AccountDetails loginUser, Model model) {
		int empId = loginUser.getEmpId();
		int year = leaveService.thisYear();
		model.addAttribute("leaveTypes", leaveService.getActiveTypes());
		model.addAttribute("annualLeave", leaveService.getAnnualLeave(empId, year));
		model.addAttribute("pendingDays", leaveService.getPendingDeductDays(empId, year));
		model.addAttribute("approver", leaveService.findApprover(empId));
		model.addAttribute("year", year);
		model.addAttribute("today", leaveService.today());
		return "leave/form";
	}

	// 전체 휴가 현황 (PERMISSION_LEAVE_READ_ALL)
	@GetMapping("/status")
	public String getStatus(@ModelAttribute("search") LeaveSearchRequestDTO search, Model model) {
		model.addAttribute("pager", leaveService.getStatusPage(search));
		model.addAttribute("summary", leaveService.getStatusSummary(search));
		model.addAttribute("departments", departmentService.getAll());
		model.addAttribute("leaveTypes", leaveService.getActiveTypes());
		return "leave/status";
	}

	// 같은 조건의 전부를 CSV 로 (엑셀에서 열림)
	@GetMapping("/status/excel")
	public void downloadStatus(@ModelAttribute("search") LeaveSearchRequestDTO search, HttpServletResponse response) throws IOException {
		List<LeaveRequest> leaves = leaveService.getStatusAll(search);

		response.setContentType("text/csv; charset=UTF-8");
		response.setHeader("Content-Disposition", "attachment; filename=\"leave-status-" + search.getFrom() + "_" + search.getTo() + ".csv\"");
		PrintWriter out = response.getWriter();
		out.write('\uFEFF'); // BOM: 엑셀이 UTF-8 한글을 깨지지 않게 읽도록
		out.println("신청번호,사번,이름,부서,직급,휴가 유형,시작일,종료일,일수,사유,결재자,신청일,상태,처리일,반려 사유");
		for (LeaveRequest l : leaves) {
			out.println(String.join(",",
				l.getRequestNo(), l.getEmp().getEmpNo(), csv(l.getEmp().getEname()),
				csv(l.getEmp().getDepartment().getDepartmentName()), csv(l.getEmp().getJob().getJobName()),
				csv(l.getLeaveType().getLeaveTypeName()), l.getStartDate().toString(), l.getEndDate().toString(),
				l.getDaysText(), csv(l.getReason()), csv(l.getApprover() == null ? "" : l.getApprover().getEname()),
				l.getRequestDateText(), l.getStatusName(), l.getApprovedAtText(), csv(l.getRejectReason())));
		}
		out.flush();
	}

	// 쉼표·따옴표가 들어 있어도 칸이 밀리지 않게
	private String csv(String value) {
		return value == null ? "" : "\"" + value.replace("\"", "\"\"") + "\"";
	}

	@GetMapping("/approvals")
	public String getApprovals(@RequestParam(value = "tab", required = false) String tab,
			@AuthenticationPrincipal AccountDetails loginUser, Model model) {
		boolean done = "done".equals(tab);
		int empId = loginUser.getEmpId();
		model.addAttribute("tab", done ? "done" : "pending");
		model.addAttribute("pendingCount", leaveService.countApprovals(empId, false));
		model.addAttribute("doneCount", leaveService.countApprovals(empId, true));
		model.addAttribute("leaves", leaveService.getApprovals(empId, done));
		return "leave/approval";
	}
}
