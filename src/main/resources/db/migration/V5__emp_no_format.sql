update emp e
join (
	select emp_id,
	       concat(year(hiredate), lpad(row_number() over (partition by year(hiredate) order by hiredate, emp_id), 4, '0')) as new_emp_no
	from emp
) n on n.emp_id = e.emp_id
set e.emp_no = n.new_emp_no;

alter table emp modify emp_no varchar(8) not null;  -- 사번 (입사연도 + 순번 4자리)
