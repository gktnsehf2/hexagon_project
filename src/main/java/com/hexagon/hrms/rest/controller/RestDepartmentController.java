package com.hexagon.hrms.rest.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hexagon.hrms.service.DepartmentService;

import lombok.RequiredArgsConstructor;

/*
 F-12 부서 삭제 (조직도의 삭제 확인 팝업 P-04 에서 fetch 로 호출)            PERMISSION_DEPT_DELETE
 요청: DELETE /api/departments/{id}
 성공: 200 {result: "success", message, deleted}  deleted=false 면 사용 기록이 있어 '사용 안 함'으로 바뀜
 실패: 400 {result: "fail", message}  ← RestExceptionHandler (소속 사원·하위 조직이 있을 때 등)
*/
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class RestDepartmentController {

	private final DepartmentService departmentService;

	@DeleteMapping("/{id}")
	public Map<String, Object> delete(@PathVariable("id") int id) {
		boolean deleted = departmentService.delete(id);
		Map<String, Object> map = new HashMap<>();
		map.put("result", "success");
		map.put("deleted", deleted);
		map.put("message", deleted ? "조직을 삭제했습니다."
			: "퇴직자·인사 이력 등 사용 기록이 있어 삭제 대신 '사용 안 함'으로 바꿨습니다.");
		return map;
	}
}
