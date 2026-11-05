
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
    
    @GetMapping("/count")
    public Map<String, Integer> getRoleCount() {

        return Collections.singletonMap(
            "count",
            hrRoleService.getRoleCount()
        );
    }

    @GetMapping
    public List<HrRole> getRoles() {

        return hrRoleService.getRoles();
    }


    @GetMapping("/{hrRoleId}")
    public HrRole getRole(
            @PathVariable int hrRoleId) {

        return hrRoleService.getRole(hrRoleId);
    }

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


    @PatchMapping("/{hrRoleId}/deactivate")
    public ResponseEntity<Void> deactivateRole(
            @PathVariable int hrRoleId) {

        hrRoleService.deactivateRole(hrRoleId);

        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/{hrRoleId}/activate")
    public ResponseEntity<Void> activateRole(
            @PathVariable int hrRoleId) {

        hrRoleService.activateRole(hrRoleId);

        return ResponseEntity.noContent().build();
    }

    @Getter
    @Setter
    public static class RoleRequest {

        private String roleCode;

        private String roleName;

        private List<Integer> permissionIds;
    }
}
