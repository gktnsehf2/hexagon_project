package com.hexagon.hrms.domain;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

// 실제 기능 권한 (Spring Security의 GrantedAuthority로 사용)
@Getter
@Setter
public class Permission implements Serializable {
	private static final long serialVersionUID = 1L;

	private int permissionId;
	private String permissionCode;
	private String permissionName;
	private String permissionGroup;

	// 화면 표시용 코드 (PERMISSION_ 접두어 생략, 예: EMP_READ_TEAM)
	public String getShortCode() {
		return permissionCode != null && permissionCode.startsWith("PERMISSION_")
			? permissionCode.substring("PERMISSION_".length()) : permissionCode;
	}

	// 화면 표시용 분류 이름
	public String getGroupName() {
		if (permissionGroup == null) {
			return "";
		}
		switch (permissionGroup) {
		case "SECURITY":
			return "보안";
		case "HR":
			return "인사";
		case "ATTENDANCE":
			return "근태";
		case "LEAVE":
			return "휴가";
		case "AUTH":
			return "권한";
		default:
			return permissionGroup;
		}
	}
}
