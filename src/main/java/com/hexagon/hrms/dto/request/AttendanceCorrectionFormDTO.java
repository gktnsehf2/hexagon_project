package com.hexagon.hrms.dto.request;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttendanceCorrectionFormDTO {
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate workDate;
	private String checkIn;  
	private String checkOut;  
	private String reason;
}
