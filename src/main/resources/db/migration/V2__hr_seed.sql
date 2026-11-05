-- ----------------------------------------
-- 인사관리 시스템 기본 데이터 (Flyway: V2__hr_seed.sql)
-- 목데이터(사원 104명 · 계정 · 근태 · 휴가 · 인사 이력) 없이 시스템이 돌아가는 데 필요한 것만
--   권한 32개 · 권한 그룹(Role) 6개와 그룹별 권한 · 조직도(본사 > 부 > 팀) · 직급 · 휴가 유형 · 2026 공휴일
--   + 첫 로그인용 관리자 1명 (아이디 admin / 비밀번호 1234 → 로그인 후 바꿀 것)
-- 관리자로 로그인해서 사원 등록 → 부서 수정에서 조직장 지정 → 계정 권한 부여 순서로 시작
-- ----------------------------------------

-- ----------------------------------------
-- 역할(Role)
-- ----------------------------------------
insert into hr_role(hr_role_id, role_code, role_name, is_active) values(1, 'ROLE_EMPLOYEE', '일반 사원', 1);
insert into hr_role(hr_role_id, role_code, role_name, is_active) values(2, 'ROLE_TEAM_APPROVER', '팀 업무 승인자', 1);
insert into hr_role(hr_role_id, role_code, role_name, is_active) values(3, 'ROLE_DEPT_APPROVER', '부서 업무 승인자', 1);
insert into hr_role(hr_role_id, role_code, role_name, is_active) values(4, 'ROLE_HR_STAFF', '인사 담당자', 1);
insert into hr_role(hr_role_id, role_code, role_name, is_active) values(5, 'ROLE_HR_ADMIN', '인사 관리자', 1);
insert into hr_role(hr_role_id, role_code, role_name, is_active) values(6, 'ROLE_SYSTEM_ADMIN', '시스템 관리자', 1);

