package com.hexagon.hrms.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.hexagon.hrms.domain.AttendanceCorrection;

@Mapper
public interface AttendanceCorrectionMapper {
	// 내 정정 신청
	List<AttendanceCorrection> selectMyList(int empId);

	// 한 건 
	AttendanceCorrection select(int attendanceCorrectionId);

	// 같은 날짜에 대기 중인 내 신청 수 (중복 신청 막기)
	int countPending(@Param("empId") int empId, @Param("workDate") LocalDate workDate);

	int insert(AttendanceCorrection correction);

	// 본인 취소 
	int cancel(@Param("attendanceCorrectionId") int attendanceCorrectionId, @Param("empId") int empId);

	// 내가 결재자인 신청
	List<AttendanceCorrection> selectApprovalList(@Param("approverId") int approverId, @Param("done") boolean done);

	int countApprovals(@Param("approverId") int approverId, @Param("done") boolean done);

	// 승인 / 반려 
	int approve(@Param("attendanceCorrectionId") int attendanceCorrectionId, @Param("approverId") Integer approverId,
				@Param("now") LocalDateTime now);

	int reject(@Param("attendanceCorrectionId") int attendanceCorrectionId, @Param("approverId") int approverId,
			   @Param("rejectReason") String rejectReason, @Param("now") LocalDateTime now);
}
