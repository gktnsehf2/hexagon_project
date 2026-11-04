package com.hexagon.hrms.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Collections;

import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hexagon.hrms.domain.Emp;
import com.hexagon.hrms.dto.response.AttendanceStatusDTO;
import com.hexagon.hrms.dto.response.AttendanceStatusRowDTO;
import com.hexagon.hrms.dto.response.MyAttendanceDayDTO;
import com.hexagon.hrms.dto.response.MyAttendanceMonthDTO;
import com.hexagon.hrms.security.AccountDetails;
import com.hexagon.hrms.security.PermissionCode;
import com.hexagon.hrms.service.AttendanceCorrectionService;
import com.hexagon.hrms.service.AttendanceService;
import com.hexagon.hrms.service.DepartmentService;
import com.hexagon.hrms.service.EmpScope;
import com.hexagon.hrms.service.EmpService;

import lombok.RequiredArgsConstructor;

/*
 내 근태 (S-08)                       GET /attendance/my, /my/download
 근태 현황 (S-09)                     GET /attendance/status, /status/excel      ATT_READ_TEAM / DEPT / ALL
 근태 정정 신청 (S-10)                GET /attendance/corrections?date=          ATT_CORRECT_REQUEST
 근태 결재 (S-10)                     GET /attendance/corrections/approvals      ATT_CORRECT_APPROVE
 신청·취소·승인·반려는 fetch → RestAttendanceCorrectionController
*/
@Controller
@RequiredArgsConstructor
@RequestMapping("/attendance")
public class AttendanceController {

	private final AttendanceService attendanceService;
	private final EmpService empService;
	private final DepartmentService departmentService;
	private final AttendanceCorrectionService correctionService;

	@GetMapping("/my")
	public String getMyAttendance(@RequestParam(value = "month", required = false) String month,
								  @AuthenticationPrincipal AccountDetails loginUser, Model model) {
		model.addAttribute("att", getMonth(month, loginUser));
		return "attendance/my";
	}

	@GetMapping("/my/download")
	public void download(@RequestParam(value = "month", required = false) String month,
						 @AuthenticationPrincipal AccountDetails loginUser, HttpServletResponse response) throws IOException {
		MyAttendanceMonthDTO att = getMonth(month, loginUser);

		response.setContentType("text/csv; charset=UTF-8");
		response.setHeader("Content-Disposition", "attachment; filename=\"my-attendance-" + att.getMonth() + ".csv\"");
		PrintWriter out = response.getWriter();
		out.write('﻿'); // BOM: 엑셀이 UTF-8 한글을 깨지지 않게 읽도록
		out.println("날짜,출근,퇴근,상태,근무 시간,비고");
		for (MyAttendanceDayDTO day : att.getDays()) {
			out.println(day.getWorkDate() + " " + day.getWorkDateText().substring(6) + ","
				+ day.getCheckInTime() + "," + day.getCheckOutTime() + ","
				+ day.getStatusName() + "," + day.getWorkTimeText() + "," + csv(day.getNote()));
		}
		out.println();
		out.println("정상 출근," + att.getNormalCount() + "일,지각," + att.getLateCount() + "회,조퇴," + att.getEarlyLeaveCount()
			+ "회,결근," + att.getAbsentCount() + "일,휴가," + att.getLeaveDays() + "일");
		out.flush();
	}

	// ---------------------------------------- 근태 현황
	@GetMapping("/status")
	public String getStatus(@RequestParam(value = "month", required = false) String month,
							@RequestParam(value = "departmentId", required = false) Integer departmentId,
							@RequestParam(value = "keyword", required = false) String keyword,
							@AuthenticationPrincipal AccountDetails loginUser, Model model) {
		EmpScope scope = getAttScope(loginUser);
		model.addAttribute("att", attendanceService.getStatus(toDeptIds(scope, departmentId), parseMonth(month), keyword));
		model.addAttribute("scope", scope);
		model.addAttribute("departments", empService.getSelectableDepartments(scope));
		model.addAttribute("departmentId", departmentId);
		model.addAttribute("keyword", keyword);
		return "attendance/status";
	}

