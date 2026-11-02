package com.hexagon.hrms.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.hexagon.hrms.domain.HrAppointment;

@Mapper
public interface HrAppointmentMapper {
	// 발령 등록 (입사 · 퇴직 처리 때도 함께 기록)
	int insert(HrAppointment appointment);

	// 사원의 발령 이력 (최근 발령일 순). 조직·직급·직책·등록자는 이름까지
	List<HrAppointment> selectByEmpId(int empId);
}
