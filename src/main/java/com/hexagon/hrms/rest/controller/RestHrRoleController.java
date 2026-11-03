
package com.hexagon.hrms.rest.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hexagon.hrms.domain.HrRole;
import com.hexagon.hrms.service.HrRoleService;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/roles")
public class RestHrRoleController {

    private final HrRoleService hrRoleService;


    // 1. S-16: 등록된 전체 Role 개수 조회 (활성·비활성 포함)
    @GetMapping("/count")
    public Map<String, Integer> getRoleCount() {

        return Collections.singletonMap(
            "count",
            hrRoleService.getRoleCount()
        );
    }


    // 2. S-24: 전체 Role 목록 조회 (활성·비활성 포함)
    @GetMapping
    public List<HrRole> getRoles() {

        return hrRoleService.getRoles();
    }


    // 3. S-24: Role 상세 조회 (Permission 포함)
    @GetMapping("/{hrRoleId}")
    public HrRole getRole(
            @PathVariable int hrRoleId) {

        return hrRoleService.getRole(hrRoleId);
    }


    // 4. S-24: 신규 Role 등록 및 Permission 연결
    @PostMapping
    public ResponseEntity<Map<String, Integer>> createRole(
            @RequestBody RoleRequest request) {

        HrRole role = new HrRole();

        role.setRoleCode(request.getRoleCode());
        role.setRoleName(request.getRoleName());

        hrRoleService.createRole(
            role,
            request.getPermissionIds()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Collections.singletonMap(
                    "hrRoleId",
                    role.getHrRoleId()
                ));
    }


    // 5. S-24: Role 이름 및 Permission 수정
    @PutMapping("/{hrRoleId}")
    public ResponseEntity<Void> updateRole(
            @PathVariable int hrRoleId,
            @RequestBody RoleRequest request) {

        HrRole role = new HrRole();

        role.setHrRoleId(hrRoleId);
        role.setRoleCode(request.getRoleCode());
        role.setRoleName(request.getRoleName());

        hrRoleService.updateRoleAndPermissions(
            role,
            request.getPermissionIds()
        );

        return ResponseEntity.noContent().build();
    }


    // 6. S-24: Role 비활성화
    @PatchMapping("/{hrRoleId}/deactivate")
    public ResponseEntity<Void> deactivateRole(
            @PathVariable int hrRoleId) {

        hrRoleService.deactivateRole(hrRoleId);

        return ResponseEntity.noContent().build();
    }


    // 7. S-24: Role 재활성화
    @PatchMapping("/{hrRoleId}/activate")
    public ResponseEntity<Void> activateRole(
            @PathVariable int hrRoleId) {

        hrRoleService.activateRole(hrRoleId);

        return ResponseEntity.noContent().build();
    }


    // 8. S-24: Role 등록·수정 요청 데이터
    @Getter
    @Setter
    public static class RoleRequest {

        private String roleCode;

        private String roleName;

        private List<Integer> permissionIds;
    }
}
