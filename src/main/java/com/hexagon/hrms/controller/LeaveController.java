package com.hexagon.hrms.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hexagon.hrms.domain.Emp;
import com.hexagon.hrms.domain.LeaveRequest;
import com.hexagon.hrms.security.AccountDetails;
import com.hexagon.hrms.service.EmpService;
import com.hexagon.hrms.service.LeaveService;

import lombok.RequiredArgsConstructor;

/*
 F-13 내 휴가 내역 · 연차 현황 (S-14)                       PERMISSION_MY_LEAVE_READ
   GET /leaves/my?year=2026&status=REQUESTED    연차 카드 + 신청 내역 (year 없으면 올해, status 없으면 전체)
 취소(F-14)는 목록의 취소 버튼에서 fetch 로 부름 → RestLeaveController
*/
@Controller
@RequiredArgsConstructor
@RequestMapping("/leaves")
public class LeaveController {

	private static final int YEAR_OPTIONS = 5; // 연도 선택은 최근 5년까지

	private final LeaveService leaveService;
	private final EmpService empService;

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
}