-- ----------------------------------------
-- 권한(Permission)
-- ----------------------------------------
insert into permission(permission_id, permission_code, permission_name, permission_group) values(1, 'PERMISSION_ACCOUNT_UNLOCK', '계정 잠금 해제', 'SECURITY');
insert into permission(permission_id, permission_code, permission_name, permission_group) values(2, 'PERMISSION_ACCOUNT_PASSWORD_RESET', '비밀번호 초기화', 'SECURITY');
insert into permission(permission_id, permission_code, permission_name, permission_group) values(3, 'PERMISSION_ACCOUNT_ENABLE', '계정 사용/중지', 'SECURITY');
insert into permission(permission_id, permission_code, permission_name, permission_group) values(4, 'PERMISSION_MY_INFO_READ', '내 정보 조회', 'HR');
insert into permission(permission_id, permission_code, permission_name, permission_group) values(5, 'PERMISSION_EMP_READ_TEAM', '팀 사원 조회', 'HR');
insert into permission(permission_id, permission_code, permission_name, permission_group) values(6, 'PERMISSION_EMP_READ_DEPT', '부서 사원 조회', 'HR');
insert into permission(permission_id, permission_code, permission_name, permission_group) values(7, 'PERMISSION_EMP_READ_ALL', '전사 사원 조회', 'HR');
insert into permission(permission_id, permission_code, permission_name, permission_group) values(8, 'PERMISSION_EMP_CREATE', '사원 등록 (계정 발급 포함)', 'HR');
insert into permission(permission_id, permission_code, permission_name, permission_group) values(9, 'PERMISSION_EMP_UPDATE', '사원 정보 수정', 'HR');
insert into permission(permission_id, permission_code, permission_name, permission_group) values(10, 'PERMISSION_EMP_RESIGN', '퇴직 처리', 'HR');
insert into permission(permission_id, permission_code, permission_name, permission_group) values(11, 'PERMISSION_DEPT_CREATE', '부서 등록', 'HR');
insert into permission(permission_id, permission_code, permission_name, permission_group) values(12, 'PERMISSION_DEPT_UPDATE', '부서 수정 (조직장 지정 포함)', 'HR');
insert into permission(permission_id, permission_code, permission_name, permission_group) values(13, 'PERMISSION_DEPT_DELETE', '부서 삭제', 'HR');
insert into permission(permission_id, permission_code, permission_name, permission_group) values(14, 'PERMISSION_ATT_CHECK', '출퇴근 체크', 'ATTENDANCE');
insert into permission(permission_id, permission_code, permission_name, permission_group) values(15, 'PERMISSION_MY_ATT_READ', '내 근태 조회', 'ATTENDANCE');
insert into permission(permission_id, permission_code, permission_name, permission_group) values(16, 'PERMISSION_ATT_READ_TEAM', '팀 근태 조회', 'ATTENDANCE');
insert into permission(permission_id, permission_code, permission_name, permission_group) values(17, 'PERMISSION_ATT_READ_DEPT', '부서 근태 조회', 'ATTENDANCE');
insert into permission(permission_id, permission_code, permission_name, permission_group) values(18, 'PERMISSION_ATT_READ_ALL', '전사 근태 조회', 'ATTENDANCE');
insert into permission(permission_id, permission_code, permission_name, permission_group) values(19, 'PERMISSION_ATT_CORRECT_REQUEST', '근태 정정 신청', 'ATTENDANCE');
insert into permission(permission_id, permission_code, permission_name, permission_group) values(20, 'PERMISSION_ATT_CORRECT_APPROVE', '근태 정정 승인/반려', 'ATTENDANCE');
insert into permission(permission_id, permission_code, permission_name, permission_group) values(21, 'PERMISSION_LEAVE_REQUEST', '휴가 신청', 'LEAVE');
insert into permission(permission_id, permission_code, permission_name, permission_group) values(22, 'PERMISSION_LEAVE_CANCEL', '내 휴가 취소', 'LEAVE');
insert into permission(permission_id, permission_code, permission_name, permission_group) values(23, 'PERMISSION_MY_LEAVE_READ', '내 휴가·잔여 연차 조회', 'LEAVE');
insert into permission(permission_id, permission_code, permission_name, permission_group) values(24, 'PERMISSION_LEAVE_READ_ALL', '전사 휴가 현황 조회', 'LEAVE');
insert into permission(permission_id, permission_code, permission_name, permission_group) values(25, 'PERMISSION_LEAVE_APPROVE', '휴가 결재 (승인/반려)', 'LEAVE');
insert into permission(permission_id, permission_code, permission_name, permission_group) values(26, 'PERMISSION_PERM_CREATE', '권한 코드 등록', 'AUTH');
insert into permission(permission_id, permission_code, permission_name, permission_group) values(27, 'PERMISSION_PERM_UPDATE', '권한 코드 수정', 'AUTH');
insert into permission(permission_id, permission_code, permission_name, permission_group) values(28, 'PERMISSION_PERM_DELETE', '권한 코드 삭제', 'AUTH');
insert into permission(permission_id, permission_code, permission_name, permission_group) values(29, 'PERMISSION_ROLE_CREATE', 'Role 등록', 'AUTH');
insert into permission(permission_id, permission_code, permission_name, permission_group) values(30, 'PERMISSION_ROLE_UPDATE', 'Role 수정 (권한 체크박스 포함)', 'AUTH');
insert into permission(permission_id, permission_code, permission_name, permission_group) values(31, 'PERMISSION_ROLE_DELETE', 'Role 삭제', 'AUTH');
insert into permission(permission_id, permission_code, permission_name, permission_group) values(32, 'PERMISSION_ROLE_ASSIGN', '계정 Role 부여/회수', 'AUTH');

-- ----------------------------------------
-- 역할별 권한 (1 일반 사원 7개 / 2 팀 승인자 4개 / 3 부서 승인자 4개 / 4 인사 담당자 6개 / 5 인사 관리자 13개 / 6 시스템 관리자 10개)
-- ----------------------------------------
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(1, 1, 4);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(2, 1, 14);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(3, 1, 15);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(4, 1, 19);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(5, 1, 21);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(6, 1, 22);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(7, 1, 23);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(8, 2, 5);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(9, 2, 16);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(10, 2, 20);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(11, 2, 25);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(12, 3, 6);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(13, 3, 17);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(14, 3, 20);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(15, 3, 25);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(16, 4, 1);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(17, 4, 7);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(18, 4, 8);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(19, 4, 9);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(20, 4, 18);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(21, 4, 24);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(22, 5, 1);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(23, 5, 2);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(24, 5, 3);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(25, 5, 7);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(26, 5, 8);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(27, 5, 9);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(28, 5, 10);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(29, 5, 11);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(30, 5, 12);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(31, 5, 13);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(32, 5, 18);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(33, 5, 24);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(34, 5, 32);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(35, 6, 1);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(36, 6, 2);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(37, 6, 3);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(38, 6, 26);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(39, 6, 27);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(40, 6, 28);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(41, 6, 29);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(42, 6, 30);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(43, 6, 31);
insert into hr_role_permission(hr_role_permission_id, hr_role_id, permission_id) values(44, 6, 32);

