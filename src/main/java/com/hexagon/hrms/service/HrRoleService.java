
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

    // 신규 입사자 기본 권한 그룹 · 권한 관리를 할 수 있는 마지막 그룹 (사용 안 함으로 바꿀 수 없음)
    public static final String DEFAULT_ROLE = "ROLE_EMPLOYEE";
    public static final String ADMIN_ROLE = "ROLE_SYSTEM_ADMIN";

    // 시스템 관리자 그룹에서 뺄 수 없는 권한 (빼면 아무도 권한 그룹을 고치거나 부여할 수 없게 될 수 있음)
    private static final String[] ADMIN_REQUIRED = { PermissionCode.ROLE_UPDATE, PermissionCode.ROLE_ASSIGN };

    private static final Pattern CODE_PATTERN = Pattern.compile("^ROLE_[A-Z0-9_]+$");

    private final HrRoleMapper hrRoleMapper;
    private final PermissionMapper permissionMapper;


    // 1. 등록된 전체 Role 개수 조회
    @Transactional(readOnly = true)
    public int getRoleCount() {

        return hrRoleMapper.countAll();
    }


    // 2. 전체 Role 목록 조회 (활성·비활성 포함)
    @Transactional(readOnly = true)
    public List<HrRole> getRoles() {

        return hrRoleMapper.selectAll();
    }


    // 3. Role 상세 조회 (Permission 포함)
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


    // 4. 신규 Role 등록
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

        // 코드 형식 검사 (화면 검사를 거치지 않은 요청 대비)
        if (!CODE_PATTERN.matcher(role.getRoleCode()).matches()
                || role.getRoleCode().length() > 30) {

            throw new BusinessException(
                "권한 그룹 코드는 ROLE_로 시작하고 영문 대문자·숫자·_만 쓸 수 있습니다. (최대 30자)"
            );
        }

        // Role 코드 중복 검사
        if (hrRoleMapper.selectByCode(role.getRoleCode()) != null) {

            throw new BusinessException(
                "이미 등록된 권한 그룹 코드입니다."
            );
        }

        // 신규 Role 등록
        int result = hrRoleMapper.insert(role);

        if (result != 1) {
            throw new BusinessException(
                "권한 그룹 등록에 실패했습니다."
            );
        }

        // 체크한 Permission 연결
        savePermissions(
            role.getHrRoleId(),
            permissionIds
        );
    }


    // 5. Role 이름 수정
    @Transactional
    public void updateRole(HrRole role) {

        if (role == null) {
            throw new BusinessException(
                "수정할 권한 그룹 정보가 없습니다."
            );
        }

        HrRole existing = getRole(role.getHrRoleId());

        validateRoleName(role.getRoleName());

        // Role Code 수정 방지
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


    // 6. Role에 연결된 Permission 변경
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


    // 7. Role 이름과 Permission을 함께 수정
    @Transactional
    public void updateRoleAndPermissions(
            HrRole role, List<Integer> permissionIds) {

        if (permissionIds == null) {
            throw new BusinessException(
                "권한 목록이 필요합니다."
            );
        }

        // Role 이름 수정
        updateRole(role);

        // 시스템 관리자 그룹은 권한 관리에 꼭 필요한 권한을 뺄 수 없음
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

        // 연결된 Permission 수정
        updateRolePermissions(
            role.getHrRoleId(),
            permissionIds
        );
    }


    // 8. Role 비활성화
    @Transactional
    public void deactivateRole(int hrRoleId) {

        HrRole role = getRole(hrRoleId);

        if (role.getIsActive() == 0) {
            throw new BusinessException(
                "이미 비활성화된 권한 그룹입니다."
            );
        }

        // 기본 그룹은 사용 안 함으로 바꿀 수 없음
        if (DEFAULT_ROLE.equals(role.getRoleCode())
                || ADMIN_ROLE.equals(role.getRoleCode())) {

            throw new BusinessException(
                "'" + role.getRoleName() + "'은(는) 기본 권한 그룹이라 비활성화할 수 없습니다."
            );
        }

        // 부여된 계정이 존재하면 비활성화 제한
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


    // 9. Role 재활성화
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


    // 10. Permission 연결 저장 (내부 공통 메서드)
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

        // 연결할 Permission이 실제 존재하는지 확인
        for (Integer permissionId : uniqueIds) {

            if (permissionId == null
                    || permissionMapper.selectById(permissionId) == null) {

                throw new BusinessException(
                    "존재하지 않는 권한이 포함되어 있습니다."
                );
            }
        }

        // 기존 Permission 연결 모두 제거
        hrRoleMapper.deletePermissionsByHrRoleId(
            hrRoleId
        );

        // 선택한 Permission 다시 연결
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


    // 11. Role 이름 입력값 검증 (내부 공통 메서드)
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
