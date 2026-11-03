
package com.hexagon.hrms.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.hexagon.hrms.domain.HrRole;

@Mapper
public interface HrRoleMapper {

	// 1. 특정 계정의 활성 Role 조회 (Permission 포함)
	List<HrRole> selectByAccountId(int accountId);

	// 2. Role 코드로 조회 (Permission 제외)
	HrRole selectByCode(String roleCode);

	// 3. 전체 Role 조회 (Permission 포함)
	List<HrRole> selectAll();

	// 4. Role ID로 상세 조회 (Permission 포함)
	HrRole selectById(int hrRoleId);

	// 5. 신규 Role 등록
	int insert(HrRole hrRole);

	// 6. Role 이름 수정
	int update(HrRole hrRole);

	// 7. 해당 Role을 부여받은 계정 수 조회
	int countAssignedAccounts(int hrRoleId);

	// 8. Role 비활성화
	int deactivate(int hrRoleId);

	// 9. Role 재활성화
	int activate(int hrRoleId);

	// Role에 Permission 연결
	int insertPermission(@Param("hrRoleId") int hrRoleId, @Param("permissionId") int permissionId);

	// Role에 연결된 Permission 전체 해제
	int deletePermissionsByHrRoleId(int hrRoleId);
	
	// 활성화된 Role 목록 조회
	List<HrRole> selectActive();
	
	// 등록된 전체 Role 개수 조회 (활성·비활성 포함)
	int countAll();
	
	

}