-- ----------------------------------------
-- 부서(조직) : 1 본사 / 2 부 / 3 팀. 조직장(manager_id)은 비워 둠 → 사원 등록 후 부서 수정에서 지정
-- ----------------------------------------
insert into department(department_id, department_code, department_name, department_level, parent_id, manager_id, is_active) values(1, 'HQ', '본사', 1, null, null, 1);
insert into department(department_id, department_code, department_name, department_level, parent_id, manager_id, is_active) values(2, 'MGMT_DIV', '경영지원부', 2, 1, null, 1);
insert into department(department_id, department_code, department_name, department_level, parent_id, manager_id, is_active) values(3, 'SALES_DIV', '영업부', 2, 1, null, 1);
insert into department(department_id, department_code, department_name, department_level, parent_id, manager_id, is_active) values(4, 'DEV_DIV', '개발부', 2, 1, null, 1);
insert into department(department_id, department_code, department_name, department_level, parent_id, manager_id, is_active) values(5, 'MKT_DIV', '마케팅부', 2, 1, null, 1);
insert into department(department_id, department_code, department_name, department_level, parent_id, manager_id, is_active) values(6, 'HR_TEAM', '인사팀', 3, 2, null, 1);
insert into department(department_id, department_code, department_name, department_level, parent_id, manager_id, is_active) values(7, 'FIN_TEAM', '재무회계팀', 3, 2, null, 1);
insert into department(department_id, department_code, department_name, department_level, parent_id, manager_id, is_active) values(8, 'GA_TEAM', '총무팀', 3, 2, null, 1);
insert into department(department_id, department_code, department_name, department_level, parent_id, manager_id, is_active) values(9, 'SALES1_TEAM', '영업1팀', 3, 3, null, 1);
insert into department(department_id, department_code, department_name, department_level, parent_id, manager_id, is_active) values(10, 'SALES2_TEAM', '영업2팀', 3, 3, null, 1);
insert into department(department_id, department_code, department_name, department_level, parent_id, manager_id, is_active) values(11, 'GLOBAL_TEAM', '해외영업팀', 3, 3, null, 1);
insert into department(department_id, department_code, department_name, department_level, parent_id, manager_id, is_active) values(12, 'DEV1_TEAM', '개발1팀', 3, 4, null, 1);
insert into department(department_id, department_code, department_name, department_level, parent_id, manager_id, is_active) values(13, 'DEV2_TEAM', '개발2팀', 3, 4, null, 1);
insert into department(department_id, department_code, department_name, department_level, parent_id, manager_id, is_active) values(14, 'QA_TEAM', 'QA팀', 3, 4, null, 1);
insert into department(department_id, department_code, department_name, department_level, parent_id, manager_id, is_active) values(15, 'DESIGN_TEAM', '디자인팀', 3, 5, null, 1);
insert into department(department_id, department_code, department_name, department_level, parent_id, manager_id, is_active) values(16, 'MKT_TEAM', '마케팅팀', 3, 5, null, 1);

-- ----------------------------------------
-- 직급 (job_level 클수록 높음)
-- ----------------------------------------
insert into job(job_id, job_code, job_name, job_level, is_active) values(1, 'STAFF', '사원', 1, 1);
insert into job(job_id, job_code, job_name, job_level, is_active) values(2, 'ASSOCIATE', '대리', 2, 1);
insert into job(job_id, job_code, job_name, job_level, is_active) values(3, 'MANAGER', '과장', 3, 1);
insert into job(job_id, job_code, job_name, job_level, is_active) values(4, 'SENIOR_MANAGER', '차장', 4, 1);
insert into job(job_id, job_code, job_name, job_level, is_active) values(5, 'DEPARTMENT_MANAGER', '부장', 5, 1);
insert into job(job_id, job_code, job_name, job_level, is_active) values(6, 'DIRECTOR', '이사', 6, 1);
insert into job(job_id, job_code, job_name, job_level, is_active) values(7, 'PRESIDENT', '사장', 7, 1);
insert into job(job_id, job_code, job_name, job_level, is_active) values(8, 'CHAIRMAN', '회장', 8, 1);

