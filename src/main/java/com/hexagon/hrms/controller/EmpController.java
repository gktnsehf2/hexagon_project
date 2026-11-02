package com.hexagon.hrms.controller;

import java.io.File;
import java.time.Clock;
import java.time.LocalDate;

import javax.validation.Valid;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hexagon.hrms.common.web.Pager;
import com.hexagon.hrms.domain.Emp;
import com.hexagon.hrms.dto.request.EmpFormRequestDTO;
import com.hexagon.hrms.dto.request.EmpSearchRequestDTO;
import com.hexagon.hrms.security.AccountDetails;
import com.hexagon.hrms.security.PermissionCode;
import com.hexagon.hrms.service.DepartmentService;
import com.hexagon.hrms.service.EmpScope;
import com.hexagon.hrms.service.EmpService;
import com.hexagon.hrms.service.JobService;
import com.hexagon.hrms.service.PhotoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 사원(직원) 관련 화면
   GET  /employees                  F-03 목록 (S-03)
   GET  /employees/{empId}          F-05 상세 (S-05)        조회 범위 안의 사원만
   GET  /employees/new              F-04 등록 화면 (S-04)   PERMISSION_EMP_CREATE
   POST /employees/new              F-04 등록
   GET  /employees/{empId}/edit     F-04 수정 화면          PERMISSION_EMP_UPDATE
   POST /employees/{empId}/edit     F-04 수정
   GET  /employees/{empId}/photo    증명사진 이미지         로그인한 사람
 퇴직 처리(F-06)는 상세 화면의 팝업에서 fetch 로 부름 → RestEmpController
 URL 별 권한은 SecurityConfig 에서 검사
*/
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/employees")
public class EmpController {

	private final EmpService empService;
	private final JobService jobService;
	private final DepartmentService departmentService;
	private final PhotoService photoService;
	private final Clock clock;

	/*--------------------------------------------
	 F-03 사원 목록 조회·검색
	 검색 조건은 EmpSearchRequestDTO 로 자동 바인딩 (keyword, departmentId, jobId, status, currentPage)
	--------------------------------------------*/
	@GetMapping
	public String getList(@ModelAttribute("search") EmpSearchRequestDTO search,
						  @AuthenticationPrincipal AccountDetails loginUser, Model model) {

		EmpScope scope = empService.getScope(loginUser);
		Pager pager = empService.getEmpList(search, scope);

		model.addAttribute("pager", pager);
		model.addAttribute("scope", scope);
		model.addAttribute("departments", empService.getSelectableDepartments(scope));
		model.addAttribute("jobs", jobService.getAll());
		return "emp/list";
	}

	/*--------------------------------------------
	 F-05 사원 상세 (발령 이력 포함)
	--------------------------------------------*/
	@GetMapping("/{empId}")
	public String getDetail(@PathVariable("empId") int empId, @AuthenticationPrincipal AccountDetails loginUser, Model model) {
		Emp emp = empService.getVisibleEmp(empId, loginUser);

		model.addAttribute("emp", emp);
		model.addAttribute("tenure", empService.getTenure(emp));
		model.addAttribute("annualLeave", empService.getThisYearLeave(empId));
		model.addAttribute("appointments", empService.getAppointments(empId));
		// 계정 정보(아이디·Role·잠금)는 인사 담당만 볼 수 있게
		if (loginUser.hasAuthority(PermissionCode.EMP_UPDATE) || loginUser.hasAuthority(PermissionCode.EMP_CREATE)) {
			model.addAttribute("account", empService.getAccount(empId));
		}
		model.addAttribute("today", LocalDate.now(clock)); // 퇴직일 기본값
		return "emp/detail";
	}

	/*--------------------------------------------
	 F-04 사원 등록
	--------------------------------------------*/
	@GetMapping("/new")
	public String getRegisterForm(Model model) {
		EmpFormRequestDTO form = new EmpFormRequestDTO();
		form.setHiredate(LocalDate.now(clock));
		model.addAttribute("form", form);
		addFormOptions(model);
		return "emp/form";
	}

	// @Valid 검사 결과는 바로 뒤의 BindingResult 에 담김 → 오류가 있으면 입력값을 그대로 둔 채 화면으로 돌아감
	@PostMapping("/new")
	public String register(@Valid @ModelAttribute("form") EmpFormRequestDTO form, BindingResult result,
						   @AuthenticationPrincipal AccountDetails loginUser, Model model, RedirectAttributes ra) {
		checkRegister(form, result);
		if (result.hasErrors()) {
			addFormOptions(model);
			return "emp/form";
		}
		int empId = empService.register(form, loginUser);
		ra.addFlashAttribute("message", form.getEname() + " 사원을 등록했습니다. 계정 " + form.getLoginId() + " 으로 로그인할 수 있습니다.");
		return "redirect:/employees/" + empId;
	}

