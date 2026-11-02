package com.hexagon.hrms.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import com.hexagon.hrms.dto.request.EmpFormRequestDTO;
import com.hexagon.hrms.dto.request.EmpSearchRequestDTO;
import com.hexagon.hrms.exception.BusinessException;
import com.hexagon.hrms.mapper.AccountMapper;
import com.hexagon.hrms.mapper.AccountRoleMapper;
import com.hexagon.hrms.mapper.AnnualLeaveMapper;
import com.hexagon.hrms.mapper.EmpMapper;
import com.hexagon.hrms.mapper.HrAppointmentMapper;
import com.hexagon.hrms.mapper.HrRoleMapper;
import com.hexagon.hrms.mapper.JobMapper;
import com.hexagon.hrms.security.AccountDetails;
import com.hexagon.hrms.security.PermissionCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 F-03 사원 목록 조회·검색
 권한에 따라 볼 수 있는 범위가 다르다 (계산 규칙은 DepartmentService.getScope 참고)
   PERMISSION_EMP_READ_ALL  : 전사
   PERMISSION_EMP_READ_DEPT : 본인이 조직장인 조직과 그 하위, 조직장이 아니면 소속 부 전체
   PERMISSION_EMP_READ_TEAM : 소속 팀 + 본인이 조직장인 팀
 F-04 사원 등록 / 수정, F-05 사원 상세, F-06 퇴직 처리
   상세·수정·퇴직도 목록과 같은 조회 범위 안의 사원만 가능
