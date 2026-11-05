package com.hexagon.hrms.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.hexagon.hrms.domain.Account;
import com.hexagon.hrms.domain.HrRole;
import com.hexagon.hrms.domain.Permission;

import lombok.Getter;

@Getter
public class AccountDetails implements UserDetails {
	private static final long serialVersionUID = 1L;

	private final Account account;
	private final List<GrantedAuthority> authorities;

	public AccountDetails(Account account) {
		this.account = account;

		Set<String> codes = new LinkedHashSet<>();
		if (account.getRoles() != null) {
			for (HrRole role : account.getRoles()) {
				codes.add(role.getRoleCode());
				if (role.getPermissions() != null) {
					for (Permission permission : role.getPermissions()) {
						codes.add(permission.getPermissionCode());
					}
				}
			}
		}
		List<GrantedAuthority> list = new ArrayList<>();
		for (String code : codes) {
			list.add(new SimpleGrantedAuthority(code));
		}
		this.authorities = list;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	public boolean hasAuthority(String code) {
		for (GrantedAuthority authority : authorities) {
			if (authority.getAuthority().equals(code)) {
				return true;
			}
		}
		return false;
	}

	public int getEmpId() {
		return account.getEmp().getEmpId();
	}

	@Override
	public String getPassword() {
		return account.getPassword();
	}

	@Override
	public String getUsername() {
		return account.getLoginId();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return account.getIsLocked() == 0;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return account.getEnabled() == 1
			&& account.getEmp() != null
			&& "ACTIVE".equals(account.getEmp().getStatus());
	}
}
