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

/*
 사원 등록 / 수정 화면(S-04) 입력값
 - 공통 항목은 어노테이션으로 검사 (@Valid)
 - 등록 때만 받는 항목(입사일, 부서, 직급, 아이디, 초기 비밀번호)은 EmpController.checkRegister 에서 검사
   → 수정 화면에서는 이 값들을 바꾸지 않음 (부서·직급은 인사 발령에서 변경)
*/
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

	// 비워 두거나 010-1234-5678 형식
	@Pattern(regexp = "^$|^01[016789]-\\d{3,4}-\\d{4}$", message = "연락처는 010-0000-0000 형식으로 입력하세요.")
	private String phone;

	// ---------- 등록 때만 (입사일·계정)
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) // <input type="date"> 값 (yyyy-MM-dd)
	private LocalDate hiredate;
	// 부서·직급: 등록 때 필수, 수정 때 바꾸면 인사 이력(승진·전보)이 남음
	private Integer departmentId;
	private Integer jobId;
	// ---------- 수정에서 부서·직급을 바꿀 때만 (검사는 EmpService.update)
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate changeDate;    // 적용일 (인사 이력의 일자)
	private String changeReason;     // 변경 사유 (필수)
	private String loginId;
	private String password;

	// 증명사진 (선택). 수정 때 비워 두면 기존 사진 유지
	private MultipartFile photo;

	// 수정 화면을 열 때 현재 값 채우기
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
