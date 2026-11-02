-- ----------------------------------------
-- V3: 테스트용 전체 권한 계정 (아이디 admin / 비밀번호 1234)
-- Role 6개(일반 사원 ~ 시스템 관리자)를 모두 부여 → 권한 32개 전부
-- 소속은 본사, 직급은 사원. 조직장이 아니므로 결재자로 지정되지는 않는다
-- 제출·시연 전 실제 운영처럼 보여야 하면 이 파일을 지우고 DB를 다시 만들 것
-- ----------------------------------------

insert into emp(emp_no, ename, gender, email, phone, hiredate, department_id, job_id, status)
values('EMP2026105', '테스트관리자', 'M', 'admin@company.com', '010-0000-0000', '2026-01-02', 1, 1, 'ACTIVE');
set @admin_emp_id = last_insert_id();

insert into account(emp_id, login_id, password, enabled, fail_count, is_locked, created_at)
values(@admin_emp_id, 'admin', '$2a$10$nZkSAw4h6oBkRUQBPof1Fed6nUe4pd9qzIX3adMLP1Ivy/Xo.bLje', 1, 0, 0, '2026-01-02 09:00:00');
set @admin_account_id = last_insert_id();

insert into account_role(account_id, hr_role_id)
select @admin_account_id, hr_role_id from hr_role;

insert into account_role_history(account_id, hr_role_id, action_type, reason, register_id, created_at)
select @admin_account_id, hr_role_id, 'GRANT', '테스트용 전체 권한 계정', 1, '2026-01-02 10:00:00' from hr_role;

insert into annual_leave(emp_id, leave_year, granted_days, used_days, created_at)
values(@admin_emp_id, 2026, 11, 0, '2026-01-02 09:00:00');

insert into hr_appointment(emp_id, appointment_type, after_department_id, after_job_id, effective_date, reason, register_id, created_at)
values(@admin_emp_id, 'HIRE', 1, 1, '2026-01-02', '테스트 계정 등록', 1, '2026-01-02 09:00:00');

commit;
