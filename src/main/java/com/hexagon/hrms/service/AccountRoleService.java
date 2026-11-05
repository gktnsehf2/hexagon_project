
package com.hexagon.hrms.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexagon.hrms.domain.Account;
import com.hexagon.hrms.domain.AccountRoleHistory;
import com.hexagon.hrms.domain.Emp;
import com.hexagon.hrms.domain.HrRole;
import com.hexagon.hrms.domain.Permission;
import com.hexagon.hrms.exception.BusinessException;
import com.hexagon.hrms.mapper.AccountMapper;
import com.hexagon.hrms.mapper.AccountRoleMapper;
import com.hexagon.hrms.mapper.AccountRoleHistoryMapper;
import com.hexagon.hrms.mapper.HrRoleMapper;
import com.hexagon.hrms.security.AccountDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountRoleService {

    private final AccountMapper accountMapper;
    private final AccountRoleMapper accountRoleMapper;
    private final AccountRoleHistoryMapper accountRoleHistoryMapper;
    private final HrRoleMapper hrRoleMapper;

    @Transactional(readOnly = true)
    public List<Account> getAssignableAccounts() {

        return accountMapper.selectAssignableAccounts();
    }


    @Transactional(readOnly = true)
    public List<HrRole> getActiveRoles() {

        return hrRoleMapper.selectActive();
    }

    @Transactional(readOnly = true)
    public List<HrRole> getAccountRoles(int accountId) {

        getAssignableAccount(accountId);

        return hrRoleMapper.selectByAccountId(accountId);
    }


    @Transactional(readOnly = true)
    public List<AccountRoleHistory> getRoleHistory(int accountId) {

        getAssignableAccount(accountId);

        return accountRoleHistoryMapper.selectByAccountId(accountId);
    }

    @Transactional
    public void grantRole(
            int accountId,
            int hrRoleId,
            String reason,
            AccountDetails loginUser) {

        Account account = getAssignableAccount(accountId);

        HrRole role = getExistingRole(hrRoleId);

        if (role.getIsActive() != 1) {
            throw new BusinessException(
                "비활성화된 권한 그룹은 부여할 수 없습니다."
            );
        }
        
        checkChangeable(accountId, role, loginUser);

        String validReason = validateReason(reason);
        Emp register = getRegister(loginUser);

        int count =
                accountRoleMapper.countByAccountIdAndHrRoleId(
                    accountId,
                    hrRoleId
                );

        if (count > 0) {
            throw new BusinessException(
                "이미 해당 계정에 부여된 권한 그룹입니다."
            );
        }

        // 계정에 Role 부여
        int result = accountRoleMapper.insert(
            accountId,
            hrRoleId
        );

        if (result != 1) {
            throw new BusinessException(
                "권한 그룹 부여에 실패했습니다."
            );
        }
        
        saveHistory(
            account,
            role,
            "GRANT",
            validReason,
            register
        );
    }

    @Transactional
    public void revokeRole(
            int accountId,
            int hrRoleId,
            String reason,
            AccountDetails loginUser) {

        Account account = getAssignableAccount(accountId);

        HrRole role = getExistingRole(hrRoleId);

        checkChangeable(accountId, role, loginUser);

        String validReason = validateReason(reason);
        Emp register = getRegister(loginUser);

        int count =
                accountRoleMapper.countByAccountIdAndHrRoleId(
                    accountId,
                    hrRoleId
                );

        if (count == 0) {
            throw new BusinessException(
                "해당 계정에 부여되지 않은 권한 그룹입니다."
            );
        }

        int result = accountRoleMapper.delete(
            accountId,
            hrRoleId
        );

        if (result != 1) {
            throw new BusinessException(
                "권한 그룹 회수에 실패했습니다."
            );
        }

        saveHistory(
            account,
            role,
            "REVOKE",
            validReason,
            register
        );
    }

    private Account getAssignableAccount(int accountId) {

        Account account =
                accountMapper.selectAssignableAccountById(
                    accountId
                );

        if (account == null) {
            throw new BusinessException(
                "존재하지 않거나 비활성화된 계정입니다."
            );
        }

        return account;
    }

    private HrRole getExistingRole(int hrRoleId) {

        HrRole role = hrRoleMapper.selectById(hrRoleId);

        if (role == null) {
            throw new BusinessException(
                "존재하지 않는 권한 그룹입니다."
            );
        }

        return role;
    }

    private void checkChangeable(int accountId, HrRole role, AccountDetails loginUser) {

        if (loginUser == null || loginUser.getAccount() == null) {
            throw new AccessDeniedException("처리자 정보를 확인할 수 없습니다.");
        }

        if (loginUser.getAccount().getAccountId() == accountId) {
            throw new BusinessException(
                "내 계정의 권한 그룹은 직접 바꿀 수 없습니다. 다른 담당자에게 요청해주세요."
            );
        }

        if (role.getPermissions() != null) {
            for (Permission permission : role.getPermissions()) {
                if (!loginUser.hasAuthority(permission.getPermissionCode())) {
                    throw new BusinessException(
                        "'" + role.getRoleName() + "'에는 내가 가지지 않은 권한이 있어 부여·회수할 수 없습니다."
                    );
                }
            }
        }
    }

    private String validateReason(String reason) {

        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessException(
                "권한 그룹 변경 사유를 입력해주세요."
            );
        }

        if (reason.trim().length() > 200) {
            throw new BusinessException(
                "변경 사유는 200자 이하로 입력해주세요."
            );
        }

        return reason.trim();
    }


    private Emp getRegister(AccountDetails loginUser) {

        if (loginUser == null
                || loginUser.getAccount() == null
                || loginUser.getAccount().getEmp() == null) {

            throw new AccessDeniedException(
                "권한 그룹 변경 처리자 정보를 확인할 수 없습니다."
            );
        }

        return loginUser.getAccount().getEmp();
    }


    private void saveHistory(
            Account account,
            HrRole role,
            String actionType,
            String reason,
            Emp register) {

        AccountRoleHistory history = new AccountRoleHistory();

        history.setAccount(account);
        history.setHrRole(role);
        history.setActionType(actionType);
        history.setReason(reason);
        history.setRegister(register);

        int result = accountRoleHistoryMapper.insert(history);

        if (result != 1) {
            throw new BusinessException(
                "권한 그룹 변경 이력 저장에 실패했습니다."
            );
        }
    }
}
