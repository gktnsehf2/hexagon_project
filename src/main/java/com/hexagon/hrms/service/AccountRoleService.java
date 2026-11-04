
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


    // 1. Role 부여가 가능한 활성 계정 목록 조회
    @Transactional(readOnly = true)
    public List<Account> getAssignableAccounts() {

        return accountMapper.selectAssignableAccounts();
    }


    // 2. 계정에 부여할 수 있는 활성 Role 목록 조회
    @Transactional(readOnly = true)
    public List<HrRole> getActiveRoles() {

        return hrRoleMapper.selectActive();
    }


    // 3. 특정 계정에 현재 부여된 Role 목록 조회
    @Transactional(readOnly = true)
    public List<HrRole> getAccountRoles(int accountId) {

        // Role 변경 대상 계정인지 확인
        getAssignableAccount(accountId);

        return hrRoleMapper.selectByAccountId(accountId);
    }


    // 4. 특정 계정의 Role 부여·회수 이력 조회
    @Transactional(readOnly = true)
    public List<AccountRoleHistory> getRoleHistory(int accountId) {

        // Role 변경 대상 계정인지 확인
        getAssignableAccount(accountId);

        return accountRoleHistoryMapper.selectByAccountId(accountId);
    }


    // 5. 계정에 Role 부여
    @Transactional
    public void grantRole(
            int accountId,
            int hrRoleId,
            String reason,
            AccountDetails loginUser) {

        // 변경 대상 계정 확인
        Account account = getAssignableAccount(accountId);

        // 부여할 Role 확인
        HrRole role = getExistingRole(hrRoleId);

        // 비활성 Role 부여 방지
        if (role.getIsActive() != 1) {
            throw new BusinessException(
                "비활성화된 권한 그룹은 부여할 수 없습니다."
            );
        }

        // 내 계정 변경 · 내가 갖지 않은 권한이 든 그룹 부여 방지
        checkChangeable(accountId, role, loginUser);

        // 변경 사유 및 처리자 확인
        String validReason = validateReason(reason);
        Emp register = getRegister(loginUser);

        // 이미 부여된 Role인지 확인
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

        // Role 부여 이력 저장
        saveHistory(
            account,
            role,
            "GRANT",
            validReason,
            register
        );
    }


    // 6. 계정에 부여된 Role 회수
    @Transactional
    public void revokeRole(
            int accountId,
            int hrRoleId,
            String reason,
            AccountDetails loginUser) {

        // 변경 대상 계정 확인
        Account account = getAssignableAccount(accountId);

        // 회수할 Role 확인
        HrRole role = getExistingRole(hrRoleId);

        // 내 계정 변경 · 내가 갖지 않은 권한이 든 그룹 회수 방지
        checkChangeable(accountId, role, loginUser);

        // 변경 사유 및 처리자 확인
        String validReason = validateReason(reason);
        Emp register = getRegister(loginUser);

        // 실제로 부여된 Role인지 확인
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

        // 계정에 부여된 Role 회수
        int result = accountRoleMapper.delete(
            accountId,
            hrRoleId
        );

        if (result != 1) {
            throw new BusinessException(
                "권한 그룹 회수에 실패했습니다."
            );
        }

        // Role 회수 이력 저장
        saveHistory(
            account,
            role,
            "REVOKE",
            validReason,
            register
        );
    }


    // 7. Role 변경이 가능한 활성 계정 확인
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


    // 8. Role 존재 여부 확인
    private HrRole getExistingRole(int hrRoleId) {

        HrRole role = hrRoleMapper.selectById(hrRoleId);

        if (role == null) {
            throw new BusinessException(
                "존재하지 않는 권한 그룹입니다."
            );
        }

        return role;
    }


    // 8-1. 바꿀 수 있는 계정·그룹인지 확인
    //  - 내 계정은 직접 바꿀 수 없음 (스스로 권한을 올리는 것 방지)
    //  - 그룹에 내가 가지지 않은 권한이 있으면 부여·회수 불가 (예: 인사 관리자가 시스템 관리자 그룹을 주는 것 방지)
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


    // 9. Role 변경 사유 검증
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


    // 10. Role 변경 처리자 확인
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


    // 11. Role 부여·회수 이력 저장 (내부 공통 메서드)
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