	@GetMapping("/status/excel")
	public void downloadStatus(@RequestParam(value = "month", required = false) String month,
							   @RequestParam(value = "departmentId", required = false) Integer departmentId,
							   @RequestParam(value = "keyword", required = false) String keyword,
							   @AuthenticationPrincipal AccountDetails loginUser, HttpServletResponse response) throws IOException {
		AttendanceStatusDTO att = attendanceService.getStatus(toDeptIds(getAttScope(loginUser), departmentId), parseMonth(month), keyword);

		response.setContentType("text/csv; charset=UTF-8");
		response.setHeader("Content-Disposition", "attachment; filename=\"attendance-status-" + att.getMonth() + ".csv\"");
		PrintWriter out = response.getWriter();
		out.write('\uFEFF'); // BOM: 엑셀이 UTF-8 한글을 깨지지 않게 읽도록
		out.println("사번,이름,부서,직급,출근일,정상,지각,조퇴,결근,휴가,총 근무,초과근무");
		for (AttendanceStatusRowDTO r : att.getRows()) {
			out.println(String.join(",", r.getEmp().getEmpNo(), csv(r.getEmp().getEname()),
				csv(r.getEmp().getDepartment().getDepartmentName()), csv(r.getEmp().getJob().getJobName()),
				String.valueOf(r.getWorkDays()), String.valueOf(r.getNormalCount()), String.valueOf(r.getLateCount()),
				String.valueOf(r.getEarlyLeaveCount()), String.valueOf(r.getAbsentCount()), String.valueOf(r.getLeaveDays()),
				r.getTotalWorkText(), r.getOvertimeText()));
		}
		out.flush();
	}

	private EmpScope getAttScope(AccountDetails loginUser) {
		return departmentService.getScope(loginUser,
			PermissionCode.ATT_READ_ALL, PermissionCode.ATT_READ_DEPT, PermissionCode.ATT_READ_TEAM);
	}

	// 조직을 고르면 그 조직과 하위 조직 (내 범위 밖이면 빈 목록 = 아무도 안 보임)
	private Collection<Integer> toDeptIds(EmpScope scope, Integer departmentId) {
		if (departmentId == null || departmentId <= 0) {
			return scope.getDeptIds();
		}
		if (!scope.contains(departmentId)) {
			return Collections.emptyList();
		}
		return departmentService.getSubtreeIds(Collections.singletonList(departmentId));
	}

	// ---------------------------------------- 근태 정정
	@GetMapping("/corrections")
	public String getCorrections(@RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
								 @AuthenticationPrincipal AccountDetails loginUser, Model model) {
		int empId = loginUser.getEmpId();
		model.addAttribute("date", date);
		model.addAttribute("record", correctionService.getRecord(empId, date));
		model.addAttribute("approver", correctionService.findApprover(empId));
		model.addAttribute("today", correctionService.today());
		model.addAttribute("minDate", AttendanceService.RECORD_START);
		model.addAttribute("corrections", correctionService.getMyList(empId));
		return "attendance/correction";
	}

	@GetMapping("/corrections/approvals")
	public String getCorrectionApprovals(@RequestParam(value = "tab", required = false) String tab,
										 @AuthenticationPrincipal AccountDetails loginUser, Model model) {
		boolean done = "done".equals(tab);
		int empId = loginUser.getEmpId();
		model.addAttribute("tab", done ? "done" : "pending");
		model.addAttribute("pendingCount", correctionService.countApprovals(empId, false));
		model.addAttribute("doneCount", correctionService.countApprovals(empId, true));
		model.addAttribute("corrections", correctionService.getApprovals(empId, done));
		return "attendance/correction-approval";
	}

	private MyAttendanceMonthDTO getMonth(String month, AccountDetails loginUser) {
		Emp me = empService.getMe(loginUser);
		return attendanceService.getMyMonth(me.getEmpId(), me.getHiredate(), parseMonth(month));
	}

	// "2026-10" → YearMonth, 비었거나 형식이 틀리면 null (서비스가 이번 달로 맞춤)
	private YearMonth parseMonth(String month) {
		if (month == null || month.isEmpty()) {
			return null;
		}
		try {
			return YearMonth.parse(month);
		} catch (DateTimeParseException e) {
			return null;
		}
	}

	// 쉼표·따옴표가 들어 있어도 칸이 밀리지 않게
	private String csv(String value) {
		if (value == null) {
			return "";
		}
		return "\"" + value.replace("\"", "\"\"") + "\"";
	}
}
