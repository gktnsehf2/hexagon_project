-- ----------------------------------------
-- 사번 형식 변경 (Flyway: V5__emp_no_format.sql)
-- EMP + 입사연도 + 순번 3자리(EMP2026105) → 입사연도 + 그해 입사 순번 4자리, 숫자 8자리(20260001)
-- 같은 해 입사자는 입사일 → 사원 ID 순으로 0001 부터 다시 매김
-- 새 사원의 사번은 EmpService.register 가 같은 방식으로 만든다 (그해 마지막 순번 + 1)
-- ----------------------------------------
update emp e
join (
	select emp_id,
	       concat(year(hiredate), lpad(row_number() over (partition by year(hiredate) order by hiredate, emp_id), 4, '0')) as new_emp_no
	from emp
) n on n.emp_id = e.emp_id
set e.emp_no = n.new_emp_no;

alter table emp modify emp_no varchar(8) not null;  -- 사번 (입사연도 + 순번 4자리)
