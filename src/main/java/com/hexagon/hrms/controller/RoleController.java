package com.hexagon.hrms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RoleController {

    @GetMapping("/auth/roles")
    public String getRoleList() {

        return "auth/role";
    }
}
 