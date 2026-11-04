
package com.hexagon.hrms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PermissionController {

    // S-16 권한 목록 관리 화면
    @GetMapping("/auth/permissions")
    public String getPermissionList() {
        return "auth/permission";
    }
}
