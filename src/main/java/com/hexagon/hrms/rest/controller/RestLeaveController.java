package com.hexagon.hrms.rest.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hexagon.hrms.domain.LeaveRequest;
import com.hexagon.hrms.dto.request.LeaveFormRequestDTO;
import com.hexagon.hrms.dto.response.LeavePlanDTO;
import com.hexagon.hrms.security.AccountDetails;
import com.hexagon.hrms.service.LeaveService;
import com.hexagon.hrms.common.web.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class RestLeaveController {

	private final LeaveService leaveService;

	@GetMapping("/plan")
	public ApiResponse<Map<String, Object>> plan(@ModelAttribute LeaveFormRequestDTO form, @AuthenticationPrincipal AccountDetails loginUser) {
		LeavePlanDTO plan = leaveService.plan(loginUser.getEmpId(), form);
		Map<String, Object> map = new HashMap<>();
		map.put("days", plan.getDays().stripTrailingZeros().toPlainString());
		map.put("deduct", plan.isDeduct());
		map.put("year", plan.getYear());
		map.put("remainDays", plan.getRemainDays() == null ? null : plan.getRemainDays().stripTrailingZeros().toPlainString());
		map.put("pendingDays", plan.getPendingDays().stripTrailingZeros().toPlainString());
		map.put("afterDays", plan.getAfterDays() == null ? null : plan.getAfterDays().stripTrailingZeros().toPlainString());
		map.put("approverName", plan.getApprover() == null ? null
			: plan.getApprover().getEname() + " " + plan.getApprover().getJob().getJobName());
		return ApiResponse.success(null, map);
	}

	@PostMapping("")
	public ApiResponse<Map<String, Object>> request(@ModelAttribute LeaveFormRequestDTO form, @AuthenticationPrincipal AccountDetails loginUser) {
		LeaveRequest leave = leaveService.request(loginUser, form);
		String message = "APPROVED".equals(leave.getStatus())
			? leave.getRequestNo() + " 휴가가 등록되었습니다. (결재자가 없어 바로 승인)"
			: leave.getRequestNo() + " 휴가를 신청했습니다. 결재자가 승인하면 확정됩니다.";
		Map<String, Object> data = new HashMap<>();
		data.put("leaveRequestId", leave.getLeaveRequestId());
		data.put("requestNo", leave.getRequestNo());
		data.put("status", leave.getStatus());
		return ApiResponse.success(message, data);
	}

	@PostMapping("/{leaveRequestId}/cancel")
	public ApiResponse<Void> cancel(@PathVariable("leaveRequestId") int leaveRequestId,
									  @AuthenticationPrincipal AccountDetails loginUser) {
		LeaveRequest leave = leaveService.cancel(leaveRequestId, loginUser);
		return ApiResponse.success(leave.getRequestNo() + " " + leave.getLeaveType().getLeaveTypeName() + " 신청을 취소했습니다.");
	}

	@PostMapping("/{leaveRequestId}/approve")
	public ApiResponse<Void> approve(@PathVariable("leaveRequestId") int leaveRequestId,
									   @AuthenticationPrincipal AccountDetails loginUser) {
		LeaveRequest leave = leaveService.approve(leaveRequestId, loginUser);
		String message = leave.getRequestNo() + " 휴가를 승인했습니다."
			+ (leave.isDeduct() ? " (연차 " + leave.getDaysText() + "일 차감)" : "");
		return ApiResponse.success(message);
	}

	@PostMapping("/{leaveRequestId}/reject")
	public ApiResponse<Void> reject(@PathVariable("leaveRequestId") int leaveRequestId,
									  @RequestParam(value = "rejectReason", required = false) String rejectReason,
									  @AuthenticationPrincipal AccountDetails loginUser) {
		LeaveRequest leave = leaveService.reject(leaveRequestId, rejectReason, loginUser);
		return ApiResponse.success(leave.getRequestNo() + " 휴가를 반려했습니다.");
	}
}
