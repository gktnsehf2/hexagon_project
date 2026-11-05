package com.hexagon.hrms.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.hexagon.hrms.domain.Account;
import com.hexagon.hrms.mapper.AccountMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final AccountMapper accountMapper;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Account account = accountMapper.selectByLoginId(username);
		if (account == null) {
			throw new UsernameNotFoundException("존재하지 않는 아이디: " + username);
		}
		return new AccountDetails(account);
	}
}
