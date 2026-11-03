package com.hexagon.hrms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RoleController {

    // S-24: Role 관리 화면 이동
    @GetMapping("/auth/roles")
    public String getRoleList() {

        return "auth/role";
    }
}
 