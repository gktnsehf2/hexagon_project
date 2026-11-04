
package com.hexagon.hrms.rest.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hexagon.hrms.domain.Permission;
import com.hexagon.hrms.service.PermissionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/permissions")
public class RestPermissionController {

    private final PermissionService permissionService;


    // 1. 전체 권한 조회 및 검색
    @GetMapping
    public ResponseEntity<List<Permission>> getPermissions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String permissionGroup) {

        List<Permission> permissions =
                permissionService.getPermissions(
                    keyword, permissionGroup
                );

        return ResponseEntity.ok(permissions);
    }


    // 2. 권한 상세 조회
    @GetMapping("/{permissionId}")
    public ResponseEntity<Permission> getPermission(
            @PathVariable int permissionId) {

        Permission permission =
                permissionService.getPermission(permissionId);

        return ResponseEntity.ok(permission);
    }


    // 3. 권한 신규 등록
    @PostMapping
    public ResponseEntity<Void> createPermission(
            @RequestBody Permission permission) {

        permissionService.createPermission(permission);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }


    // 4. 권한 수정
    @PutMapping("/{permissionId}")
    public ResponseEntity<Void> updatePermission(
            @PathVariable int permissionId,
            @RequestBody Permission permission) {

        // URL에 있는 ID를 수정 대상으로 사용
        permission.setPermissionId(permissionId);

        permissionService.updatePermission(permission);

        return ResponseEntity.noContent().build();
    }


    // 5. 권한 삭제
    @DeleteMapping("/{permissionId}")
    public ResponseEntity<Void> deletePermission(
            @PathVariable int permissionId) {

        permissionService.deletePermission(permissionId);

        return ResponseEntity.noContent().build();
    }
}
