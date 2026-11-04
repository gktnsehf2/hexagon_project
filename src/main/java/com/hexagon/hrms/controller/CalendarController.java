package com.hexagon.hrms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

//휴가 캘린더
@Controller
public class CalendarController {

	@GetMapping("/calendar")
	public String calendar() {
		return "attendance/calendar";
	}
}
