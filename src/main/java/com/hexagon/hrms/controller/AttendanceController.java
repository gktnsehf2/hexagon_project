package com.hexagon.hrms.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hexagon.hrms.domain.Emp;
import com.hexagon.hrms.dto.response.MyAttendanceDayDTO;
import com.hexagon.hrms.dto.response.MyAttendanceMonthDTO;
import com.hexagon.hrms.security.AccountDetails;
import com.hexagon.hrms.service.AttendanceService;
import com.hexagon.hrms.service.EmpService;

import lombok.RequiredArgsConstructor;

/*
 F-09 내 근태 (S-08)                                        PERMISSION_MY_ATT_READ
   GET /attendance/my?month=2026-10             월 요약 + 일별 기록 (month 없으면 이번 달)
   GET /attendance/my/download?month=2026-10    같은 내용을 CSV 로 내려받기 (엑셀에서 열림)
*/
@Controller
@RequiredArgsConstructor
@RequestMapping("/attendance")
public class AttendanceController {

	private final AttendanceService attendanceService;
	private final EmpService empService;

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
