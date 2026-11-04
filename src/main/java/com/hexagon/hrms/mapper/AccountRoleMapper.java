
package com.hexagon.hrms.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.hexagon.hrms.domain.AccountRoleHistory;

@Mapper
public interface AccountRoleMapper {

    // 1. 계정에 Role 부여
    int insert(
        @Param("accountId") int accountId,
        @Param("hrRoleId") int hrRoleId
    );

    // 2. 특정 계정의 Role 중복 부여 여부 확인
    int countByAccountIdAndHrRoleId(
        @Param("accountId") int accountId,
        @Param("hrRoleId") int hrRoleId
    );

    // 3. 계정에 부여된 Role 회수
    int delete(
        @Param("accountId") int accountId,
        @Param("hrRoleId") int hrRoleId
    );

    // 4. 계정의 Role 부여·회수 이력 저장
    int insertHistory(AccountRoleHistory history);
}
