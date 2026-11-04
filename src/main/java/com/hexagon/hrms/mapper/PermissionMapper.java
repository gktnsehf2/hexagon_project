
package com.hexagon.hrms.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.hexagon.hrms.domain.Permission;

@Mapper
public interface PermissionMapper {

    // 1. Role별 Permission 조회 
    List<Permission> selectByHrRoleId(int hrRoleId);

    // 2. 전체 조회 및 검색
    List<Permission> selectAll(
        @Param("keyword") String keyword,
        @Param("permissionGroup") String permissionGroup
    );

    // 3. Permission ID로 조회
    Permission selectById(int permissionId);

    // 4. Permission 코드로 조회
    Permission selectByCode(String permissionCode);

    // 5. Permission 등록
    int insert(Permission permission);

    // 6. Permission 수정
    int update(Permission permission);

    // 7. Permission을 사용하는 Role 개수 조회
    int countLinkedRoles(int permissionId);

    // 8. Permission 삭제
    int delete(int permissionId);

}
