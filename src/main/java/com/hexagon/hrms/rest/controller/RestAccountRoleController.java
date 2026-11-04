
package com.hexagon.hrms.rest.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hexagon.hrms.domain.Account;
import com.hexagon.hrms.domain.AccountRoleHistory;
import com.hexagon.hrms.domain.Emp;
import com.hexagon.hrms.domain.HrRole;
import com.hexagon.hrms.exception.BusinessException;
import com.hexagon.hrms.security.AccountDetails;
import com.hexagon.hrms.service.AccountRoleService;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
public class RestAccountRoleController {

    private final AccountRoleService accountRoleService;


    // 1. Role 부여가 가능한 활성 계정 목록 조회
    @GetMapping("/assignable")
    public List<AccountSummary> getAssignableAccounts() {

        List<Account> accounts =
                accountRoleService.getAssignableAccounts();

        List<AccountSummary> result = new ArrayList<>();

        for (Account account : accounts) {

            Emp emp = account.getEmp();

            result.add(new AccountSummary(
                account.getAccountId(),
                account.getLoginId(),
                emp != null ? emp.getEmpId() : null,
                emp != null ? emp.getEname() : null
            ));
        }

        return result;
    }


    // 2. 특정 계정에 부여된 Role 목록 조회
    @GetMapping("/{accountId}/roles")
    public List<HrRole> getAccountRoles(
            @PathVariable int accountId) {

        return accountRoleService.getAccountRoles(accountId);
    }


    // 3. 특정 계정의 Role 부여·회수 이력 조회
    @GetMapping("/{accountId}/roles/history")
    public List<RoleHistorySummary> getRoleHistory(
            @PathVariable int accountId) {

        List<AccountRoleHistory> histories =
                accountRoleService.getRoleHistory(accountId);

        List<RoleHistorySummary> result = new ArrayList<>();

        for (AccountRoleHistory history : histories) {

            HrRole role = history.getHrRole();
            Emp register = history.getRegister();

            result.add(new RoleHistorySummary(
                history.getAccountRoleHistoryId(),
                role != null ? role.getHrRoleId() : null,
                role != null ? role.getRoleName() : null,
                history.getActionType(),
                history.getReason(),
                history.getCreatedAt() != null
                    ? history.getCreatedAt().toString()
                    : null,
                register != null ? register.getEmpId() : null,
                register != null ? register.getEname() : null
            ));
        }

        return result;
    }


    // 4. 특정 계정에 Role 부여
    @PostMapping("/{accountId}/roles")
    public ResponseEntity<Void> grantRole(
            @PathVariable int accountId,
            @RequestBody RoleChangeRequest request,
            @AuthenticationPrincipal AccountDetails loginUser) {

        if (request == null || request.getHrRoleId() == null
                || request.getHrRoleId() <= 0) {

            throw new BusinessException(
                "부여할 Role을 선택해주세요."
            );
        }

        accountRoleService.grantRole(
            accountId,
            request.getHrRoleId(),
            request.getReason(),
            loginUser
        );

        return ResponseEntity.noContent().build();
    }


    // 5. 특정 계정에 부여된 Role 회수
    @DeleteMapping("/{accountId}/roles/{hrRoleId}")
    public ResponseEntity<Void> revokeRole(
            @PathVariable int accountId,
            @PathVariable int hrRoleId,
            @RequestBody RoleChangeRequest request,
            @AuthenticationPrincipal AccountDetails loginUser) {

        if (request == null) {
            throw new BusinessException(
                "Role 회수 사유를 입력해주세요."
            );
        }

        accountRoleService.revokeRole(
            accountId,
            hrRoleId,
            request.getReason(),
            loginUser
        );

        return ResponseEntity.noContent().build();
    }


    // 6. Role 변경 요청 데이터
    @Getter
    @Setter
    public static class RoleChangeRequest {

        private Integer hrRoleId;
        private String reason;
    }


    // 7. 활성 계정 목록 응답 데이터
    @Getter
    @AllArgsConstructor
    public static class AccountSummary {

        private int accountId;
        private String loginId;
        private Integer empId;
        private String empName;
    }


    // 8. Role 변경 이력 응답 데이터
    @Getter
    @AllArgsConstructor
    public static class RoleHistorySummary {

        private int accountRoleHistoryId;
        private Integer hrRoleId;
        private String roleName;
        private String actionType;
        private String reason;
        private String createdAt;
        private Integer registerId;
        private String registerName;
    }
}
