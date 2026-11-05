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

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final LoginSuccessHandler loginSuccessHandler;
	private final LoginFailureHandler loginFailureHandler;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> csrf.disable())

			.authorizeHttpRequests(auth -> auth
				.requestMatchers(antMatcher("/loginform"), antMatcher("/resources/**")).permitAll()
				.requestMatchers(antMatcher("/me")).hasAuthority(PermissionCode.MY_INFO_READ)
				.requestMatchers(antMatcher("/employees/*/photo")).authenticated()
				.requestMatchers(antMatcher("/employees/new")).hasAuthority(PermissionCode.EMP_CREATE)
				.requestMatchers(antMatcher("/employees/*/edit")).hasAuthority(PermissionCode.EMP_UPDATE)
				.requestMatchers(antMatcher("/api/employees/*/resign")).hasAuthority(PermissionCode.EMP_RESIGN)
				.requestMatchers(antMatcher("/employees/**")).hasAnyAuthority(
					PermissionCode.EMP_READ_TEAM, PermissionCode.EMP_READ_DEPT, PermissionCode.EMP_READ_ALL)
				.requestMatchers(antMatcher("/attendance/my/**")).hasAuthority(PermissionCode.MY_ATT_READ)
				.requestMatchers(antMatcher("/leaves/my/**")).hasAuthority(PermissionCode.MY_LEAVE_READ)
				.requestMatchers(antMatcher("/api/leaves/*/cancel")).hasAuthority(PermissionCode.LEAVE_CANCEL)
				.requestMatchers(antMatcher("/leaves/new"), antMatcher("/api/leaves"), antMatcher("/api/leaves/plan"))
					.hasAuthority(PermissionCode.LEAVE_REQUEST)
				.requestMatchers(antMatcher("/leaves/status/**")).hasAuthority(PermissionCode.LEAVE_READ_ALL)
				.requestMatchers(antMatcher("/leaves/approvals/**"), antMatcher("/api/leaves/*/approve"), antMatcher("/api/leaves/*/reject"))
					.hasAuthority(PermissionCode.LEAVE_APPROVE)
				.requestMatchers(antMatcher("/calendar"), antMatcher("/api/calendar/**")).hasAnyAuthority(
					PermissionCode.MY_LEAVE_READ, PermissionCode.LEAVE_APPROVE, PermissionCode.LEAVE_READ_ALL)
				.requestMatchers(antMatcher("/departments/new")).hasAuthority(PermissionCode.DEPT_CREATE)
				.requestMatchers(antMatcher("/departments/*/edit")).hasAuthority(PermissionCode.DEPT_UPDATE)
				.requestMatchers(antMatcher(HttpMethod.DELETE, "/api/departments/*")).hasAuthority(PermissionCode.DEPT_DELETE)
				.requestMatchers(antMatcher("/departments/**")).hasAnyAuthority(
					PermissionCode.EMP_READ_TEAM, PermissionCode.EMP_READ_DEPT, PermissionCode.EMP_READ_ALL,
					PermissionCode.DEPT_CREATE, PermissionCode.DEPT_UPDATE, PermissionCode.DEPT_DELETE)
				.requestMatchers(antMatcher("/attendance/status/**")).hasAnyAuthority(
					PermissionCode.ATT_READ_TEAM, PermissionCode.ATT_READ_DEPT, PermissionCode.ATT_READ_ALL)
				.requestMatchers(antMatcher("/attendance/corrections/approvals/**"),
					antMatcher("/api/attendance/corrections/*/approve"), antMatcher("/api/attendance/corrections/*/reject"))
					.hasAuthority(PermissionCode.ATT_CORRECT_APPROVE)
				.requestMatchers(antMatcher("/attendance/corrections"), antMatcher("/api/attendance/corrections"),
					antMatcher("/api/attendance/corrections/record"), antMatcher("/api/attendance/corrections/*/cancel"))
					.hasAuthority(PermissionCode.ATT_CORRECT_REQUEST)
				.requestMatchers(antMatcher("/api/accounts/*/unlock")).hasAuthority(PermissionCode.ACCOUNT_UNLOCK)
				.requestMatchers(antMatcher("/api/accounts/*/reset-password")).hasAuthority(PermissionCode.ACCOUNT_PASSWORD_RESET)
				.requestMatchers(antMatcher("/api/accounts/*/enable")).hasAuthority(PermissionCode.ACCOUNT_ENABLE)
				.requestMatchers(antMatcher("/accounts")).hasAnyAuthority(
					PermissionCode.ACCOUNT_UNLOCK, PermissionCode.ACCOUNT_PASSWORD_RESET, PermissionCode.ACCOUNT_ENABLE)
				.requestMatchers(antMatcher(HttpMethod.POST, "/api/roles")).hasAuthority(PermissionCode.ROLE_CREATE)
				.requestMatchers(antMatcher(HttpMethod.PUT, "/api/roles/*")).hasAuthority(PermissionCode.ROLE_UPDATE)
				.requestMatchers(antMatcher(HttpMethod.PATCH, "/api/roles/*/deactivate"), antMatcher(HttpMethod.PATCH, "/api/roles/*/activate"))
					.hasAuthority(PermissionCode.ROLE_DELETE)
				.requestMatchers(antMatcher("/auth/roles")).hasAnyAuthority(
					PermissionCode.ROLE_CREATE, PermissionCode.ROLE_UPDATE, PermissionCode.ROLE_DELETE)
				.requestMatchers(antMatcher(HttpMethod.GET, "/api/roles"), antMatcher(HttpMethod.GET, "/api/roles/**")).hasAnyAuthority(
					PermissionCode.ROLE_CREATE, PermissionCode.ROLE_UPDATE, PermissionCode.ROLE_DELETE, PermissionCode.ROLE_ASSIGN,
					PermissionCode.PERM_CREATE, PermissionCode.PERM_UPDATE, PermissionCode.PERM_DELETE)
				.requestMatchers(antMatcher(HttpMethod.POST, "/api/permissions")).hasAuthority(PermissionCode.PERM_CREATE)
				.requestMatchers(antMatcher(HttpMethod.PUT, "/api/permissions/*")).hasAuthority(PermissionCode.PERM_UPDATE)
				.requestMatchers(antMatcher(HttpMethod.DELETE, "/api/permissions/*")).hasAuthority(PermissionCode.PERM_DELETE)
				.requestMatchers(antMatcher(HttpMethod.GET, "/api/permissions"), antMatcher(HttpMethod.GET, "/api/permissions/**")).hasAnyAuthority(
					PermissionCode.PERM_CREATE, PermissionCode.PERM_UPDATE, PermissionCode.PERM_DELETE,
					PermissionCode.ROLE_CREATE, PermissionCode.ROLE_UPDATE, PermissionCode.ROLE_DELETE)
				.requestMatchers(antMatcher("/auth/permissions"), antMatcher("/api/permissions/**"))
					.hasAnyAuthority(PermissionCode.PERM_CREATE, PermissionCode.PERM_UPDATE, PermissionCode.PERM_DELETE)
				.requestMatchers(antMatcher("/auth/account-roles"), antMatcher("/api/accounts/assignable"),
					antMatcher("/api/accounts/*/roles"), antMatcher("/api/accounts/*/roles/**"))
					.hasAuthority(PermissionCode.ROLE_ASSIGN)
				.requestMatchers(antMatcher("/api/attendance/**")).hasAuthority(PermissionCode.ATT_CHECK)
				.anyRequest().authenticated()
			)

			.formLogin(form -> form
				.loginPage("/loginform")          
				.loginProcessingUrl("/login")    
				.usernameParameter("accountId")  
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

			.exceptionHandling(ex -> ex.accessDeniedPage("/error/403"));

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}