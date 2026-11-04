
package com.hexagon.hrms.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexagon.hrms.domain.HrRole;
import com.hexagon.hrms.exception.BusinessException;
import com.hexagon.hrms.mapper.HrRoleMapper;
import com.hexagon.hrms.mapper.PermissionMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HrRoleService {

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
                "존재하지 않는 Role입니다."
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
                "Role 정보를 입력해주세요."
            );
        }

        if (role.getRoleCode() == null
                || role.getRoleCode().trim().isEmpty()) {

            throw new BusinessException(
                "Role 코드를 입력해주세요."
            );
        }

        validateRoleName(role.getRoleName());

        role.setRoleCode(role.getRoleCode().trim());
        role.setRoleName(role.getRoleName().trim());

        // Role 코드 중복 검사
        if (hrRoleMapper.selectByCode(role.getRoleCode()) != null) {

            throw new BusinessException(
                "이미 등록된 Role 코드입니다."
            );
        }

        // 신규 Role 등록
        int result = hrRoleMapper.insert(role);

        if (result != 1) {
            throw new BusinessException(
                "Role 등록에 실패했습니다."
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
                "수정할 Role 정보가 없습니다."
            );
        }

        HrRole existing = getRole(role.getHrRoleId());

        validateRoleName(role.getRoleName());

        // Role Code 수정 방지
        if (role.getRoleCode() != null
                && !existing.getRoleCode().equals(
                    role.getRoleCode())) {

            throw new BusinessException(
                "Role 코드는 수정할 수 없습니다."
            );
        }

        role.setRoleName(
            role.getRoleName().trim()
        );

        int result = hrRoleMapper.update(role);

        if (result != 1) {
            throw new BusinessException(
                "Role 수정에 실패했습니다."
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
                "Permission 목록이 필요합니다."
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
                "Permission 목록이 필요합니다."
            );
        }

        // Role 이름 수정
        updateRole(role);

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
                "이미 비활성화된 Role입니다."
            );
        }

        // 부여된 계정이 존재하면 비활성화 제한
        int assignedCount =
                hrRoleMapper.countAssignedAccounts(hrRoleId);

        if (assignedCount > 0) {
            throw new BusinessException(
                "계정에 부여된 Role은 비활성화할 수 없습니다."
            );
        }

        int result = hrRoleMapper.deactivate(hrRoleId);

        if (result != 1) {
            throw new BusinessException(
                "Role 비활성화에 실패했습니다."
            );
        }
    }


    // 9. Role 재활성화
    @Transactional
    public void activateRole(int hrRoleId) {

        HrRole role = getRole(hrRoleId);

        if (role.getIsActive() == 1) {
            throw new BusinessException(
                "이미 활성화된 Role입니다."
            );
        }

        int result = hrRoleMapper.activate(hrRoleId);

        if (result != 1) {
            throw new BusinessException(
                "Role 활성화에 실패했습니다."
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

        // 연결할 Permission이 실제 존재하는지 확인
        for (Integer permissionId : uniqueIds) {

            if (permissionId == null
                    || permissionMapper.selectById(permissionId) == null) {

                throw new BusinessException(
                    "존재하지 않는 Permission이 포함되어 있습니다."
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
                    "Permission 연결에 실패했습니다."
                );
            }
        }
    }


    // 11. Role 이름 입력값 검증 (내부 공통 메서드)
    private void validateRoleName(String roleName) {

        if (roleName == null
                || roleName.trim().isEmpty()) {

            throw new BusinessException(
                "Role 이름을 입력해주세요."
            );
        }
    }
}
