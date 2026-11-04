
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


    // 1. 전체 권한 조회 및 검색
    @Transactional(readOnly = true)
    public List<Permission> getPermissions(
            String keyword, String permissionGroup) {

        return permissionMapper.selectAll(
            keyword, permissionGroup
        );
    }


    // 2. 권한 상세 조회
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


    // 3. 권한 등록
    @Transactional
    public void createPermission(Permission permission) {

        if (permission == null) {
            throw new BusinessException(
                "권한 정보를 입력해주세요."
            );
        }

        // 필수 입력값 확인
        validatePermission(permission, true);

        // 앞뒤 공백 제거
        permission.setPermissionCode(
            permission.getPermissionCode().trim()
        );

        permission.setPermissionName(
            permission.getPermissionName().trim()
        );

        permission.setPermissionGroup(
            permission.getPermissionGroup().trim()
        );

        // 권한 코드 형식 (화면과 같은 규칙, 컬럼 길이 40)
        if (!permission.getPermissionCode().matches("^PERMISSION_[A-Z0-9_]+$")
                || permission.getPermissionCode().length() > 40) {
            throw new BusinessException(
                "권한 코드는 PERMISSION_ 으로 시작하는 영문 대문자·숫자·_ 40자 이내로 입력해주세요."
            );
        }

        // 권한 코드 중복 확인
        Permission existing =
                permissionMapper.selectByCode(
                    permission.getPermissionCode()
                );

        if (existing != null) {
            throw new BusinessException(
                "이미 등록된 권한 코드입니다."
            );
        }

        // DB 등록
        int result = permissionMapper.insert(permission);

        if (result != 1) {
            throw new BusinessException(
                "권한 등록에 실패했습니다."
            );
        }
    }


    // 4. 권한 수정
    @Transactional
    public void updatePermission(Permission permission) {

        if (permission == null) {
            throw new BusinessException(
                "수정할 권한 정보가 없습니다."
            );
        }

        // 기존 권한 확인
        Permission existing =
                getPermission(permission.getPermissionId());

        // 권한명과 모듈은 필수 입력
        validatePermission(permission, false);

        // 권한 코드는 수정 불가
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

        // DB 수정
        int result = permissionMapper.update(permission);

        if (result != 1) {
            throw new BusinessException(
                "권한 수정에 실패했습니다."
            );
        }
    }


    // 5. 권한 삭제
    @Transactional
    public void deletePermission(int permissionId) {

        // 권한 존재 여부 확인
        getPermission(permissionId);

        // 다른 Role에서 사용 중인지 확인
        int linkedCount =
                permissionMapper.countLinkedRoles(permissionId);

        if (linkedCount > 0) {
            throw new BusinessException(
                "다른 Role에서 사용 중인 권한은 삭제할 수 없습니다."
            );
        }

        // DB 삭제
        int result = permissionMapper.delete(permissionId);

        if (result != 1) {
            throw new BusinessException(
                "권한 삭제에 실패했습니다."
            );
        }
    }


    // 6. 입력값 검증 (Service 내부에서만 사용)
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
