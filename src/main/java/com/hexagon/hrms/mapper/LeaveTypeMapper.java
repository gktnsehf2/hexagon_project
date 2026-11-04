package com.hexagon.hrms.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.hexagon.hrms.domain.LeaveType;

@Mapper
public interface LeaveTypeMapper {
	// 사용 중인 휴가 유형 (신청 화면 선택지)
	List<LeaveType> selectActive();

	LeaveType select(int leaveTypeId);
}
