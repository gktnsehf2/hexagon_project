package com.hexagon.hrms.dto.request;

import java.time.LocalDate;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import com.hexagon.hrms.domain.Emp;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class EmpFormRequestDTO {

	@NotBlank(message = "이름을 입력하세요.")
	@Size(max = 30, message = "이름은 30자까지 입력할 수 있습니다.")
	private String ename;

	@NotBlank(message = "성별을 선택하세요.")
	@Pattern(regexp = "[MF]", message = "성별을 선택하세요.")
	private String gender;

	@Email(message = "이메일 형식이 올바르지 않습니다.")
	@Size(max = 100, message = "이메일은 100자까지 입력할 수 있습니다.")
	private String email;

	@Pattern(regexp = "^$|^01[016789]-\\d{3,4}-\\d{4}$", message = "연락처는 010-0000-0000 형식으로 입력하세요.")
	private String phone;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 

	private Integer departmentId;
	private Integer jobId;
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate changeDate;    
	private String changeReason;  
	private String loginId;
	private String password;

	private MultipartFile photo;

	public static EmpFormRequestDTO from(Emp emp) {
		EmpFormRequestDTO form = new EmpFormRequestDTO();
		form.setEname(emp.getEname());
		form.setGender(emp.getGender());
		form.setEmail(emp.getEmail());
		form.setPhone(emp.getPhone());
		form.setHiredate(emp.getHiredate());
		form.setDepartmentId(emp.getDepartment().getDepartmentId());
		form.setJobId(emp.getJob().getJobId());
		form.setChangeDate(LocalDate.now());
		return form;
	}

	public boolean hasPhoto() {
		return photo != null && !photo.isEmpty();
	}
}
