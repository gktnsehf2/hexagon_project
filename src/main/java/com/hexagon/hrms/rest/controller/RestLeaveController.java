package com.hexagon.hrms.rest.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hexagon.hrms.domain.LeaveRequest;
import com.hexagon.hrms.security.AccountDetails;
import com.hexagon.hrms.service.LeaveService;

import lombok.RequiredArgsConstructor;

/*
 F-14 휴가 신청 취소 (내 휴가 내역 화면의 취소 버튼에서 fetch 로 호출)
 요청: POST /api/leaves/{leaveRequestId}/cancel
 성공: 200 {result: "success", message}
 실패: 400 {result: "fail", message}  ← RestExceptionHandler
 권한(PERMISSION_LEAVE_CANCEL)은 SecurityConfig 에서, 본인·대기 건 여부는 LeaveService 에서 검사
*/
@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class RestLeaveController {

	private final LeaveService leaveService;

	@PostMapping("/{leaveRequestId}/cancel")
	public Map<String, Object> cancel(@PathVariable("leaveRequestId") int leaveRequestId,
									  @AuthenticationPrincipal AccountDetails loginUser) {
		LeaveRequest leave = leaveService.cancel(leaveRequestId, loginUser);

		Map<String, Object> map = new HashMap<>();
		map.put("result", "success");
		map.put("message", leave.getRequestNo() + " " + leave.getLeaveType().getLeaveTypeName() + " 신청을 취소했습니다.");
		return map;
	}
}
