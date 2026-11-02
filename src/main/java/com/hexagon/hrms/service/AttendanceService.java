package com.hexagon.hrms.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexagon.hrms.domain.Attendance;
import com.hexagon.hrms.domain.Holiday;
import com.hexagon.hrms.domain.LeaveRequest;
import com.hexagon.hrms.domain.LeaveType;
import com.hexagon.hrms.dto.response.MyAttendanceDayDTO;
import com.hexagon.hrms.dto.response.MyAttendanceMonthDTO;
import com.hexagon.hrms.dto.response.TodayAttendanceDTO;
import com.hexagon.hrms.exception.BusinessException;
import com.hexagon.hrms.mapper.AttendanceMapper;
import com.hexagon.hrms.mapper.HolidayMapper;
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
 F-09 내 근태 조회 (S-08)
   - 근태 기록이 없는 평일은 결근 (승인된 휴가·공휴일·오늘은 제외)
   - 근태 시스템 도입일(RECORD_START) 전 날짜는 기록이 없으므로 결근으로 보지 않음
*/
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AttendanceService {

	public static final LocalTime WORK_START = LocalTime.of(9, 0);
	public static final LocalTime WORK_END = LocalTime.of(18, 0);
	public static final LocalTime HALF_DAY_TIME = LocalTime.of(14, 0);
	public static final LocalDate RECORD_START = LocalDate.of(2026, 10, 1); // 근태 시스템 도입일

	private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

	private final AttendanceMapper attendanceMapper;
	private final LeaveRequestMapper leaveRequestMapper;
	private final HolidayMapper holidayMapper;
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

	// 내 근태 월 조회. month 가 조회 가능한 범위를 벗어나면 가장 가까운 달로 맞춘다
	@Transactional(readOnly = true)
	public MyAttendanceMonthDTO getMyMonth(int empId, LocalDate hiredate, YearMonth month) {
		LocalDate today = LocalDate.now(clock);
		YearMonth maxMonth = YearMonth.from(today);
		YearMonth minMonth = YearMonth.from(hiredate != null && hiredate.isAfter(RECORD_START) ? hiredate : RECORD_START);
		if (minMonth.isAfter(maxMonth)) {
			minMonth = maxMonth;
		}
		if (month == null || month.isAfter(maxMonth)) {
			month = maxMonth;
		}
		if (month.isBefore(minMonth)) {
			month = minMonth;
		}

		MyAttendanceMonthDTO dto = new MyAttendanceMonthDTO();
		dto.setMonth(month);
		dto.setMinMonth(minMonth);
		dto.setMaxMonth(maxMonth);
		dto.setPrevMonth(month.isAfter(minMonth) ? month.minusMonths(1) : null);
		dto.setNextMonth(month.isBefore(maxMonth) ? month.plusMonths(1) : null);

		LocalDate from = month.atDay(1);
		LocalDate to = month.atEndOfMonth().isAfter(today) ? today : month.atEndOfMonth(); // 미래 날짜는 안 보여줌

		// 기간의 근태 · 승인된 휴가 · 공휴일을 한 번씩만 읽어서 날짜별로 맞춘다
		Map<LocalDate, Attendance> records = new HashMap<>();
		for (Attendance attendance : attendanceMapper.selectByEmpBetween(empId, from, to)) {
			records.put(attendance.getWorkDate(), attendance);
		}
		Map<LocalDate, List<LeaveRequest>> leaves = new HashMap<>();
		for (LeaveRequest leave : leaveRequestMapper.selectApprovedBetween(empId, from, to)) {
			for (LocalDate d = leave.getStartDate(); !d.isAfter(leave.getEndDate()); d = d.plusDays(1)) {
				leaves.computeIfAbsent(d, k -> new ArrayList<>()).add(leave);
			}
		}
		Map<LocalDate, String> holidays = new HashMap<>();
		for (Holiday holiday : holidayMapper.selectBetween(from, to)) {
			holidays.put(holiday.getHolidayDate(), holiday.getHolidayName());
		}

		for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
			if (hiredate != null && d.isBefore(hiredate)) {
				continue;
			}
			MyAttendanceDayDTO day = makeDay(d, today, records.get(d), leaves.get(d), holidays.get(d));
			if (day == null) {
				continue; // 기록 없는 주말, 도입일 전
			}
			dto.getDays().add(day);
			count(dto, day);
		}
		return dto;
	}

	// 하루 한 줄 만들기 (보여줄 필요가 없는 날이면 null)
	private MyAttendanceDayDTO makeDay(LocalDate d, LocalDate today, Attendance record, List<LeaveRequest> leaves, String holidayName) {
		MyAttendanceDayDTO day = new MyAttendanceDayDTO();
		day.setWorkDate(d);
		day.setToday(d.equals(today));
		day.setAttendance(record);

		// 그날 승인된 휴가: 반차 두 건이면 하루 휴가와 같음
		String leaveName = null;
		boolean fullDayLeave = false;
		int halfCount = 0;
		if (leaves != null) {
			for (LeaveRequest leave : leaves) {
				leaveName = leaveName == null ? leave.getLeaveType().getLeaveTypeName() : leaveName + ", " + leave.getLeaveType().getLeaveTypeName();
				if (leave.getLeaveType().isHalfDay()) {
					halfCount++;
				} else {
					fullDayLeave = true;
				}
			}
		}
		if (halfCount >= 2) {
			fullDayLeave = true;
		}
		boolean weekend = day.isWeekend();

		if (record != null && record.getCheckInAt() != null) {
			day.setDayStatus(record.getStatus());
			day.setNote(leaveName != null ? leaveName : (holidayName != null ? holidayName + " 근무" : (weekend ? "주말 근무" : null)));
			if (record.getCheckOutAt() == null && !day.isToday()) {
				day.setNote(day.getNote() == null ? "퇴근 기록 없음" : day.getNote() + " · 퇴근 기록 없음");
			}
			setWorkMinutes(day, record);
		} else if (fullDayLeave || (record != null && "ON_LEAVE".equals(record.getStatus()))) {
			day.setDayStatus("ON_LEAVE");
			day.setNote(leaveName != null ? leaveName : "휴가");
		} else if (weekend) {
			return null;
		} else if (holidayName != null) {
			day.setDayStatus("HOLIDAY");
			day.setNote(holidayName);
		} else if (day.isToday()) {
			day.setDayStatus("BEFORE");
			day.setNote(leaveName);
		} else if (d.isBefore(RECORD_START)) {
			return null;
		} else {
			day.setDayStatus("ABSENT");
			day.setNote(leaveName != null ? leaveName + " (출근 기록 없음)" : null);
		}
		return day;
	}

	// 근무 시간 = 퇴근 - 출근 - 점심 1시간(12~13시에 걸쳐 있으면), 초과근무 = 18:00 이후 30분 단위 내림
	private void setWorkMinutes(MyAttendanceDayDTO day, Attendance record) {
		if (record.getCheckInAt() == null || record.getCheckOutAt() == null) {
			return;
		}
		LocalTime in = record.getCheckInAt().toLocalTime();
		LocalTime out = record.getCheckOutAt().toLocalTime();
		int minutes = (int) ChronoUnit.MINUTES.between(record.getCheckInAt(), record.getCheckOutAt());
		if (!in.isAfter(LocalTime.NOON) && !out.isBefore(LocalTime.of(13, 0))) {
			minutes -= 60;
		}
		day.setWorkMinutes(Math.max(minutes, 0));

		LocalDateTime workEnd = record.getWorkDate().atTime(WORK_END);
		if (record.getCheckOutAt().isAfter(workEnd)) {
			int over = (int) ChronoUnit.MINUTES.between(workEnd, record.getCheckOutAt());
			day.setOvertimeMinutes(over / 30 * 30);
		}
	}

	private void count(MyAttendanceMonthDTO dto, MyAttendanceDayDTO day) {
		if (day.getAttendance() != null && day.getAttendance().getCheckInAt() != null) {
			dto.setWorkDays(dto.getWorkDays() + 1);
		}
		switch (day.getDayStatus()) {
		case "NORMAL":
			dto.setNormalCount(dto.getNormalCount() + 1);
			break;
		case "LATE":
			dto.setLateCount(dto.getLateCount() + 1);
			break;
		case "EARLY_LEAVE":
			dto.setEarlyLeaveCount(dto.getEarlyLeaveCount() + 1);
			break;
		case "ABSENT":
			dto.setAbsentCount(dto.getAbsentCount() + 1);
			break;
		case "ON_LEAVE":
			dto.setLeaveDays(dto.getLeaveDays() + 1);
			break;
		default:
			break;
		}
		dto.setTotalWorkMinutes(dto.getTotalWorkMinutes() + day.getWorkMinutes());
		dto.setTotalOvertimeMinutes(dto.getTotalOvertimeMinutes() + day.getOvertimeMinutes());
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
