package com.hexagon.hrms.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexagon.hrms.domain.Attendance;
import com.hexagon.hrms.domain.LeaveType;
import com.hexagon.hrms.dto.response.TodayAttendanceDTO;
import com.hexagon.hrms.exception.BusinessException;
import com.hexagon.hrms.mapper.AttendanceMapper;
import com.hexagon.hrms.mapper.LeaveRequestMapper;
import com.hexagon.hrms.security.AccountDetails;
import com.hexagon.hrms.security.PermissionCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 F-08 출퇴근 기록
 근무 시간 09:00 ~ 18:00
   - 09:01 부터 출근하면 지각 (분 단위로 판단)
   - 18:00 전에 퇴근하면 조퇴 (지각한 날은 지각으로 남김)
   - 오전 반차: 14:00 까지 출근 / 오후 반차: 14:00 부터 퇴근 가능
   - 하루 휴가(연차, 병가, 경조사, 공가)인 날은 출근할 수 없음
*/
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AttendanceService {

	public static final LocalTime WORK_START = LocalTime.of(9, 0);
	public static final LocalTime WORK_END = LocalTime.of(18, 0);
	public static final LocalTime HALF_DAY_TIME = LocalTime.of(14, 0);

	private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

	private final AttendanceMapper attendanceMapper;
	private final LeaveRequestMapper leaveRequestMapper;
	private final Clock clock;

	// 대시보드 시계 카드: 내 오늘 출퇴근 상태
	@Transactional(readOnly = true)
	public TodayAttendanceDTO getMyToday(int empId) {
		LocalDate today = LocalDate.now(clock);
		TodaySchedule schedule = getSchedule(empId, today);
		Attendance attendance = attendanceMapper.selectByEmpAndDate(empId, today);

		TodayAttendanceDTO dto = new TodayAttendanceDTO();
		dto.setLeaveTypeName(schedule.leaveName);
		dto.setWorkStart(schedule.start);
		dto.setWorkEnd(schedule.end);

		if (attendance != null) {
			dto.setStatus(attendance.getStatus());
			dto.setCheckInAt(attendance.getCheckInAt());
			dto.setCheckOutAt(attendance.getCheckOutAt());
		}

		if (attendance != null && attendance.getCheckOutAt() != null) {
			dto.setState("DONE");
		} else if (attendance != null && attendance.getCheckInAt() != null) {
			dto.setState("WORKING");
		} else if (schedule.dayOff || (attendance != null && "ON_LEAVE".equals(attendance.getStatus()))) {
			dto.setState("LEAVE");
		} else {
			dto.setState("BEFORE");
		}
		return dto;
	}

	// 출근
	public Attendance checkIn(AccountDetails loginUser) {
		checkPermission(loginUser);
		int empId = loginUser.getEmpId();
		LocalDateTime now = LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS); // DB 는 초 단위까지만 저장
		LocalDate today = now.toLocalDate();

		TodaySchedule schedule = getSchedule(empId, today);
		if (schedule.dayOff) {
			throw new BusinessException("오늘은 " + schedule.leaveName + " 휴가일이라 출근할 수 없습니다.");
		}

		Attendance attendance = attendanceMapper.selectByEmpAndDate(empId, today);
		if (attendance != null && attendance.getCheckInAt() != null) {
			throw new BusinessException("이미 " + attendance.getCheckInAt().format(HH_MM) + "에 출근했습니다.");
		}

		// 09:00:59 까지는 정상, 09:01:00 부터 지각
		boolean late = now.toLocalTime().truncatedTo(ChronoUnit.MINUTES).isAfter(schedule.start);
		String status = late ? "LATE" : "NORMAL";

		if (attendance == null) {
			attendance = new Attendance();
			attendance.setEmp(loginUser.getAccount().getEmp());
			attendance.setWorkDate(today);
			attendance.setCheckInAt(now);
			attendance.setStatus(status);
			try {
				attendanceMapper.insert(attendance);
			} catch (DuplicateKeyException e) {
				// 버튼을 빠르게 두 번 누른 경우 등 (uq_attendance)
				throw new BusinessException("이미 출근 처리되었습니다.");
			}
		} else {
			attendance.setCheckInAt(now);
			attendance.setStatus(status);
			if (attendanceMapper.updateCheckIn(attendance) == 0) {
				throw new BusinessException("이미 출근 처리되었습니다.");
			}
		}
		log.debug("출근 empId={} {} {}", empId, now, status);
		return attendance;
	}

	// 퇴근
	public Attendance checkOut(AccountDetails loginUser) {
		checkPermission(loginUser);
		int empId = loginUser.getEmpId();
		LocalDateTime now = LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS);
		LocalDate today = now.toLocalDate();

		Attendance attendance = attendanceMapper.selectByEmpAndDate(empId, today);
		if (attendance == null || attendance.getCheckInAt() == null) {
			throw new BusinessException("오늘 출근 기록이 없습니다. 먼저 출근해 주세요.");
		}
		if (attendance.getCheckOutAt() != null) {
			throw new BusinessException("이미 " + attendance.getCheckOutAt().format(HH_MM) + "에 퇴근했습니다.");
		}

		TodaySchedule schedule = getSchedule(empId, today);
		attendance.setCheckOutAt(now);
		if ("NORMAL".equals(attendance.getStatus()) && now.toLocalTime().isBefore(schedule.end)) {
			attendance.setStatus("EARLY_LEAVE");
		}
		if (attendanceMapper.updateCheckOut(attendance) == 0) {
			throw new BusinessException("이미 퇴근 처리되었습니다.");
		}
		log.debug("퇴근 empId={} {} {}", empId, now, attendance.getStatus());
		return attendance;
	}

	// URL 권한(SecurityConfig)과 별개로 서비스에서도 한 번 더 확인
	private void checkPermission(AccountDetails loginUser) {
		if (!loginUser.hasAuthority(PermissionCode.ATT_CHECK)) {
			throw new BusinessException("출퇴근 기록 권한이 없습니다.");
		}
	}

	// 오늘 승인된 휴가를 반영한 근무 시간
	private TodaySchedule getSchedule(int empId, LocalDate date) {
		List<LeaveType> leaves = leaveRequestMapper.selectApprovedTypesOn(empId, date);
		TodaySchedule schedule = new TodaySchedule();
		boolean halfAm = false;
		boolean halfPm = false;
		for (LeaveType leave : leaves) {
			schedule.leaveName = leave.getLeaveTypeName();
			if ("HALF_AM".equals(leave.getLeaveTypeCode())) {
				halfAm = true;
			} else if ("HALF_PM".equals(leave.getLeaveTypeCode())) {
				halfPm = true;
			} else {
				schedule.dayOff = true;
			}
		}
		if (halfAm && halfPm) { // 오전 반차 + 오후 반차 = 하루
			schedule.dayOff = true;
			schedule.leaveName = "반차";
		}
		schedule.start = halfAm ? HALF_DAY_TIME : WORK_START;
		schedule.end = halfPm ? HALF_DAY_TIME : WORK_END;
		return schedule;
	}

	private static class TodaySchedule {
		private boolean dayOff;    // 하루 휴가
		private String leaveName;  // 휴가 유형 이름 (없으면 null)
		private LocalTime start;
		private LocalTime end;
	}
}
