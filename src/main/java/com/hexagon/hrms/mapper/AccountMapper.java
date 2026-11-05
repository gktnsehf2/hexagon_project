package com.hexagon.hrms.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.hexagon.hrms.domain.Account;
import com.hexagon.hrms.dto.response.AccountRowDTO;

@Mapper
public interface AccountMapper {
	// 로그인용: 사원 정보 + Role + Role별 권한까지 함께 가져옴
	Account selectByLoginId(String loginId);

	// 로그인 실패 처리용: 계정 기본 정보만
	Account selectSimpleByLoginId(String loginId);

	// 로그인 성공: 실패 횟수 초기화 + 마지막 로그인 시각 기록
	int updateLoginSuccess(int accountId);

	// 로그인 실패: 실패 횟수 갱신 (5회째면 잠금까지)
	int updateLoginFail(Account account);

	// 사원 상세: 사원의 계정 + Role 
	Account selectByEmpId(int empId);

	// 아이디 중복 확인
	int countByLoginId(String loginId);

	// 계정 발급 (생성된 account_id 는 account.accountId 에 채워짐)
	int insert(Account account);

	// 사용 / 중지 (퇴직 처리하면 0)
	int updateEnabledByEmpId(@Param("empId") int empId, @Param("enabled") int enabled);
	
	// 계정 PK로 기본 정보 조회
	Account selectById(int accountId);
	
	// Role 부여가 가능한 활성 계정 조회
	List<Account> selectAssignableAccounts();
	
	//Role 변경이 가능한 활성 계정 단건 조회
	Account selectAssignableAccountById(int accountId);

	// status: ACTIVE(사용) / LOCKED(잠김) / DISABLED(사용 중지) / null(전체), keyword: 아이디·이름·사번
	List<AccountRowDTO> selectManageList(@Param("status") String status, @Param("keyword") String keyword,
										 @Param("offset") int offset, @Param("size") int size);

	int countManageList(@Param("status") String status, @Param("keyword") String keyword);

	// 상태별 계정 수
	List<java.util.Map<String, Object>> countByStatus();

	AccountRowDTO selectManage(int accountId);

	// 잠금 해제 
	int unlock(int accountId);

	// 비밀번호 초기화
	int updatePassword(@Param("accountId") int accountId, @Param("password") String password);

	// 계정 단위 사용 / 중지
	int updateEnabled(@Param("accountId") int accountId, @Param("enabled") int enabled);
}
