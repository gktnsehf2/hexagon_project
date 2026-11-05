
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

    @GetMapping("/{permissionId}")
    public ResponseEntity<Permission> getPermission(
            @PathVariable int permissionId) {

        Permission permission =
                permissionService.getPermission(permissionId);

        return ResponseEntity.ok(permission);
    }

    @PostMapping
    public ResponseEntity<Void> createPermission(
            @RequestBody Permission permission) {

        permissionService.createPermission(permission);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @PutMapping("/{permissionId}")
    public ResponseEntity<Void> updatePermission(
            @PathVariable int permissionId,
            @RequestBody Permission permission) {

        permission.setPermissionId(permissionId);

        permissionService.updatePermission(permission);

        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/{permissionId}")
    public ResponseEntity<Void> deletePermission(
            @PathVariable int permissionId) {

        permissionService.deletePermission(permissionId);

        return ResponseEntity.noContent().build();
    }
}
