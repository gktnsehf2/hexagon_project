package com.hexagon.hrms.common.web;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Pager {
	private List<?> list;   		    // 현재 페이지에 보여줄 목록
	private int totalCount;   		// 전체 레코드 수
	private int pageSize = 10;		// 페이지당 레코드 수
	private int blockSize = 10;	// 블럭당 페이지 수
	private int totalPage;
	private int currentPage = 1;
	private int firstPage		;     	// 현재 블럭의 시작 페이지
	private int lastPage;    		  	// 현재 블럭의 끝 페이지 (totalPage 를 넘지 않음)
	private int offset;       			// limit 에 쓸 시작 위치 
	private int num;           		// 현재 페이지 첫 줄 번호 

	public Pager(int currentPage, int totalCount) {
		this.totalCount = totalCount;
		this.totalPage = Math.max(1, (int) Math.ceil((double) totalCount / pageSize));
		this.currentPage = Math.min(Math.max(1, currentPage), totalPage);
		this.firstPage = this.currentPage - (this.currentPage - 1) % blockSize;
		this.lastPage = Math.min(firstPage + blockSize - 1, totalPage);
		this.offset = (this.currentPage - 1) * pageSize;
		this.num = offset + 1;
	}

	public boolean isHasPrevBlock() {
		return firstPage > 1;
	}

	public boolean isHasNextBlock() {
		return lastPage < totalPage;
	}
}
