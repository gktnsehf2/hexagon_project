package com.hexagon.hrms.rest.controller;

import java.util.Collections;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hexagon.hrms.service.HrRoleService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hr-roles")
public class RestHrRoleController {

    private final HrRoleService hrRoleService;

    // S-16: 등록된 전체 Role 개수 조회 (활성·비활성 포함)
    @GetMapping("/count")
    public Map<String, Integer> getRoleCount() {

        return Collections.singletonMap(
            "count",
            hrRoleService.getRoleCount()
        );
    }
}
