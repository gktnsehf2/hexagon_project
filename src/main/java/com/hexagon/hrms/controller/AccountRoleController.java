
package com.hexagon.hrms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccountRoleController {

    // S-25: 계정 Role 부여 관리 화면 이동
    @GetMapping("/auth/account-roles")
    public String getAccountRolePage() {

        return "auth/account-role";
    }
}
