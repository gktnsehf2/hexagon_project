package com.hexagon.hrms.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexagon.hrms.domain.Attendance;
import com.hexagon.hrms.domain.AttendanceCorrection;
import com.hexagon.hrms.domain.Emp;
import com.hexagon.hrms.dto.request.AttendanceCorrectionFormDTO;
import com.hexagon.hrms.exception.BusinessException;
import com.hexagon.hrms.exception.ErrorCode;
import com.hexagon.hrms.mapper.AttendanceCorrectionMapper;
import com.hexagon.hrms.mapper.AttendanceMapper;
import com.hexagon.hrms.mapper.EmpMapper;
import com.hexagon.hrms.security.AccountDetails;
import com.hexagon.hrms.security.PermissionCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AttendanceCorrectionService {

	private final AttendanceCorrectionMapper correctionMapper;
	private final AttendanceMapper attendanceMapper;
	private final AttendanceService attendanceService;
	private final LeaveService leaveService;    
	private final EmpMapper empMapper;
	private final Clock clock;

	public LocalDate today() {
		return LocalDate.now(clock);
	}

	@Transactional(readOnly = true)
	public List<AttendanceCorrection> getMyList(int empId) {
		return correctionMapper.selectMyList(empId);
	}

	@Transactional(readOnly = true)
	public Attendance getRecord(int empId, LocalDate workDate) {
		return workDate == null ? null : attendanceMapper.selectByEmpAndDate(empId, workDate);
	}

	@Transactional(readOnly = true)
	public Emp findApprover(int empId) {
		return leaveService.findApprover(empId);
	}

	// 신청
	public AttendanceCorrection request(AccountDetails loginUser, AttendanceCorrectionFormDTO form) {
		if (!loginUser.hasAuthority(PermissionCode.ATT_CORRECT_REQUEST)) {
			throw new BusinessException("근태 정정 신청 권한이 없습니다.");
		}
		int empId = loginUser.getEmpId();
		Emp me = empMapper.select(empId);
		LocalDate today = LocalDate.now(clock);
		LocalDate workDate = form.getWorkDate();

		if (workDate == null) {
			throw new BusinessException("정정할 근무일을 선택하세요.");
		}
		if (workDate.isAfter(today)) {
			throw new BusinessException("오늘 이후 날짜는 정정 신청할 수 없습니다.");
		}
		if (workDate.isBefore(AttendanceService.RECORD_START) || (me != null && workDate.isBefore(me.getHiredate()))) {
			throw new BusinessException("근태 기록이 없는 기간입니다. (근태 시스템 도입일 " + AttendanceService.RECORD_START + ", 입사일 이후만 가능)");
		}
		LocalDateTime in = toDateTime(workDate, form.getCheckIn(), "출근");
		LocalDateTime out = toDateTime(workDate, form.getCheckOut(), "퇴근");
		if (in == null && out == null) {
			throw new BusinessException("정정할 출근 시각이나 퇴근 시각을 하나 이상 입력하세요.");
		}

		Attendance record = attendanceMapper.selectByEmpAndDate(empId, workDate);
		LocalDateTime afterIn = in != null ? in : (record == null ? null : record.getCheckInAt());
		LocalDateTime afterOut = out != null ? out : (record == null ? null : record.getCheckOutAt());
		if (afterIn == null) {
			throw new BusinessException("그날 출근 기록이 없어 출근 시각을 함께 입력해야 합니다.");
		}
		if (afterOut != null && !afterOut.isAfter(afterIn)) {
			throw new BusinessException("퇴근 시각은 출근 시각보다 늦어야 합니다.");
		}
		if (record != null && afterIn.equals(record.getCheckInAt()) && afterOut != null && afterOut.equals(record.getCheckOutAt())) {
			throw new BusinessException("현재 기록과 같은 시각입니다. 바꿀 시각을 입력하세요.");
		}
		String reason = form.getReason() == null ? "" : form.getReason().trim();
		if (reason.isEmpty()) {
			throw new BusinessException("정정 사유를 입력하세요.");
		}
		if (reason.length() > 200) {
			throw new BusinessException("정정 사유는 200자까지 입력할 수 있습니다.");
		}
		if (correctionMapper.countPending(empId, workDate) > 0) {
			throw new BusinessException(workDate + "에 이미 결재 대기 중인 정정 신청이 있습니다.");
		}

		LocalDateTime now = LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS);
		AttendanceCorrection correction = new AttendanceCorrection();
		Emp emp = new Emp();
		emp.setEmpId(empId);
		correction.setEmp(emp);
		correction.setWorkDate(workDate);
		correction.setRequestCheckInAt(in);
		correction.setRequestCheckOutAt(out);
		correction.setReason(reason);
		correction.setRequestDate(now);
		correction.setApprover(findApprover(empId));
		correctionMapper.insert(correction);
		correction.setStatus("REQUESTED");

		// 결재자가 없으면(회장 본인) 바로 승인·반영
		if (correction.getApprover() == null) {
			correctionMapper.approve(correction.getAttendanceCorrectionId(), null, now);
			attendanceService.applyCorrection(empId, workDate, in, out);
			correction.setStatus("APPROVED");
		}
		log.info("근태 정정 신청 {} empId={} {} 출근={} 퇴근={}", correction.getRequestNo(), empId, workDate, in, out);
		return correction;
	}
	public AttendanceCorrection cancel(int correctionId, AccountDetails loginUser) {
		AttendanceCorrection correction = correctionMapper.select(correctionId);
		if (correction == null || correction.getEmp().getEmpId() != loginUser.getEmpId()) {
			throw new BusinessException("본인이 신청한 정정만 취소할 수 있습니다.");
		}
		if (!correction.isCancelable()) {
			throw new BusinessException("이미 " + correction.getStatusName() + " 처리된 신청은 취소할 수 없습니다.");
		}
		if (correctionMapper.cancel(correctionId, loginUser.getEmpId()) == 0) {
			throw new BusinessException(ErrorCode.CONFLICT, "방금 결재가 처리되어 취소할 수 없습니다. 새로고침 후 확인해 주세요.");
		}
		correction.setStatus("CANCELED");
		return correction;
	}

	@Transactional(readOnly = true)
	public List<AttendanceCorrection> getApprovals(int approverId, boolean done) {
		return correctionMapper.selectApprovalList(approverId, done);
	}

	@Transactional(readOnly = true)
	public int countApprovals(int approverId, boolean done) {
		return correctionMapper.countApprovals(approverId, done);
	}

	public AttendanceCorrection approve(int correctionId, AccountDetails loginUser) {
		AttendanceCorrection correction = getMyApproval(correctionId, loginUser);
		LocalDateTime now = LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS);
		if (correctionMapper.approve(correctionId, loginUser.getEmpId(), now) == 0) {
			throw new BusinessException(ErrorCode.CONFLICT, "방금 다른 처리가 되어 승인할 수 없습니다. 새로고침 후 확인해 주세요.");
		}
		Attendance applied = attendanceService.applyCorrection(correction.getEmp().getEmpId(), correction.getWorkDate(),
			correction.getRequestCheckInAt(), correction.getRequestCheckOutAt());
		correction.setStatus("APPROVED");
		correction.setCurrentCheckInAt(applied.getCheckInAt());
		correction.setCurrentCheckOutAt(applied.getCheckOutAt());
		log.info("근태 정정 승인 {} by empId={} → {}", correction.getRequestNo(), loginUser.getEmpId(), applied.getStatus());
		return correction;
	}

	public AttendanceCorrection reject(int correctionId, String rejectReason, AccountDetails loginUser) {
		String reason = rejectReason == null ? "" : rejectReason.trim();
		if (reason.isEmpty()) {
			throw new BusinessException("반려 사유를 입력하세요.");
		}
		if (reason.length() > 200) {
			throw new BusinessException("반려 사유는 200자까지 입력할 수 있습니다.");
		}
		AttendanceCorrection correction = getMyApproval(correctionId, loginUser);
		LocalDateTime now = LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS);
		if (correctionMapper.reject(correctionId, loginUser.getEmpId(), reason, now) == 0) {
			throw new BusinessException(ErrorCode.CONFLICT, "방금 다른 처리가 되어 반려할 수 없습니다. 새로고침 후 확인해 주세요.");
		}
		correction.setStatus("REJECTED");
		correction.setRejectReason(reason);
		log.info("근태 정정 반려 {} by empId={}", correction.getRequestNo(), loginUser.getEmpId());
		return correction;
	}

	private AttendanceCorrection getMyApproval(int correctionId, AccountDetails loginUser) {
		if (!loginUser.hasAuthority(PermissionCode.ATT_CORRECT_APPROVE)) {
			throw new BusinessException("근태 결재 권한이 없습니다.");
		}
		AttendanceCorrection correction = correctionMapper.select(correctionId);
		if (correction == null || correction.getApprover() == null || correction.getApprover().getEmpId() != loginUser.getEmpId()) {
			throw new BusinessException("내가 결재자로 지정된 신청만 처리할 수 있습니다.");
		}
		if (!correction.isCancelable()) {
			throw new BusinessException("이미 " + correction.getStatusName() + " 처리된 신청입니다.");
		}
		return correction;
	}
	
	private LocalDateTime toDateTime(LocalDate date, String time, String label) {
		if (time == null || time.trim().isEmpty()) {
			return null;
		}
		try {
			return date.atTime(LocalTime.parse(time.trim()).truncatedTo(ChronoUnit.MINUTES));
		} catch (DateTimeParseException e) {
			throw new BusinessException(label + " 시각 형식이 올바르지 않습니다. (예: 09:00)");
		}
	}
}
