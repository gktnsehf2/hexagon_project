package com.hexagon.hrms.dto.request;

import lombok.Getter;
import lombok.Setter;

// 사원 목록 검색 조건 
@Getter
@Setter
public class EmpSearchRequestDTO {
	private String keyword;        			    // 이름 또는 사번
	private Integer departmentId;    	   // 조직 (선택한 조직과 그 하위 조직)
	private Integer jobId;           			  // 직급
	private String status = "ACTIVE";    // 기본은 재직자만
	private int currentPage = 1;
}
