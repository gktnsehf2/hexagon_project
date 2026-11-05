
package com.hexagon.hrms.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexagon.hrms.domain.Permission;
import com.hexagon.hrms.exception.BusinessException;
import com.hexagon.hrms.mapper.PermissionMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionMapper permissionMapper;


    @Transactional(readOnly = true)
    public List<Permission> getPermissions(
            String keyword, String permissionGroup) {

        return permissionMapper.selectAll(
            keyword, permissionGroup
        );
    }

    @Transactional(readOnly = true)
    public Permission getPermission(int permissionId) {

        Permission permission =
                permissionMapper.selectById(permissionId);

        if (permission == null) {
            throw new BusinessException(
                "존재하지 않는 권한입니다."
            );
        }

        return permission;
    }


    @Transactional
    public void createPermission(Permission permission) {

        if (permission == null) {
            throw new BusinessException(
                "권한 정보를 입력해주세요."
            );
        }

        validatePermission(permission, true);

        permission.setPermissionCode(
            permission.getPermissionCode().trim()
        );

        permission.setPermissionName(
            permission.getPermissionName().trim()
        );

        permission.setPermissionGroup(
            permission.getPermissionGroup().trim()
        );

        if (!permission.getPermissionCode().matches("^PERMISSION_[A-Z0-9_]+$")
                || permission.getPermissionCode().length() > 40) {
            throw new BusinessException(
                "권한 코드는 PERMISSION_ 으로 시작하는 영문 대문자·숫자·_ 40자 이내로 입력해주세요."
            );
        }

        Permission existing =
                permissionMapper.selectByCode(
                    permission.getPermissionCode()
                );

        if (existing != null) {
            throw new BusinessException(
                "이미 등록된 권한 코드입니다."
            );
        }

        int result = permissionMapper.insert(permission);

        if (result != 1) {
            throw new BusinessException(
                "권한 등록에 실패했습니다."
            );
        }
    }

    @Transactional
    public void updatePermission(Permission permission) {

        if (permission == null) {
            throw new BusinessException(
                "수정할 권한 정보가 없습니다."
            );
        }

        Permission existing =
                getPermission(permission.getPermissionId());

        validatePermission(permission, false);

        if (permission.getPermissionCode() != null
                && !existing.getPermissionCode().equals(
                    permission.getPermissionCode())) {

            throw new BusinessException(
                "권한 코드는 수정할 수 없습니다."
            );
        }

        permission.setPermissionName(
            permission.getPermissionName().trim()
        );

        permission.setPermissionGroup(
            permission.getPermissionGroup().trim()
        );

        int result = permissionMapper.update(permission);

        if (result != 1) {
            throw new BusinessException(
                "권한 수정에 실패했습니다."
            );
        }
    }


    @Transactional
    public void deletePermission(int permissionId) {

        getPermission(permissionId);

        int linkedCount =
                permissionMapper.countLinkedRoles(permissionId);

        if (linkedCount > 0) {
            throw new BusinessException(
                "다른 Role에서 사용 중인 권한은 삭제할 수 없습니다."
            );
        }

        int result = permissionMapper.delete(permissionId);

        if (result != 1) {
            throw new BusinessException(
                "권한 삭제에 실패했습니다."
            );
        }
    }

    private void validatePermission(
            Permission permission, boolean checkCode) {

        if (checkCode &&
                (permission.getPermissionCode() == null
                || permission.getPermissionCode().trim().isEmpty())) {

            throw new BusinessException(
                "권한 코드를 입력해주세요."
            );
        }

        if (permission.getPermissionName() == null
                || permission.getPermissionName().trim().isEmpty()) {

            throw new BusinessException(
                "권한명을 입력해주세요."
            );
        }

        if (permission.getPermissionGroup() == null
                || permission.getPermissionGroup().trim().isEmpty()) {

            throw new BusinessException(
                "권한 모듈을 입력해주세요."
            );
        }
    }
}
