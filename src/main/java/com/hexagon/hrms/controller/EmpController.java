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
import com.hexagon.hrms.exception.BusinessException;
import com.hexagon.hrms.security.AccountDetails;
import com.hexagon.hrms.security.PermissionCode;
import com.hexagon.hrms.service.DepartmentService;
import com.hexagon.hrms.service.EmpScope;
import com.hexagon.hrms.service.EmpService;
import com.hexagon.hrms.service.JobService;
import com.hexagon.hrms.service.PhotoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

	@GetMapping("/{empId}")
	public String getDetail(@PathVariable("empId") int empId, @AuthenticationPrincipal AccountDetails loginUser, Model model) {
		Emp emp = empService.getVisibleEmp(empId, loginUser);

		model.addAttribute("emp", emp);
		model.addAttribute("tenure", empService.getTenure(emp));
		model.addAttribute("annualLeave", empService.getThisYearLeave(empId));
		model.addAttribute("appointments", empService.getAppointments(empId));
		if (loginUser.hasAuthority(PermissionCode.EMP_UPDATE) || loginUser.hasAuthority(PermissionCode.EMP_CREATE)) {
			model.addAttribute("account", empService.getAccount(empId));
		}
		model.addAttribute("today", LocalDate.now(clock)); 
		return "emp/detail";
	}

	@GetMapping("/new")
	public String getRegisterForm(Model model) {
		EmpFormRequestDTO form = new EmpFormRequestDTO();
		form.setHiredate(LocalDate.now(clock));
		model.addAttribute("form", form);
		addFormOptions(model);
		return "emp/form";
	}

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

	private void checkAssignmentChange(Emp emp, EmpFormRequestDTO form, BindingResult result) {
		boolean changed = (form.getDepartmentId() != null && form.getDepartmentId() != emp.getDepartment().getDepartmentId())
			|| (form.getJobId() != null && form.getJobId() != emp.getJob().getJobId());
		if (!changed) {
			return;
		}
		if (form.getChangeDate() == null) {
			result.rejectValue("changeDate", "required", "적용일을 입력하세요.");
		} else if (form.getChangeDate().isBefore(emp.getHiredate())) {
			result.rejectValue("changeDate", "invalid", "적용일은 입사일 이후여야 합니다.");
		}
		if (form.getChangeReason() == null || form.getChangeReason().trim().isEmpty()) {
			result.rejectValue("changeReason", "required", "변경 사유를 입력하세요.");
		} else if (form.getChangeReason().trim().length() > 200) {
			result.rejectValue("changeReason", "size", "변경 사유는 200자까지 입력할 수 있습니다.");
		}
	}

	private void addFormOptions(Model model) {
		model.addAttribute("departments", departmentService.getAll());
		model.addAttribute("jobs", jobService.getAll());
	}

	@GetMapping("/{empId}/edit")
	public String getEditForm(@PathVariable("empId") int empId, @AuthenticationPrincipal AccountDetails loginUser, Model model) {
		Emp emp = empService.getVisibleEmp(empId, loginUser);
		model.addAttribute("emp", emp);
		model.addAttribute("form", EmpFormRequestDTO.from(emp));
		addFormOptions(model);
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
		checkAssignmentChange(emp, form, result);
		if (!result.hasErrors()) {
			try {
				String changed = empService.update(empId, form, loginUser);
				ra.addFlashAttribute("message", "사원 정보를 수정했습니다." + (changed == null ? "" : " (" + changed + " 이력이 기록되었습니다)"));
				return "redirect:/employees/" + empId;
			} catch (BusinessException e) {
				model.addAttribute("formError", e.getMessage()); 
			}
		}
		model.addAttribute("emp", emp);
		addFormOptions(model);
		return "emp/form";
	}

	@GetMapping("/{empId}/photo")
	public ResponseEntity<Resource> getPhoto(@PathVariable("empId") int empId) {
		Emp emp = empService.getPhotoOwner(empId);
		File file = emp == null ? null : photoService.getFile(emp.getPhotoPath());
		if (file == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok()
			.contentType(MediaType.parseMediaType(PhotoService.getContentType(file)))
			.cacheControl(CacheControl.noCache())
			.body(new FileSystemResource(file));
	}
}
