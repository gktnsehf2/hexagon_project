
package com.hexagon.hrms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccountRoleController {

    @GetMapping("/auth/account-roles")
    public String getAccountRolePage() {

        return "auth/account-role";
    }
}
