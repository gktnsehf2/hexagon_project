package com.hexagon.hrms.service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexagon.hrms.common.web.Pager;
import com.hexagon.hrms.dto.response.AccountRowDTO;
import com.hexagon.hrms.exception.BusinessException;
import com.hexagon.hrms.mapper.AccountMapper;
import com.hexagon.hrms.security.AccountDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 계정 관리 (F-25, S-26 / P-07)
 - 잠금 해제        : is_locked 0, 실패 횟수 0                       ACCOUNT_UNLOCK
 - 비밀번호 초기화  : 임시 비밀번호를 만들어 BCrypt 로 저장, 화면에 한 번만 보여 줌   ACCOUNT_PASSWORD_RESET
 - 사용 / 중지      : 내 계정은 중지할 수 없고, 퇴직자 계정은 다시 사용할 수 없음   ACCOUNT_ENABLE
 (로그인 실패 처리용 AccountService 와 나눈 이유: AccountService 는 시큐리티 설정에서 쓰므로
  PasswordEncoder 를 넣으면 SecurityConfig ↔ AccountService 순환 참조가 생김)
*/
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AccountManageService {

	// 헷갈리는 글자(0 O o 1 l I)는 뺐다
	private static final String LETTERS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz";
	private static final String DIGITS = "23456789";
	private static final int TEMP_LENGTH = 10;
	private static final SecureRandom RANDOM = new SecureRandom();

	private final AccountMapper accountMapper;
	private final PasswordEncoder passwordEncoder;

	// status: ACTIVE / LOCKED / DISABLED / 그 외는 전체
	@Transactional(readOnly = true)
	public Pager getList(String status, String keyword, int page) {
		String st = normalizeStatus(status);
		String kw = keyword == null || keyword.trim().isEmpty() ? null : keyword.trim();
		Pager pager = new Pager(page, accountMapper.countManageList(st, kw));
		pager.setList(accountMapper.selectManageList(st, kw, pager.getOffset(), pager.getPageSize()));
		return pager;
	}

	// 상태별 개수 (ALL 포함)
	@Transactional(readOnly = true)
	public Map<String, Integer> getStatusCounts() {
		Map<String, Integer> map = new HashMap<>();
		map.put("ACTIVE", 0);
		map.put("LOCKED", 0);
		map.put("DISABLED", 0);
		int all = 0;
		for (Map<String, Object> row : accountMapper.countByStatus()) {
			int cnt = ((Number) row.get("cnt")).intValue();
			map.put(String.valueOf(row.get("status")), cnt);
			all += cnt;
		}
		map.put("ALL", all);
		return map;
	}

	@Transactional(readOnly = true)
	public AccountRowDTO getAccount(int accountId) {
		AccountRowDTO row = accountMapper.selectManage(accountId);
		if (row == null) {
			throw new BusinessException("계정을 찾을 수 없습니다.");
		}
		return row;
	}

	public static String normalizeStatus(String status) {
		return "ACTIVE".equals(status) || "LOCKED".equals(status) || "DISABLED".equals(status) ? status : null;
	}

	public String unlock(int accountId) {
		AccountRowDTO row = getAccount(accountId);
		if (row.getIsLocked() != 1) {
			throw new BusinessException("잠긴 계정이 아닙니다.");
		}
		accountMapper.unlock(accountId);
		log.debug("계정 잠금 해제 {}", row.getLoginId());
		return row.getEname() + "(" + row.getLoginId() + ") 계정의 잠금을 해제했습니다.";
	}

	// 반환값은 임시 비밀번호 (화면에 한 번만 보여 줌, 저장은 BCrypt 값만)
	public String resetPassword(AccountDetails loginUser, int accountId) {
		AccountRowDTO row = getAccount(accountId);
		if (loginUser.getAccount().getAccountId() == accountId) {
			throw new BusinessException("내 계정의 비밀번호는 여기서 초기화할 수 없습니다.");
		}
		if (row.getEnabled() != 1) {
			throw new BusinessException("사용 중지된 계정입니다. 먼저 사용으로 바꾸세요.");
		}
		String temp = makeTempPassword();
		accountMapper.updatePassword(accountId, passwordEncoder.encode(temp));
		log.debug("비밀번호 초기화 {} (처리 {})", row.getLoginId(), loginUser.getUsername());
		return temp;
	}

	public String setEnabled(AccountDetails loginUser, int accountId, int enabled) {
		if (enabled != 0 && enabled != 1) {
			throw new BusinessException("잘못된 요청입니다.");
		}
		AccountRowDTO row = getAccount(accountId);
		if (row.getEnabled() == enabled) {
			throw new BusinessException(enabled == 1 ? "이미 사용 중인 계정입니다." : "이미 사용 중지된 계정입니다.");
		}
		if (enabled == 0 && loginUser.getAccount().getAccountId() == accountId) {
			throw new BusinessException("내 계정은 사용 중지할 수 없습니다.");
		}
		if (enabled == 1 && row.isResigned()) {
			throw new BusinessException("퇴직자 계정은 다시 사용할 수 없습니다.");
		}
		accountMapper.updateEnabled(accountId, enabled);
		log.debug("계정 {} {}", row.getLoginId(), enabled == 1 ? "사용" : "중지");
		return row.getEname() + "(" + row.getLoginId() + ") 계정을 " + (enabled == 1 ? "사용으로 바꿨습니다." : "사용 중지했습니다.");
	}

	// 영문 + 숫자 10자 (영문·숫자 각각 1자 이상)
	static String makeTempPassword() {
		char[] chars = new char[TEMP_LENGTH];
		String all = LETTERS + DIGITS;
		for (int i = 0; i < TEMP_LENGTH; i++) {
			chars[i] = all.charAt(RANDOM.nextInt(all.length()));
		}
		int digitAt = RANDOM.nextInt(TEMP_LENGTH);
		int letterAt = (digitAt + 1 + RANDOM.nextInt(TEMP_LENGTH - 1)) % TEMP_LENGTH;
		chars[digitAt] = DIGITS.charAt(RANDOM.nextInt(DIGITS.length()));
		chars[letterAt] = LETTERS.charAt(RANDOM.nextInt(LETTERS.length()));
		return new String(chars);
	}
}
