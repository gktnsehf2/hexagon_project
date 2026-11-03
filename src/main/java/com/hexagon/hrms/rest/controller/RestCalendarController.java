package com.hexagon.hrms.rest.controller;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hexagon.hrms.exception.BusinessException;
import com.hexagon.hrms.service.CalendarService;

import lombok.RequiredArgsConstructor;

/*
 F-17 휴가 캘린더 일정 (FullCalendar 가 보기를 바꿀 때마다 자동으로 호출)
 요청: GET /api/calendar/events?start=2026-10-26T00:00:00+09:00&end=2026-12-07T00:00:00+09:00
 응답: [{id, title, start, end?, allDay, display?, classNames, extendedProps}, ...]  (FullCalendar 이벤트 형식)
 실패: 400 {result: "fail", message}  ← RestExceptionHandler
*/
@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class RestCalendarController {

	private final CalendarService calendarService;

	@GetMapping("/events")
	public List<Map<String, Object>> events(@RequestParam("start") String start, @RequestParam("end") String end) {
		return calendarService.getEvents(toDate(start), toDate(end));
	}

	// "2026-10-26T00:00:00+09:00" 또는 "2026-10-26" → 앞의 날짜만
	private LocalDate toDate(String value) {
		try {
			return LocalDate.parse(value.length() > 10 ? value.substring(0, 10) : value);
		} catch (DateTimeParseException e) {
			throw new BusinessException("날짜 형식이 올바르지 않습니다: " + value);
		}
	}
}
