package com.hexagon.hrms.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.hexagon.hrms.domain.AccountRoleHistory;

@Mapper
public interface AccountRoleHistoryMapper {

    int insert(AccountRoleHistory history);

    List<AccountRoleHistory> selectByAccountId(int accountId);

    AccountRoleHistory selectById(int accountRoleHistoryId);
}