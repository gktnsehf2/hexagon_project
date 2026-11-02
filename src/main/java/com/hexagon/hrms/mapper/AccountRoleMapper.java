package com.hexagon.hrms.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.hexagon.hrms.domain.AccountRoleHistory;

// 계정별 Role (account_role) 과 부여/회수 이력 (account_role_history)
@Mapper
public interface AccountRoleMapper {
	// 계정에 Role 부여
	int insert(@Param("accountId") int accountId, @Param("hrRoleId") int hrRoleId);

	// 부여/회수 이력 남기기
	int insertHistory(AccountRoleHistory history);
}
