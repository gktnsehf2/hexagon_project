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

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class RestCalendarController {

	private final CalendarService calendarService;

	@GetMapping("/events")
	public List<Map<String, Object>> events(@RequestParam("start") String start, @RequestParam("end") String end) {
		return calendarService.getEvents(toDate(start), toDate(end));
	}

	private LocalDate toDate(String value) {
		try {
			return LocalDate.parse(value.length() > 10 ? value.substring(0, 10) : value);
		} catch (DateTimeParseException e) {
			throw new BusinessException("날짜 형식이 올바르지 않습니다: " + value);
		}
	}
}
