package com.hexagon.hrms.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.hexagon.hrms.domain.AttendanceCorrection;

@Mapper
public interface AttendanceCorrectionMapper {
	// 내 정정 신청 (최근 신청 순 100건)
	List<AttendanceCorrection> selectMyList(int empId);

	// 한 건 (없으면 null)
	AttendanceCorrection select(int attendanceCorrectionId);

	// 같은 날짜에 대기 중인 내 신청 수 (중복 신청 막기)
	int countPending(@Param("empId") int empId, @Param("workDate") LocalDate workDate);

	int insert(AttendanceCorrection correction);

	// 본인 취소 (대기 중인 건만 → 0 이면 이미 처리됨)
	int cancel(@Param("attendanceCorrectionId") int attendanceCorrectionId, @Param("empId") int empId);

	// 내가 결재자인 신청 (done = false 대기, true 처리 완료 최근 100건)
	List<AttendanceCorrection> selectApprovalList(@Param("approverId") int approverId, @Param("done") boolean done);

	int countApprovals(@Param("approverId") int approverId, @Param("done") boolean done);

	// 승인 / 반려 (지정된 결재자 + 대기 중인 건만 → 0 이면 이미 처리됨)
	int approve(@Param("attendanceCorrectionId") int attendanceCorrectionId, @Param("approverId") Integer approverId,
				@Param("now") LocalDateTime now);

	int reject(@Param("attendanceCorrectionId") int attendanceCorrectionId, @Param("approverId") int approverId,
			   @Param("rejectReason") String rejectReason, @Param("now") LocalDateTime now);
}
