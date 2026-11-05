package com.hexagon.hrms.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexagon.hrms.domain.Holiday;
import com.hexagon.hrms.dto.response.CalendarLeaveDTO;
import com.hexagon.hrms.exception.BusinessException;
import com.hexagon.hrms.mapper.CalendarMapper;
import com.hexagon.hrms.mapper.HolidayMapper;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CalendarService {

	private static final int MAX_DAYS = 100;

	private final CalendarMapper calendarMapper;
	private final HolidayMapper holidayMapper;

	public List<Map<String, Object>> getEvents(LocalDate from, LocalDate to) {
		if (from == null || to == null || !to.isAfter(from)) {
			throw new BusinessException("조회 기간이 올바르지 않습니다.");
		}
		if (ChronoUnit.DAYS.between(from, to) > MAX_DAYS) {
			throw new BusinessException("한 번에 " + MAX_DAYS + "일까지만 조회할 수 있습니다.");
		}
		LocalDate last = to.minusDays(1);
		List<Map<String, Object>> events = new ArrayList<>();

		Map<LocalDate, String> holidays = new HashMap<>();
		for (Holiday holiday : holidayMapper.selectBetween(from, last)) {
			holidays.put(holiday.getHolidayDate(), holiday.getHolidayName());
			Map<String, Object> event = new LinkedHashMap<>();
			event.put("id", "H" + holiday.getHolidayDate());
			event.put("title", holiday.getHolidayName());
			event.put("start", holiday.getHolidayDate().toString());
			event.put("allDay", true);
			event.put("display", "background");
			event.put("classNames", new String[] { "hr-cal-holiday" });
			event.put("extendedProps", Map.of("kind", "HOLIDAY"));
			events.add(event);
		}

		for (CalendarLeaveDTO leave : calendarMapper.selectApprovedLeaves(from, last)) {
			LocalDate d = leave.getStartDate().isBefore(from) ? from : leave.getStartDate();
			LocalDate end = leave.getEndDate().isAfter(last) ? last : leave.getEndDate();
			for (; !d.isAfter(end); d = d.plusDays(1)) {
				if (isWeekend(d) || holidays.containsKey(d)) {
					continue;
				}
				events.add(toEvent(leave, d));
			}
		}
		return events;
	}

	private Map<String, Object> toEvent(CalendarLeaveDTO leave, LocalDate date) {
		Map<String, Object> event = new LinkedHashMap<>();
		event.put("id", "L" + leave.getLeaveRequestId() + "-" + date);
		event.put("groupId", "L" + leave.getLeaveRequestId());
		event.put("title", "[" + leave.getShortTypeName() + "]" + leave.getEname());

		String code = leave.getLeaveTypeCode();
		if ("HALF_AM".equals(code) || "HALF_PM".equals(code)) {
			LocalTime start = "HALF_AM".equals(code) ? AttendanceService.WORK_START : AttendanceService.HALF_DAY_TIME;
			LocalTime end = "HALF_AM".equals(code) ? AttendanceService.HALF_DAY_TIME : AttendanceService.WORK_END;
			event.put("start", date.atTime(start).toString());
			event.put("end", date.atTime(end).toString());
			event.put("allDay", false);
		} else {
			event.put("start", date.toString());
			event.put("allDay", true);
		}
		event.put("classNames", new String[] { "hr-cal-leave", "is-" + code.toLowerCase().replace('_', '-') });

		Map<String, Object> props = new LinkedHashMap<>();
		props.put("kind", "LEAVE");
		props.put("ename", leave.getEname());
		props.put("departmentName", leave.getDepartmentName());
		props.put("leaveTypeName", leave.getLeaveTypeName());
		props.put("startDate", leave.getStartDate().toString());
		props.put("endDate", leave.getEndDate().toString());
		props.put("days", leave.getDays().stripTrailingZeros().toPlainString());
		event.put("extendedProps", props);
		return event;
	}

	private boolean isWeekend(LocalDate d) {
		return d.getDayOfWeek() == DayOfWeek.SATURDAY || d.getDayOfWeek() == DayOfWeek.SUNDAY;
	}
}
