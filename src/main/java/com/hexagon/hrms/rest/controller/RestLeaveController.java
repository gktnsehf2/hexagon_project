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

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class RestLeaveController {

	private final LeaveService leaveService;

	@GetMapping("/plan")
	public Map<String, Object> plan(@ModelAttribute LeaveFormRequestDTO form, @AuthenticationPrincipal AccountDetails loginUser) {
		LeavePlanDTO plan = leaveService.plan(loginUser.getEmpId(), form);
		Map<String, Object> map = new HashMap<>();
		map.put("result", "success");
		map.put("days", plan.getDays().stripTrailingZeros().toPlainString());
		map.put("deduct", plan.isDeduct());
		map.put("year", plan.getYear());
		map.put("remainDays", plan.getRemainDays() == null ? null : plan.getRemainDays().stripTrailingZeros().toPlainString());
		map.put("pendingDays", plan.getPendingDays().stripTrailingZeros().toPlainString());
		map.put("afterDays", plan.getAfterDays() == null ? null : plan.getAfterDays().stripTrailingZeros().toPlainString());
		map.put("approverName", plan.getApprover() == null ? null
			: plan.getApprover().getEname() + " " + plan.getApprover().getJob().getJobName());
		return map;
	}

	@PostMapping("")
	public Map<String, Object> request(@ModelAttribute LeaveFormRequestDTO form, @AuthenticationPrincipal AccountDetails loginUser) {
		LeaveRequest leave = leaveService.request(loginUser, form);
		Map<String, Object> map = success("APPROVED".equals(leave.getStatus())
			? leave.getRequestNo() + " 휴가가 등록되었습니다. (결재자가 없어 바로 승인)"
			: leave.getRequestNo() + " 휴가를 신청했습니다. 결재자가 승인하면 확정됩니다.");
		map.put("leaveRequestId", leave.getLeaveRequestId());
		map.put("requestNo", leave.getRequestNo());
		map.put("status", leave.getStatus());
		return map;
	}

	@PostMapping("/{leaveRequestId}/cancel")
	public Map<String, Object> cancel(@PathVariable("leaveRequestId") int leaveRequestId,
									  @AuthenticationPrincipal AccountDetails loginUser) {
		LeaveRequest leave = leaveService.cancel(leaveRequestId, loginUser);

		Map<String, Object> map = new HashMap<>();
		map.put("result", "success");
		map.put("message", leave.getRequestNo() + " " + leave.getLeaveType().getLeaveTypeName() + " 신청을 취소했습니다.");
		return map;
	}

	@PostMapping("/{leaveRequestId}/approve")
	public Map<String, Object> approve(@PathVariable("leaveRequestId") int leaveRequestId,
									   @AuthenticationPrincipal AccountDetails loginUser) {
		LeaveRequest leave = leaveService.approve(leaveRequestId, loginUser);
		String message = leave.getRequestNo() + " 휴가를 승인했습니다."
			+ (leave.isDeduct() ? " (연차 " + leave.getDaysText() + "일 차감)" : "");
		return success(message);
	}

	@PostMapping("/{leaveRequestId}/reject")
	public Map<String, Object> reject(@PathVariable("leaveRequestId") int leaveRequestId,
									  @RequestParam(value = "rejectReason", required = false) String rejectReason,
									  @AuthenticationPrincipal AccountDetails loginUser) {
		LeaveRequest leave = leaveService.reject(leaveRequestId, rejectReason, loginUser);
		return success(leave.getRequestNo() + " 휴가를 반려했습니다.");
	}

	private Map<String, Object> success(String message) {
		Map<String, Object> map = new HashMap<>();
		map.put("result", "success");
		map.put("message", message);
		return map;
	}
}
