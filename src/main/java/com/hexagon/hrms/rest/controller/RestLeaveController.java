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

/*
 F-18 휴가 신청 (휴가 신청 화면에서 fetch 로 호출)                      PERMISSION_LEAVE_REQUEST
       POST /api/leaves            leaveTypeId, startDate, endDate, reason (폼 형식) → {result, message, leaveRequestId, status}
       GET  /api/leaves/plan       같은 값으로 사용 일수·잔여 연차·결재자 미리보기 → {days, remainDays, pendingDays, afterDays, approverName, deduct}
 F-14 휴가 신청 취소 (내 휴가 내역 화면의 취소 버튼에서 fetch 로 호출)
 F-20 휴가 승인 / 반려 (휴가 결재 화면에서 fetch 로 호출)                PERMISSION_LEAVE_APPROVE
 요청: POST /api/leaves/{leaveRequestId}/cancel
       POST /api/leaves/{leaveRequestId}/approve
       POST /api/leaves/{leaveRequestId}/reject    rejectReason=... (폼 형식, 필수)
 성공: 200 {result: "success", message}
 실패: 400 {result: "fail", message}  ← RestExceptionHandler
 권한(PERMISSION_LEAVE_CANCEL)은 SecurityConfig 에서, 본인·대기 건 여부는 LeaveService 에서 검사
*/
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
