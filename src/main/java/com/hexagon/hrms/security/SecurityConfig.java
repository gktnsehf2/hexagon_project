package com.hexagon.hrms.security;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;

/*
 URL 별 접근 권한과 로그인/로그아웃 설정
 - 권한 검사는 Role 이 아니라 권한(Permission) 코드로 한다 → Role 구성을 바꿔도 코드 수정이 없음
 - antMatcher(...) 를 쓰는 이유: 시큐리티는 Root, 컨트롤러는 Servlet 컨텍스트에 있어서
   requestMatchers("문자열") 이 쓰는 MvcRequestMatcher 를 쓸 수 없음
*/
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final LoginSuccessHandler loginSuccessHandler;
	private final LoginFailureHandler loginFailureHandler;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> csrf.disable()) // 수업 코드와 같이 공부 목적상 생략

			.authorizeHttpRequests(auth -> auth
				// 로그인 화면, css/js 는 누구나
				.requestMatchers(antMatcher("/loginform"), antMatcher("/resources/**")).permitAll()
				// 내 정보
				.requestMatchers(antMatcher("/me")).hasAuthority(PermissionCode.MY_INFO_READ)
				// 사원 등록·수정·퇴직 (위에서부터 먼저 맞는 규칙이 적용되므로 /employees/** 보다 위에)
				.requestMatchers(antMatcher("/employees/*/photo")).authenticated()
				.requestMatchers(antMatcher("/employees/new")).hasAuthority(PermissionCode.EMP_CREATE)
				.requestMatchers(antMatcher("/employees/*/edit")).hasAuthority(PermissionCode.EMP_UPDATE)
				.requestMatchers(antMatcher("/api/employees/*/resign")).hasAuthority(PermissionCode.EMP_RESIGN)
				// 사원 목록·상세: 팀 / 부서 / 전사 중 하나라도 있으면 입장, 범위는 서비스에서 나눔
				.requestMatchers(antMatcher("/employees/**")).hasAnyAuthority(
					PermissionCode.EMP_READ_TEAM, PermissionCode.EMP_READ_DEPT, PermissionCode.EMP_READ_ALL)
				// 내 근태 · 내 휴가 (/** 는 /attendance/my 자체도 포함)
				.requestMatchers(antMatcher("/attendance/my/**")).hasAuthority(PermissionCode.MY_ATT_READ)
				.requestMatchers(antMatcher("/leaves/my/**")).hasAuthority(PermissionCode.MY_LEAVE_READ)
				.requestMatchers(antMatcher("/api/leaves/*/cancel")).hasAuthority(PermissionCode.LEAVE_CANCEL)
				// 휴가 신청 (POST /api/leaves 와 미리보기)
				.requestMatchers(antMatcher("/leaves/new"), antMatcher("/api/leaves"), antMatcher("/api/leaves/plan"))
					.hasAuthority(PermissionCode.LEAVE_REQUEST)
				// 전체 휴가 현황
				.requestMatchers(antMatcher("/leaves/status/**")).hasAuthority(PermissionCode.LEAVE_READ_ALL)
				// 휴가 결재
				.requestMatchers(antMatcher("/leaves/approvals/**"), antMatcher("/api/leaves/*/approve"), antMatcher("/api/leaves/*/reject"))
					.hasAuthority(PermissionCode.LEAVE_APPROVE)
				// 휴가 캘린더 (전 직원 휴가·공휴일): 휴가 조회·결재 권한 중 하나라도 있으면
				.requestMatchers(antMatcher("/calendar"), antMatcher("/api/calendar/**")).hasAnyAuthority(
					PermissionCode.MY_LEAVE_READ, PermissionCode.LEAVE_APPROVE, PermissionCode.LEAVE_READ_ALL)
				// 부서 관리: 등록·수정·삭제는 각각의 권한, 조직도는 사원 조회 권한이나 부서 관리 권한
				.requestMatchers(antMatcher("/departments/new")).hasAuthority(PermissionCode.DEPT_CREATE)
				.requestMatchers(antMatcher("/departments/*/edit")).hasAuthority(PermissionCode.DEPT_UPDATE)
				.requestMatchers(antMatcher(HttpMethod.DELETE, "/api/departments/*")).hasAuthority(PermissionCode.DEPT_DELETE)
				.requestMatchers(antMatcher("/departments/**")).hasAnyAuthority(
					PermissionCode.EMP_READ_TEAM, PermissionCode.EMP_READ_DEPT, PermissionCode.EMP_READ_ALL,
					PermissionCode.DEPT_CREATE, PermissionCode.DEPT_UPDATE, PermissionCode.DEPT_DELETE)
				// 근태 현황 (범위는 서비스에서 팀 / 부서 / 전사로 나눔)
				.requestMatchers(antMatcher("/attendance/status/**")).hasAnyAuthority(
					PermissionCode.ATT_READ_TEAM, PermissionCode.ATT_READ_DEPT, PermissionCode.ATT_READ_ALL)
				// 근태 결재 (/api/attendance/** 출퇴근 규칙보다 위에)
				.requestMatchers(antMatcher("/attendance/corrections/approvals/**"),
					antMatcher("/api/attendance/corrections/*/approve"), antMatcher("/api/attendance/corrections/*/reject"))
					.hasAuthority(PermissionCode.ATT_CORRECT_APPROVE)
				// 근태 정정 신청
				.requestMatchers(antMatcher("/attendance/corrections"), antMatcher("/api/attendance/corrections"),
					antMatcher("/api/attendance/corrections/record"), antMatcher("/api/attendance/corrections/*/cancel"))
					.hasAuthority(PermissionCode.ATT_CORRECT_REQUEST)
				// 계정 관리 (S-26): 잠금 해제 · 비밀번호 초기화 · 사용/중지는 각각의 권한
				.requestMatchers(antMatcher("/api/accounts/*/unlock")).hasAuthority(PermissionCode.ACCOUNT_UNLOCK)
				.requestMatchers(antMatcher("/api/accounts/*/reset-password")).hasAuthority(PermissionCode.ACCOUNT_PASSWORD_RESET)
				.requestMatchers(antMatcher("/api/accounts/*/enable")).hasAuthority(PermissionCode.ACCOUNT_ENABLE)
				.requestMatchers(antMatcher("/accounts")).hasAnyAuthority(
					PermissionCode.ACCOUNT_UNLOCK, PermissionCode.ACCOUNT_PASSWORD_RESET, PermissionCode.ACCOUNT_ENABLE)
				// 권한 그룹 관리 (S-24): 등록 · 수정 · 사용 여부는 각각의 권한
				.requestMatchers(antMatcher(HttpMethod.POST, "/api/roles")).hasAuthority(PermissionCode.ROLE_CREATE)
				.requestMatchers(antMatcher(HttpMethod.PUT, "/api/roles/*")).hasAuthority(PermissionCode.ROLE_UPDATE)
				.requestMatchers(antMatcher(HttpMethod.PATCH, "/api/roles/*/deactivate"), antMatcher(HttpMethod.PATCH, "/api/roles/*/activate"))
					.hasAuthority(PermissionCode.ROLE_DELETE)
				.requestMatchers(antMatcher("/auth/roles")).hasAnyAuthority(
					PermissionCode.ROLE_CREATE, PermissionCode.ROLE_UPDATE, PermissionCode.ROLE_DELETE)
				// 권한 그룹 조회: 권한 그룹 관리 · 계정 권한 부여(S-25 체크박스) · 권한 목록(S-16 개수)에서 씀
				.requestMatchers(antMatcher(HttpMethod.GET, "/api/roles"), antMatcher(HttpMethod.GET, "/api/roles/**")).hasAnyAuthority(
					PermissionCode.ROLE_CREATE, PermissionCode.ROLE_UPDATE, PermissionCode.ROLE_DELETE, PermissionCode.ROLE_ASSIGN,
					PermissionCode.PERM_CREATE, PermissionCode.PERM_UPDATE, PermissionCode.PERM_DELETE)
				// 권한 목록 (S-16): 화면·조회는 권한 관리 권한 중 하나, 등록·수정·삭제는 각각의 권한
				.requestMatchers(antMatcher(HttpMethod.POST, "/api/permissions")).hasAuthority(PermissionCode.PERM_CREATE)
				.requestMatchers(antMatcher(HttpMethod.PUT, "/api/permissions/*")).hasAuthority(PermissionCode.PERM_UPDATE)
				.requestMatchers(antMatcher(HttpMethod.DELETE, "/api/permissions/*")).hasAuthority(PermissionCode.PERM_DELETE)
				// 권한 조회는 권한 그룹 관리(S-24 체크박스)에서도 씀
				.requestMatchers(antMatcher(HttpMethod.GET, "/api/permissions"), antMatcher(HttpMethod.GET, "/api/permissions/**")).hasAnyAuthority(
					PermissionCode.PERM_CREATE, PermissionCode.PERM_UPDATE, PermissionCode.PERM_DELETE,
					PermissionCode.ROLE_CREATE, PermissionCode.ROLE_UPDATE, PermissionCode.ROLE_DELETE)
				.requestMatchers(antMatcher("/auth/permissions"), antMatcher("/api/permissions/**"))
					.hasAnyAuthority(PermissionCode.PERM_CREATE, PermissionCode.PERM_UPDATE, PermissionCode.PERM_DELETE)
				// 계정 권한 부여 (S-25)
				.requestMatchers(antMatcher("/auth/account-roles"), antMatcher("/api/accounts/assignable"),
					antMatcher("/api/accounts/*/roles"), antMatcher("/api/accounts/*/roles/**"))
					.hasAuthority(PermissionCode.ROLE_ASSIGN)
				// 출퇴근 버튼 (대시보드에서 fetch)
				.requestMatchers(antMatcher("/api/attendance/**")).hasAuthority(PermissionCode.ATT_CHECK)
				// 나머지는 로그인만 하면
				.anyRequest().authenticated()
			)

			.formLogin(form -> form
				.loginPage("/loginform")          // 커스텀 로그인 화면
				.loginProcessingUrl("/login")     // fetch 로 POST 할 주소
				.usernameParameter("accountId")   // 파라미터명을 컬럼명(login_id)과 다르게
				.passwordParameter("pwd")
				.successHandler(loginSuccessHandler)
				.failureHandler(loginFailureHandler)
				.permitAll()
			)

			.logout(logout -> logout
				.logoutUrl("/logout")
				.logoutSuccessUrl("/loginform?logout")
				.invalidateHttpSession(true)
			)

			// 권한이 없는 페이지에 들어오면 403 화면으로
			.exceptionHandling(ex -> ex.accessDeniedPage("/error/403"));

		return http.build();
	}

	// 비밀번호 검증용 (DB 에는 BCrypt 로 암호화된 값이 저장되어 있음)
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}