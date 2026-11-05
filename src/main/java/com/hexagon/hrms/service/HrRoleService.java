
package com.hexagon.hrms.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexagon.hrms.domain.HrRole;
import com.hexagon.hrms.exception.BusinessException;
import com.hexagon.hrms.mapper.HrRoleMapper;
import com.hexagon.hrms.mapper.PermissionMapper;
import com.hexagon.hrms.security.PermissionCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HrRoleService {

    public static final String DEFAULT_ROLE = "ROLE_EMPLOYEE";
    public static final String ADMIN_ROLE = "ROLE_SYSTEM_ADMIN";

    private static final String[] ADMIN_REQUIRED = { PermissionCode.ROLE_UPDATE, PermissionCode.ROLE_ASSIGN };

    private static final Pattern CODE_PATTERN = Pattern.compile("^ROLE_[A-Z0-9_]+$");

    private final HrRoleMapper hrRoleMapper;
    private final PermissionMapper permissionMapper;


    @Transactional(readOnly = true)
    public int getRoleCount() {

        return hrRoleMapper.countAll();
    }

    @Transactional(readOnly = true)
    public List<HrRole> getRoles() {

        return hrRoleMapper.selectAll();
    }


    @Transactional(readOnly = true)
    public HrRole getRole(int hrRoleId) {

        HrRole role = hrRoleMapper.selectById(hrRoleId);

        if (role == null) {
            throw new BusinessException(
                "존재하지 않는 권한 그룹입니다."
            );
        }

        return role;
    }


    @Transactional
    public void createRole(
            HrRole role, List<Integer> permissionIds) {

        if (role == null) {
            throw new BusinessException(
                "권한 그룹 정보를 입력해주세요."
            );
        }

        if (role.getRoleCode() == null
                || role.getRoleCode().trim().isEmpty()) {

            throw new BusinessException(
                "권한 그룹 코드를 입력해주세요."
            );
        }

        validateRoleName(role.getRoleName());

        role.setRoleCode(role.getRoleCode().trim().toUpperCase());
        role.setRoleName(role.getRoleName().trim());

        if (!CODE_PATTERN.matcher(role.getRoleCode()).matches()
                || role.getRoleCode().length() > 30) {

            throw new BusinessException(
                "권한 그룹 코드는 ROLE_로 시작하고 영문 대문자·숫자·_만 쓸 수 있습니다. (최대 30자)"
            );
        }

        if (hrRoleMapper.selectByCode(role.getRoleCode()) != null) {

            throw new BusinessException(
                "이미 등록된 권한 그룹 코드입니다."
            );
        }

        int result = hrRoleMapper.insert(role);

        if (result != 1) {
            throw new BusinessException(
                "권한 그룹 등록에 실패했습니다."
            );
        }


        savePermissions(
            role.getHrRoleId(),
            permissionIds
        );
    }


    @Transactional
    public void updateRole(HrRole role) {

        if (role == null) {
            throw new BusinessException(
                "수정할 권한 그룹 정보가 없습니다."
            );
        }

        HrRole existing = getRole(role.getHrRoleId());

        validateRoleName(role.getRoleName());

        if (role.getRoleCode() != null
                && !existing.getRoleCode().equals(
                    role.getRoleCode())) {

            throw new BusinessException(
                "권한 그룹 코드는 수정할 수 없습니다."
            );
        }

        role.setRoleName(
            role.getRoleName().trim()
        );

        int result = hrRoleMapper.update(role);

        if (result != 1) {
            throw new BusinessException(
                "권한 그룹 수정에 실패했습니다."
            );
        }
    }


    @Transactional
    public void updateRolePermissions(
            int hrRoleId, List<Integer> permissionIds) {

        getRole(hrRoleId);

        if (permissionIds == null) {
            throw new BusinessException(
                "권한 목록이 필요합니다."
            );
        }

        savePermissions(
            hrRoleId,
            permissionIds
        );
    }

    @Transactional
    public void updateRoleAndPermissions(
            HrRole role, List<Integer> permissionIds) {

        if (permissionIds == null) {
            throw new BusinessException(
                "권한 목록이 필요합니다."
            );
        }

        updateRole(role);

        HrRole existing = getRole(role.getHrRoleId());
        if (ADMIN_ROLE.equals(existing.getRoleCode())) {
            for (String code : ADMIN_REQUIRED) {
                com.hexagon.hrms.domain.Permission p = permissionMapper.selectByCode(code);
                if (p != null && !permissionIds.contains(p.getPermissionId())) {
                    throw new BusinessException(
                        "시스템 관리자 그룹에서는 '" + p.getPermissionName() + "' 권한을 뺄 수 없습니다."
                    );
                }
            }
        }

        updateRolePermissions(
            role.getHrRoleId(),
            permissionIds
        );
    }


    @Transactional
    public void deactivateRole(int hrRoleId) {

        HrRole role = getRole(hrRoleId);

        if (role.getIsActive() == 0) {
            throw new BusinessException(
                "이미 비활성화된 권한 그룹입니다."
            );
        }

        if (DEFAULT_ROLE.equals(role.getRoleCode())
                || ADMIN_ROLE.equals(role.getRoleCode())) {

            throw new BusinessException(
                "'" + role.getRoleName() + "'은(는) 기본 권한 그룹이라 비활성화할 수 없습니다."
            );
        }

        int assignedCount =
                hrRoleMapper.countAssignedAccounts(hrRoleId);

        if (assignedCount > 0) {
            throw new BusinessException(
                "계정에 부여된 권한 그룹은 비활성화할 수 없습니다."
            );
        }

        int result = hrRoleMapper.deactivate(hrRoleId);

        if (result != 1) {
            throw new BusinessException(
                "권한 그룹 비활성화에 실패했습니다."
            );
        }
    }

    @Transactional
    public void activateRole(int hrRoleId) {

        HrRole role = getRole(hrRoleId);

        if (role.getIsActive() == 1) {
            throw new BusinessException(
                "이미 활성화된 권한 그룹입니다."
            );
        }

        int result = hrRoleMapper.activate(hrRoleId);

        if (result != 1) {
            throw new BusinessException(
                "권한 그룹 활성화에 실패했습니다."
            );
        }
    }

    private void savePermissions(
            int hrRoleId, List<Integer> permissionIds) {

        Set<Integer> uniqueIds = new LinkedHashSet<>();

        if (permissionIds != null) {
            uniqueIds.addAll(permissionIds);
        }

        if (uniqueIds.isEmpty()) {
            throw new BusinessException(
                "권한을 1개 이상 선택해주세요."
            );
        }
        
        for (Integer permissionId : uniqueIds) {

            if (permissionId == null
                    || permissionMapper.selectById(permissionId) == null) {

                throw new BusinessException(
                    "존재하지 않는 권한이 포함되어 있습니다."
                );
            }
        }

        hrRoleMapper.deletePermissionsByHrRoleId(
            hrRoleId
        );

        for (Integer permissionId : uniqueIds) {

            int result = hrRoleMapper.insertPermission(
                hrRoleId,
                permissionId
            );

            if (result != 1) {
                throw new BusinessException(
                    "권한 연결에 실패했습니다."
                );
            }
        }
    }

    private void validateRoleName(String roleName) {

        if (roleName == null
                || roleName.trim().isEmpty()) {

            throw new BusinessException(
                "권한 그룹 이름을 입력해주세요."
            );
        }

        if (roleName.trim().length() > 50) {
            throw new BusinessException(
                "권한 그룹 이름은 50자 이하로 입력해주세요."
            );
        }
    }
}
