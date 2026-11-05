package com.hexagon.hrms.rest.controller;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hexagon.hrms.domain.Attendance;
import com.hexagon.hrms.security.AccountDetails;
import com.hexagon.hrms.service.AttendanceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class RestAttendanceController {

	private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

	private final AttendanceService attendanceService;

	@PostMapping("/check-in")
	public Map<String, Object> checkIn(@AuthenticationPrincipal AccountDetails loginUser) {
		Attendance attendance = attendanceService.checkIn(loginUser);
		String message = attendance.getCheckInAt().format(HH_MM) + " 출근 처리되었습니다."
			+ ("LATE".equals(attendance.getStatus()) ? " (지각)" : "");
		return result(message, attendance.getStatus());
	}

	@PostMapping("/check-out")
	public Map<String, Object> checkOut(@AuthenticationPrincipal AccountDetails loginUser) {
		Attendance attendance = attendanceService.checkOut(loginUser);
		String message = attendance.getCheckOutAt().format(HH_MM) + " 퇴근 처리되었습니다."
			+ ("EARLY_LEAVE".equals(attendance.getStatus()) ? " (조퇴)" : "");
		return result(message, attendance.getStatus());
	}

	private Map<String, Object> result(String message, String status) {
		Map<String, Object> map = new HashMap<>();
		map.put("result", "success");
		map.put("message", message);
		map.put("status", status);
		return map;
	}
}
