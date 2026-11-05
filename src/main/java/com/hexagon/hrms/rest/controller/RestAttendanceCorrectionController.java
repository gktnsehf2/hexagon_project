package com.hexagon.hrms.rest.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hexagon.hrms.domain.Attendance;
import com.hexagon.hrms.domain.AttendanceCorrection;
import com.hexagon.hrms.dto.request.AttendanceCorrectionFormDTO;
import com.hexagon.hrms.security.AccountDetails;
import com.hexagon.hrms.service.AttendanceCorrectionService;
import com.hexagon.hrms.common.web.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/attendance/corrections")
@RequiredArgsConstructor
public class RestAttendanceCorrectionController {

	private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

	private final AttendanceCorrectionService correctionService;

	@GetMapping("/record")
	public ApiResponse<Map<String, Object>> record(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
									  @AuthenticationPrincipal AccountDetails loginUser) {
		Attendance a = correctionService.getRecord(loginUser.getEmpId(), date);
		Map<String, Object> map = new HashMap<>();
		map.put("checkIn", a == null || a.getCheckInAt() == null ? null : a.getCheckInAt().format(HH_MM));
		map.put("checkOut", a == null || a.getCheckOutAt() == null ? null : a.getCheckOutAt().format(HH_MM));
		map.put("statusName", a == null ? "기록 없음" : a.getStatusName());
		return ApiResponse.success(null, map);
	}

	@PostMapping("")
	public ApiResponse<Map<String, Object>> request(@ModelAttribute AttendanceCorrectionFormDTO form, @AuthenticationPrincipal AccountDetails loginUser) {
		AttendanceCorrection c = correctionService.request(loginUser, form);
		String message = "APPROVED".equals(c.getStatus())
			? c.getRequestNo() + " 정정이 바로 반영되었습니다. (결재자 없음)"
			: c.getRequestNo() + " 근태 정정을 신청했습니다. 결재자가 승인하면 근태에 반영됩니다.";
		Map<String, Object> data = new HashMap<>();
		data.put("requestNo", c.getRequestNo());
		data.put("status", c.getStatus());
		return ApiResponse.success(message, data);
	}

	@PostMapping("/{id}/cancel")
	public ApiResponse<Void> cancel(@PathVariable("id") int id, @AuthenticationPrincipal AccountDetails loginUser) {
		AttendanceCorrection c = correctionService.cancel(id, loginUser);
		return ApiResponse.success(c.getRequestNo() + " 정정 신청을 취소했습니다.");
	}

	@PostMapping("/{id}/approve")
	public ApiResponse<Void> approve(@PathVariable("id") int id, @AuthenticationPrincipal AccountDetails loginUser) {
		AttendanceCorrection c = correctionService.approve(id, loginUser);
		return ApiResponse.success(c.getRequestNo() + " 정정을 승인했습니다. (" + c.getWorkDate() + " 출근 " + c.getCurrentInTime()
			+ " · 퇴근 " + c.getCurrentOutTime() + "로 반영)");
	}

	@PostMapping("/{id}/reject")
	public ApiResponse<Void> reject(@PathVariable("id") int id,
									  @RequestParam(value = "rejectReason", required = false) String rejectReason,
									  @AuthenticationPrincipal AccountDetails loginUser) {
		AttendanceCorrection c = correctionService.reject(id, rejectReason, loginUser);
		return ApiResponse.success(c.getRequestNo() + " 정정 신청을 반려했습니다.");
	}
}