	// 등록 때만 받는 값 + 중복 검사
	private void checkRegister(EmpFormRequestDTO form, BindingResult result) {
		if (form.getHiredate() == null && !result.hasFieldErrors("hiredate")) {
			result.rejectValue("hiredate", "required", "입사일을 입력하세요.");
		}
		if (form.getDepartmentId() == null) {
			result.rejectValue("departmentId", "required", "부서를 선택하세요.");
		}
		if (form.getJobId() == null) {
			result.rejectValue("jobId", "required", "직급을 선택하세요.");
		}
		String loginId = form.getLoginId() == null ? "" : form.getLoginId().trim();
		if (!loginId.matches("[a-z0-9_]{4,25}")) {
			result.rejectValue("loginId", "pattern", "아이디는 영문 소문자·숫자·_ 로 4~25자 입력하세요.");
		} else if (empService.isLoginIdTaken(loginId)) {
			result.rejectValue("loginId", "duplicate", "이미 사용 중인 아이디입니다.");
		}
		if (form.getPassword() == null || form.getPassword().length() < 4 || form.getPassword().length() > 20) {
			result.rejectValue("password", "size", "초기 비밀번호는 4~20자로 입력하세요.");
		}
		if (!result.hasFieldErrors("email") && empService.isEmailTaken(form.getEmail(), 0)) {
			result.rejectValue("email", "duplicate", "이미 등록된 이메일입니다.");
		}
		checkPhoto(form, result);
	}

	private void checkPhoto(EmpFormRequestDTO form, BindingResult result) {
		if (form.hasPhoto()) {
			String error = photoService.validate(form.getPhoto());
			if (error != null) {
				result.rejectValue("photo", "invalid", error);
			}
		}
	}

	private void addFormOptions(Model model) {
		model.addAttribute("departments", departmentService.getAll());
		model.addAttribute("jobs", jobService.getAll());
	}

	/*--------------------------------------------
	 F-04 사원 수정 (이름 · 성별 · 이메일 · 연락처 · 사진)
	 부서·직급은 이력이 남도록 기본 정보 수정에서 바꾸지 않는다
	--------------------------------------------*/
	@GetMapping("/{empId}/edit")
	public String getEditForm(@PathVariable("empId") int empId, @AuthenticationPrincipal AccountDetails loginUser, Model model) {
		Emp emp = empService.getVisibleEmp(empId, loginUser);
		model.addAttribute("emp", emp);
		model.addAttribute("form", EmpFormRequestDTO.from(emp));
		return "emp/form";
	}

	@PostMapping("/{empId}/edit")
	public String update(@PathVariable("empId") int empId, @Valid @ModelAttribute("form") EmpFormRequestDTO form, BindingResult result,
						 @AuthenticationPrincipal AccountDetails loginUser, Model model, RedirectAttributes ra) {
		Emp emp = empService.getVisibleEmp(empId, loginUser);
		if (!result.hasFieldErrors("email") && empService.isEmailTaken(form.getEmail(), empId)) {
			result.rejectValue("email", "duplicate", "이미 등록된 이메일입니다.");
		}
		checkPhoto(form, result);
		if (result.hasErrors()) {
			model.addAttribute("emp", emp);
			return "emp/form";
		}
		empService.update(empId, form, loginUser);
		ra.addFlashAttribute("message", "사원 정보를 수정했습니다.");
		return "redirect:/employees/" + empId;
	}

	/*--------------------------------------------
	 증명사진 (사진이 없으면 404 → 화면은 이니셜 아바타를 보여줌)
	--------------------------------------------*/
	@GetMapping("/{empId}/photo")
	public ResponseEntity<Resource> getPhoto(@PathVariable("empId") int empId) {
		Emp emp = empService.getPhotoOwner(empId);
		File file = emp == null ? null : photoService.getFile(emp.getPhotoPath());
		if (file == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok()
			.contentType(MediaType.parseMediaType(PhotoService.getContentType(file)))
			.cacheControl(CacheControl.noCache()) // 사진을 바꾸면 바로 보이도록
			.body(new FileSystemResource(file));
	}
}