*/
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EmpService {

	// 신규 계정에 주는 기본 Role (직급과 Role 은 별개라 직급과 상관없이 같음, 추가 Role 은 계정 Role 부여 화면에서)
	public static final String DEFAULT_ROLE = "ROLE_EMPLOYEE";

	private final EmpMapper empMapper;
	private final DepartmentService departmentService;
	private final JobMapper jobMapper;
	private final AccountMapper accountMapper;
	private final AccountRoleMapper accountRoleMapper;
	private final HrRoleMapper hrRoleMapper;
	private final AnnualLeaveMapper annualLeaveMapper;
	private final HrAppointmentMapper hrAppointmentMapper;
	private final PhotoService photoService;
	private final PasswordEncoder passwordEncoder;
	private final Clock clock;

	// 로그인한 사람의 사원 조회 범위
	public EmpScope getScope(AccountDetails loginUser) {
		return departmentService.getScope(loginUser,
			PermissionCode.EMP_READ_ALL, PermissionCode.EMP_READ_DEPT, PermissionCode.EMP_READ_TEAM);
	}

	// 사원 목록 (Pager 에 목록까지 담아서 반환)
	public Pager getEmpList(EmpSearchRequestDTO search, EmpScope scope) {
		List<Integer> deptIds = scope.getDeptIds();

		// 조직을 골랐으면 "고른 조직과 하위 조직" ∩ "내 조회 범위"
		if (search.getDepartmentId() != null && search.getDepartmentId() > 0) {
			List<Integer> chosen = departmentService.getSubtreeIds(Collections.singleton(search.getDepartmentId()));
			if (deptIds != null) {
				chosen.retainAll(deptIds);
			}
			deptIds = chosen;
		}

		// 볼 수 있는 조직이 하나도 없으면 쿼리 없이 빈 목록 (in () 는 SQL 오류)
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

	// 검색 조건의 조직 셀렉트 박스에 보여줄 조직 (내 범위 안만)
	public List<Department> getSelectableDepartments(EmpScope scope) {
		return scope.isAll() ? departmentService.getAll() : departmentService.getByIds(scope.getDeptIds());
	}

	/*--------------------------------------------
	 F-05 사원 상세
	--------------------------------------------*/

	// 내 조회 범위 안의 사원 (없으면 BusinessException, 범위 밖이면 403 화면)
	public Emp getVisibleEmp(int empId, AccountDetails loginUser) {
		Emp emp = empMapper.select(empId);
		if (emp == null) {
			throw new BusinessException("존재하지 않는 사원입니다.");
		}
		if (!getScope(loginUser).contains(emp.getDepartment().getDepartmentId())) {
			throw new AccessDeniedException("조회 범위 밖의 사원입니다.");
		}
		return emp;
	}

	// 증명사진 주인 (사진은 로그인한 사람이면 누구나 볼 수 있음 → 범위 검사 없음)
	public Emp getPhotoOwner(int empId) {
		return empMapper.select(empId);
	}

	// 사원의 계정 + Role (계정이 없으면 null)
	public Account getAccount(int empId) {
		return accountMapper.selectByEmpId(empId);
	}

	// 올해 연차 (부여 전이면 null)
	public AnnualLeave getThisYearLeave(int empId) {
		return annualLeaveMapper.select(empId, LocalDate.now(clock).getYear());
	}

	public List<HrAppointment> getAppointments(int empId) {
		return hrAppointmentMapper.selectByEmpId(empId);
	}

	// 근속 기간 (예: "4년 8개월"). 퇴직자는 퇴직일까지
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

	/*--------------------------------------------
	 F-04 사원 등록 / 수정
	--------------------------------------------*/

	public boolean isLoginIdTaken(String loginId) {
		return accountMapper.countByLoginId(loginId) > 0;
	}

	public boolean isEmailTaken(String email, int exceptEmpId) {
		return email != null && !email.trim().isEmpty() && empMapper.countByEmail(email.trim(), exceptEmpId) > 0;
	}

	/*
	 사원 등록: 하나라도 실패하면 전부 rollback
	   1. 사원 (사번 = EMP + 입사연도 + 순번 3자리)
	   2. 계정 발급 (비밀번호는 BCrypt)
	   3. 기본 Role 부여 + 부여 이력
	   4. 그해 연차 부여
	   5. 입사 발령 이력
	   6. 증명사진 저장 (파일은 rollback 이 안 되므로 DB 작업을 모두 끝낸 뒤)
	 반환: 새 사원 ID
	*/
	@Transactional
	public int register(EmpFormRequestDTO form, AccountDetails loginUser) {
		Department department = departmentService.get(form.getDepartmentId());
		if (department == null || department.getIsActive() != 1) {
			throw new BusinessException("선택한 부서를 찾을 수 없습니다.");
		}
		Job job = jobMapper.select(form.getJobId());
		if (job == null || job.getIsActive() != 1) {
			throw new BusinessException("선택한 직급을 찾을 수 없습니다.");
		}
		Emp register = loginUser.getAccount().getEmp();

		// 1. 사원
		Emp emp = new Emp();
		emp.setEmpNo(String.format("EMP%d%03d", form.getHiredate().getYear(), empMapper.selectNextSeq()));
		emp.setEname(form.getEname().trim());
		emp.setGender(form.getGender());
		emp.setEmail(blankToNull(form.getEmail()));
		emp.setPhone(blankToNull(form.getPhone()));
		emp.setHiredate(form.getHiredate());
		emp.setDepartment(department);
		emp.setJob(job);
		emp.setStatus("ACTIVE");
		empMapper.insert(emp);

		// 2. 계정
		Account account = new Account();
		account.setEmp(emp);
		account.setLoginId(form.getLoginId().trim());
		account.setPassword(passwordEncoder.encode(form.getPassword()));
		accountMapper.insert(account);

		// 3. 기본 Role
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

		// 4. 연차 (입사일이 내년이면 입사 연도 기준)
		int leaveYear = Math.max(LocalDate.now(clock).getYear(), form.getHiredate().getYear());
		AnnualLeave leave = new AnnualLeave();
		leave.setEmp(emp);
		leave.setLeaveYear(leaveYear);
		leave.setGrantedDays(BigDecimal.valueOf(getAnnualLeaveDays(form.getHiredate(), leaveYear)));
		annualLeaveMapper.insert(leave);

		// 5. 입사 발령
		HrAppointment appointment = new HrAppointment();
		appointment.setEmp(emp);
		appointment.setAppointmentType("HIRE");
		appointment.setAfterDepartment(department);
		appointment.setAfterJob(job);
		appointment.setEffectiveDate(form.getHiredate());
		appointment.setReason("신규 입사");
		appointment.setRegister(register);
		hrAppointmentMapper.insert(appointment);

		// 6. 사진
		if (form.hasPhoto()) {
			empMapper.updatePhoto(emp.getEmpId(), photoService.save(form.getPhoto(), emp.getEmpNo()));
		}

		log.info("사원 등록 {} {} (계정 {}) by {}", emp.getEmpNo(), emp.getEname(), account.getLoginId(), register.getEname());
		return emp.getEmpId();
	}

	/*
	 연차 일수 (V2 시드 데이터와 같은 규칙, 매년 1월 1일 일괄 부여로 단순화)
	   입사 첫해 : 입사 다음 달부터 12월까지 매월 1일 (최대 11일)
	   그 뒤     : 15일 + 2년마다 1일 (최대 25일)
	*/
	public static int getAnnualLeaveDays(LocalDate hiredate, int year) {
		int years = year - hiredate.getYear();
		if (years <= 0) {
			return Math.min(11, 12 - hiredate.getMonthValue());
		}
		return Math.min(25, 15 + (years - 1) / 2);
	}

	// 기본 정보 수정 (이름, 성별, 이메일, 연락처, 사진)
	@Transactional
	public void update(int empId, EmpFormRequestDTO form, AccountDetails loginUser) {
		Emp emp = getVisibleEmp(empId, loginUser);
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
	}

	/*--------------------------------------------
	 F-06 퇴직 처리 (삭제하지 않고 상태만 바꿈)
	   1. 사원 status = RESIGNED, 퇴직일
	   2. 계정 사용 중지 (enabled = 0) → 로그인 불가
	   3. 퇴직 발령 이력 (발령 전 = 퇴직 당시 조직·직급·직책)
	--------------------------------------------*/
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
		// 조직장이 빈 조직이 생기지 않도록 조직장은 먼저 교체하게 한다
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
		appointment.setBeforeHrPosition(emp.getHrPosition());
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
