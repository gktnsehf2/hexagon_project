package com.hexagon.hrms.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexagon.hrms.common.web.Pager;
import com.hexagon.hrms.domain.Account;
import com.hexagon.hrms.domain.AccountRoleHistory;
import com.hexagon.hrms.domain.AnnualLeave;
import com.hexagon.hrms.domain.Department;
import com.hexagon.hrms.domain.Emp;
import com.hexagon.hrms.domain.HrAppointment;
import com.hexagon.hrms.domain.HrRole;
import com.hexagon.hrms.domain.Job;
import com.hexagon.hrms.domain.Permission;
import com.hexagon.hrms.dto.request.EmpFormRequestDTO;
import com.hexagon.hrms.dto.request.EmpSearchRequestDTO;
import com.hexagon.hrms.exception.BusinessException;
import com.hexagon.hrms.exception.ErrorCode;
import com.hexagon.hrms.mapper.AccountMapper;
import com.hexagon.hrms.mapper.AccountRoleMapper;
import com.hexagon.hrms.mapper.AnnualLeaveMapper;
import com.hexagon.hrms.mapper.DepartmentMapper;
import com.hexagon.hrms.mapper.EmpMapper;
import com.hexagon.hrms.mapper.HrAppointmentMapper;
import com.hexagon.hrms.mapper.HrRoleMapper;
import com.hexagon.hrms.mapper.JobMapper;
import com.hexagon.hrms.security.AccountDetails;
import com.hexagon.hrms.security.PermissionCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EmpService {

		public static final String DEFAULT_ROLE = "ROLE_EMPLOYEE";

	private final EmpMapper empMapper;
	private final DepartmentService departmentService;
	private final DepartmentMapper departmentMapper;
	private final JobMapper jobMapper;
	private final AccountMapper accountMapper;
	private final AccountRoleMapper accountRoleMapper;
	private final HrRoleMapper hrRoleMapper;
	private final AnnualLeaveMapper annualLeaveMapper;
	private final HrAppointmentMapper hrAppointmentMapper;
	private final PhotoService photoService;
	private final PasswordEncoder passwordEncoder;
	private final Clock clock;

		public EmpScope getScope(AccountDetails loginUser) {
		return departmentService.getScope(loginUser,
			PermissionCode.EMP_READ_ALL, PermissionCode.EMP_READ_DEPT, PermissionCode.EMP_READ_TEAM);
	}

		public Pager getEmpList(EmpSearchRequestDTO search, EmpScope scope) {
		List<Integer> deptIds = scope.getDeptIds();

		if (search.getDepartmentId() != null && search.getDepartmentId() > 0) {
			List<Integer> chosen = departmentService.getSubtreeIds(Collections.singleton(search.getDepartmentId()));
			if (deptIds != null) {
				chosen.retainAll(deptIds);
			}
			deptIds = chosen;
		}

		if (deptIds != null && deptIds.isEmpty()) {
			Pager empty = new Pager(1, 0);
			empty.setList(new ArrayList<Emp>());
			return empty;
		}

		int totalCount = empMapper.selectCount(search, deptIds);
		Pager pager = new Pager(search.getCurrentPage(), totalCount);
		pager.setList(empMapper.selectList(search, deptIds, pager));
		log.debug("사원 목록 범위={} 전체={}건 페이지={}", scope.getLabel(), totalCount, pager.getCurrentPage());
		return pager;
	}

	public List<Department> getSelectableDepartments(EmpScope scope) {
		return scope.isAll() ? departmentService.getAll() : departmentService.getByIds(scope.getDeptIds());
	}

	public Emp getVisibleEmp(int empId, AccountDetails loginUser) {
		Emp emp = empMapper.select(empId);
		if (emp == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 사원입니다.");
		}
		if (!getScope(loginUser).contains(emp.getDepartment().getDepartmentId())) {
			throw new AccessDeniedException("조회 범위 밖의 사원입니다.");
		}
		return emp;
	}

	public Emp getMe(AccountDetails loginUser) {
		Emp emp = empMapper.select(loginUser.getEmpId());
		if (emp == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND, "사원 정보를 찾을 수 없습니다.");
		}
		return emp;
	}

	public List<Permission> getPermissions(Account account) {
		Map<Integer, Permission> map = new TreeMap<>();
		if (account != null && account.getRoles() != null) {
			for (HrRole role : account.getRoles()) {
				if (role.getPermissions() != null) {
					for (Permission p : role.getPermissions()) {
						map.putIfAbsent(p.getPermissionId(), p);
					}
				}
			}
		}
		return new ArrayList<>(map.values());
	}

	public Emp getPhotoOwner(int empId) {
		return empMapper.select(empId);
	}

	public Account getAccount(int empId) {
		return accountMapper.selectByEmpId(empId);
	}

	public AnnualLeave getThisYearLeave(int empId) {
		return annualLeaveMapper.select(empId, LocalDate.now(clock).getYear());
	}

	public List<HrAppointment> getAppointments(int empId) {
		return hrAppointmentMapper.selectByEmpId(empId);
	}

	public String getTenure(Emp emp) {
		LocalDate end = emp.getResignDate() != null ? emp.getResignDate() : LocalDate.now(clock);
		if (end.isBefore(emp.getHiredate())) {
			return "입사 전";
		}
		Period p = Period.between(emp.getHiredate(), end);
		if (p.getYears() == 0 && p.getMonths() == 0) {
			return (p.getDays() + 1) + "일";
		}
		return (p.getYears() > 0 ? p.getYears() + "년 " : "") + p.getMonths() + "개월";
	}

	public boolean isLoginIdTaken(String loginId) {
		return accountMapper.countByLoginId(loginId) > 0;
	}

	public boolean isEmailTaken(String email, int exceptEmpId) {
		return email != null && !email.trim().isEmpty() && empMapper.countByEmail(email.trim(), exceptEmpId) > 0;
	}

	@Transactional
	public int register(EmpFormRequestDTO form, AccountDetails loginUser) {
		Department department = departmentService.get(form.getDepartmentId());
		if (department == null || department.getIsActive() != 1) {
			throw new BusinessException(ErrorCode.NOT_FOUND, "선택한 부서를 찾을 수 없습니다.");
		}
		Job job = jobMapper.select(form.getJobId());
		if (job == null || job.getIsActive() != 1) {
			throw new BusinessException(ErrorCode.NOT_FOUND, "선택한 직급을 찾을 수 없습니다.");
		}
		Emp register = loginUser.getAccount().getEmp();

		Emp emp = new Emp();
		int year = form.getHiredate().getYear();
		int seq = empMapper.selectNextSeq(year);
		if (seq > 9999) {
			throw new BusinessException(year + "년 입사자 사번(9999번)을 모두 사용했습니다.");
		}
		emp.setEmpNo(String.format("%d%04d", year, seq));
		emp.setEname(form.getEname().trim());
		emp.setGender(form.getGender());
		emp.setEmail(blankToNull(form.getEmail()));
		emp.setPhone(blankToNull(form.getPhone()));
		emp.setHiredate(form.getHiredate());
		emp.setDepartment(department);
		emp.setJob(job);
		emp.setStatus("ACTIVE");
		empMapper.insert(emp);

		Account account = new Account();
		account.setEmp(emp);
		account.setLoginId(form.getLoginId().trim());
		account.setPassword(passwordEncoder.encode(form.getPassword()));
		accountMapper.insert(account);

		HrRole role = hrRoleMapper.selectByCode(DEFAULT_ROLE);
		if (role == null) {
			throw new BusinessException("기본 Role(" + DEFAULT_ROLE + ")이 없습니다. 관리자에게 문의하세요.");
		}
		accountRoleMapper.insert(account.getAccountId(), role.getHrRoleId());
		AccountRoleHistory history = new AccountRoleHistory();
		history.setAccount(account);
		history.setHrRole(role);
		history.setActionType("GRANT");
		history.setReason("신규 입사 기본 Role");
		history.setRegister(register);
		accountRoleMapper.insertHistory(history);

		int leaveYear = Math.max(LocalDate.now(clock).getYear(), form.getHiredate().getYear());
		AnnualLeave leave = new AnnualLeave();
		leave.setEmp(emp);
		leave.setLeaveYear(leaveYear);
		leave.setGrantedDays(BigDecimal.valueOf(getAnnualLeaveDays(form.getHiredate(), leaveYear)));
		annualLeaveMapper.insert(leave);

		HrAppointment appointment = new HrAppointment();
		appointment.setEmp(emp);
		appointment.setAppointmentType("HIRE");
		appointment.setAfterDepartment(department);
		appointment.setAfterJob(job);
		appointment.setEffectiveDate(form.getHiredate());
		appointment.setReason("신규 입사");
		appointment.setRegister(register);
		hrAppointmentMapper.insert(appointment);

		if (form.hasPhoto()) {
			empMapper.updatePhoto(emp.getEmpId(), photoService.save(form.getPhoto(), emp.getEmpNo()));
		}

		log.info("사원 등록 {} {} (계정 {}) by {}", emp.getEmpNo(), emp.getEname(), account.getLoginId(), register.getEname());
		return emp.getEmpId();
	}

	public static int getAnnualLeaveDays(LocalDate hiredate, int year) {
		int years = year - hiredate.getYear();
		if (years <= 0) {
			return Math.min(11, 12 - hiredate.getMonthValue());
		}
		return Math.min(25, 15 + (years - 1) / 2);
	}

	@Transactional
	public String update(int empId, EmpFormRequestDTO form, AccountDetails loginUser) {
		Emp emp = getVisibleEmp(empId, loginUser);
		String changed = changeAssignment(emp, form, loginUser);
		emp.setEname(form.getEname().trim());
		emp.setGender(form.getGender());
		emp.setEmail(blankToNull(form.getEmail()));
		emp.setPhone(blankToNull(form.getPhone()));
		empMapper.update(emp);

		if (form.hasPhoto()) {
			String oldPath = emp.getPhotoPath();
			empMapper.updatePhoto(empId, photoService.save(form.getPhoto(), emp.getEmpNo()));
			photoService.delete(oldPath);
		}
		log.info("사원 수정 {} {} by {}", emp.getEmpNo(), emp.getEname(), loginUser.getUsername());
		return changed;
	}

	private String changeAssignment(Emp emp, EmpFormRequestDTO form, AccountDetails loginUser) {
		Department beforeDept = emp.getDepartment();
		Job beforeJob = emp.getJob();
		int deptId = form.getDepartmentId() == null ? beforeDept.getDepartmentId() : form.getDepartmentId();
		int jobId = form.getJobId() == null ? beforeJob.getJobId() : form.getJobId();
		boolean deptChanged = deptId != beforeDept.getDepartmentId();
		boolean jobChanged = jobId != beforeJob.getJobId();
		if (!deptChanged && !jobChanged) {
			return null;
		}

		if (!emp.isActive()) {
			throw new BusinessException("퇴직한 사원은 부서·직급을 바꿀 수 없습니다.");
		}
		Department afterDept = departmentService.get(deptId);
		if (afterDept == null || afterDept.getIsActive() != 1) {
			throw new BusinessException(ErrorCode.NOT_FOUND, "선택한 부서를 찾을 수 없습니다.");
		}
		Job afterJob = jobMapper.select(jobId);
		if (afterJob == null || afterJob.getIsActive() != 1) {
			throw new BusinessException(ErrorCode.NOT_FOUND, "선택한 직급을 찾을 수 없습니다.");
		}
		String reason = form.getChangeReason() == null ? "" : form.getChangeReason().trim();
		if (reason.isEmpty()) {
			throw new BusinessException("부서·직급을 바꾸려면 변경 사유를 입력하세요.");
		}
		if (reason.length() > 200) {
			throw new BusinessException("변경 사유는 200자까지 입력할 수 있습니다.");
		}
		LocalDate changeDate = form.getChangeDate() == null ? LocalDate.now(clock) : form.getChangeDate();
		if (changeDate.isBefore(emp.getHiredate())) {
			throw new BusinessException("적용일은 입사일(" + emp.getHiredate() + ") 이후여야 합니다.");
		}

		empMapper.updateAssignment(emp.getEmpId(), deptId, jobId);

		String type = jobChanged && afterJob.getJobLevel() > beforeJob.getJobLevel() ? "PROMOTION" : "TRANSFER";
		HrAppointment appointment = new HrAppointment();
		appointment.setEmp(emp);
		appointment.setAppointmentType(type);
		appointment.setBeforeDepartment(beforeDept);
		appointment.setAfterDepartment(afterDept);
		appointment.setBeforeJob(beforeJob);
		appointment.setAfterJob(afterJob);
		appointment.setEffectiveDate(changeDate);
		appointment.setReason(reason);
		appointment.setRegister(loginUser.getAccount().getEmp());
		hrAppointmentMapper.insert(appointment);

		if (deptChanged && departmentMapper.clearManager(beforeDept.getDepartmentId(), emp.getEmpId()) > 0) {
			log.info("조직장 해제 {} 부서={}", emp.getEmpNo(), beforeDept.getDepartmentName());
		}
		log.info("인사 변경 {} {} {}→{} / {}→{} by {}", appointment.getTypeName(), emp.getEmpNo(),
			beforeDept.getDepartmentName(), afterDept.getDepartmentName(), beforeJob.getJobName(), afterJob.getJobName(),
			loginUser.getUsername());
		return appointment.getTypeName();
	}

	@Transactional
	public Emp resign(int empId, LocalDate resignDate, AccountDetails loginUser) {
		Emp emp = getVisibleEmp(empId, loginUser);
		if (!emp.isActive()) {
			throw new BusinessException("이미 퇴직 처리된 사원입니다.");
		}
		if (empId == loginUser.getEmpId()) {
			throw new BusinessException("본인은 퇴직 처리할 수 없습니다.");
		}
		if (resignDate == null) {
			throw new BusinessException("퇴직일을 입력하세요.");
		}
		if (resignDate.isBefore(emp.getHiredate())) {
			throw new BusinessException("퇴직일은 입사일(" + emp.getHiredate() + ") 이후여야 합니다.");
		}

		List<Integer> managed = departmentService.getManagedIds(empId);
		if (!managed.isEmpty()) {
			List<String> names = new ArrayList<>();
			for (Department d : departmentService.getByIds(managed)) {
				names.add(d.getDepartmentName());
			}
			throw new BusinessException(String.join(", ", names) + "의 조직장입니다. 부서 관리에서 조직장을 먼저 바꾼 뒤 퇴직 처리하세요.");
		}

		emp.setStatus("RESIGNED");
		emp.setResignDate(resignDate);
		empMapper.updateResign(emp);
		accountMapper.updateEnabledByEmpId(empId, 0);

		HrAppointment appointment = new HrAppointment();
		appointment.setEmp(emp);
		appointment.setAppointmentType("RESIGN");
		appointment.setBeforeDepartment(emp.getDepartment());
		appointment.setBeforeJob(emp.getJob());
		appointment.setEffectiveDate(resignDate);
		appointment.setReason("퇴직");
		appointment.setRegister(loginUser.getAccount().getEmp());
		hrAppointmentMapper.insert(appointment);

		log.info("퇴직 처리 {} {} 퇴직일 {} by {}", emp.getEmpNo(), emp.getEname(), resignDate, loginUser.getUsername());
		return emp;
	}

	private String blankToNull(String value) {
		return (value == null || value.trim().isEmpty()) ? null : value.trim();
	}
}
