package com.hexagon.hrms.rest.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hexagon.hrms.service.DepartmentService;
import com.hexagon.hrms.common.web.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class RestDepartmentController {

	private final DepartmentService departmentService;

	@DeleteMapping("/{id}")
	public ApiResponse<Map<String, Object>> delete(@PathVariable("id") int id) {
		boolean deleted = departmentService.delete(id);
		Map<String, Object> data = new HashMap<>();
		data.put("deleted", deleted);
		return ApiResponse.success(deleted ? "조직을 삭제했습니다."
			: "퇴직자·인사 이력 등 사용 기록이 있어 삭제 대신 '사용 안 함'으로 바꿨습니다.", data);
	}
}