-- ----------------------------------------
-- 휴가 유형
-- ----------------------------------------
insert into leave_type(leave_type_id, leave_type_code, leave_type_name, is_deduct, is_active) values(1, 'ANNUAL', '연차', 1, 1);
insert into leave_type(leave_type_id, leave_type_code, leave_type_name, is_deduct, is_active) values(2, 'HALF_AM', '오전 반차', 1, 1);
insert into leave_type(leave_type_id, leave_type_code, leave_type_name, is_deduct, is_active) values(3, 'HALF_PM', '오후 반차', 1, 1);
insert into leave_type(leave_type_id, leave_type_code, leave_type_name, is_deduct, is_active) values(4, 'SICK', '병가', 0, 1);
insert into leave_type(leave_type_id, leave_type_code, leave_type_name, is_deduct, is_active) values(5, 'FAMILY_EVENT', '경조사', 0, 1);
insert into leave_type(leave_type_id, leave_type_code, leave_type_name, is_deduct, is_active) values(6, 'OFFICIAL', '공가', 0, 1);

-- ----------------------------------------
-- 공휴일 (2026, 대체공휴일 포함)
-- ----------------------------------------
insert into holiday(holiday_date, holiday_name) values
('2026-01-01', '신정'),
('2026-02-16', '설날 연휴'),
('2026-02-17', '설날'),
('2026-02-18', '설날 연휴'),
('2026-03-01', '삼일절'),
('2026-03-02', '삼일절 대체공휴일'),
('2026-05-05', '어린이날'),
('2026-05-24', '부처님오신날'),
('2026-05-25', '부처님오신날 대체공휴일'),
('2026-06-03', '전국동시지방선거'),
('2026-06-06', '현충일'),
('2026-08-15', '광복절'),
('2026-08-17', '광복절 대체공휴일'),
('2026-09-24', '추석 연휴'),
('2026-09-25', '추석'),
('2026-09-26', '추석 연휴'),
('2026-10-03', '개천절'),
('2026-10-05', '개천절 대체공휴일'),
('2026-10-09', '한글날'),
('2026-12-25', '성탄절');

-- ----------------------------------------
-- 첫 관리자 (본사 소속 · 사원 직급, 권한 그룹 6개 모두 = 권한 32개 전부)
-- 사번은 입사연도 + 순번 4자리 (EmpService.register 와 같은 규칙)
-- ----------------------------------------
insert into emp(emp_no, ename, gender, email, phone, hiredate, department_id, job_id, status)
values('20260001', '시스템관리자', 'M', 'admin@company.com', '010-0000-0000', '2026-01-02', 1, 1, 'ACTIVE');
set @admin_emp_id = last_insert_id();

insert into account(emp_id, login_id, password, enabled, fail_count, is_locked, created_at)
values(@admin_emp_id, 'admin', '$2a$10$nZkSAw4h6oBkRUQBPof1Fed6nUe4pd9qzIX3adMLP1Ivy/Xo.bLje', 1, 0, 0, '2026-01-02 09:00:00');
set @admin_account_id = last_insert_id();

insert into account_role(account_id, hr_role_id)
select @admin_account_id, hr_role_id from hr_role;

insert into account_role_history(account_id, hr_role_id, action_type, reason, register_id, created_at)
select @admin_account_id, hr_role_id, 'GRANT', '시스템 초기 관리자', @admin_emp_id, '2026-01-02 09:00:00' from hr_role;

insert into annual_leave(emp_id, leave_year, granted_days, used_days, created_at)
values(@admin_emp_id, 2026, 11, 0, '2026-01-02 09:00:00');

insert into hr_appointment(emp_id, appointment_type, after_department_id, after_job_id, effective_date, reason, register_id, created_at)
values(@admin_emp_id, 'HIRE', 1, 1, '2026-01-02', '시스템 초기 관리자 등록', @admin_emp_id, '2026-01-02 09:00:00');

commit;
