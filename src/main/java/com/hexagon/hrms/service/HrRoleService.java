package com.hexagon.hrms.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexagon.hrms.mapper.HrRoleMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HrRoleService {

    private final HrRoleMapper hrRoleMapper;

    @Transactional(readOnly = true)
    public int getRoleCount() {
        return hrRoleMapper.countAll();
    }
}