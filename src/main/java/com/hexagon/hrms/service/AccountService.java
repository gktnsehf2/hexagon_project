package com.hexagon.hrms.service;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexagon.hrms.domain.Account;
import com.hexagon.hrms.mapper.AccountMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AccountService {

	public static final int MAX_FAIL_COUNT = 5;

	private final AccountMapper accountMapper;

	public void loginSucceeded(Account account) {
		accountMapper.updateLoginSuccess(account.getAccountId());
	}

	public String loginFailed(String loginId, AuthenticationException exception) {
		Account account = (loginId == null) ? null : accountMapper.selectSimpleByLoginId(loginId);

		if (exception instanceof LockedException) {
			return "잠긴 계정입니다. 인사 담당자에게 잠금 해제를 요청하세요.";
		}
		if (exception instanceof DisabledException) {
			return "사용이 중지된 계정입니다.";
		}
		if (account == null) {
			return "아이디 또는 비밀번호가 올바르지 않습니다.";
		}

		int failCount = account.getFailCount() + 1;
		account.setFailCount(failCount);
		if (failCount >= MAX_FAIL_COUNT) {
			account.setIsLocked(1);
			accountMapper.updateLoginFail(account);
			log.debug("{} 계정 잠금 (실패 {}회)", loginId, failCount);
			return "비밀번호를 " + MAX_FAIL_COUNT + "회 틀려 계정이 잠겼습니다. 인사 담당자에게 잠금 해제를 요청하세요.";
		}
		account.setIsLocked(0);
		accountMapper.updateLoginFail(account);
		return "아이디 또는 비밀번호가 올바르지 않습니다. (" + failCount + "/" + MAX_FAIL_COUNT + "회, "
			+ MAX_FAIL_COUNT + "회 틀리면 계정이 잠깁니다)";
	}
}
