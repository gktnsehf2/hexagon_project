<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- S-23 전체 휴가 현황: 기간·조직·유형·상태·이름 조회, 상태별 카드, 결재자, 엑셀(CSV) --%>
<c:set var="menuGroup" value="leave" />
<c:set var="menu" value="leaveStatus" />
<!DOCTYPE html>
<html lang="ko">
<head>
<%@ include file="/WEB-INF/views/common/head.jsp"%>
<title>전체 휴가 현황 | HexagonGMI 인사관리</title>
</head>

<body class="layout-fixed sidebar-expand-lg hr-app">
	<div class="app-wrapper">

		<%@ include file="/WEB-INF/views/common/header.jsp"%>
		<%@ include file="/WEB-INF/views/common/sidebar.jsp"%>

		<main class="app-main">
			<div class="app-content-header">
				<div class="container-fluid">
					<div class="row align-items-end g-2">
						<div class="col-md-6">
							<h2 class="hr-page-title">전체 휴가 현황</h2>
							<p class="hr-page-sub">전 직원의 휴가 신청과 결재 상태를 조회합니다.</p>
						</div>
						<div class="col-md-6">
							<ol class="breadcrumb justify-content-md-end mb-2">
								<li class="breadcrumb-item"><a href="${ctx}/dashboard">홈</a></li>
								<li class="breadcrumb-item">휴가 관리</li>
								<li class="breadcrumb-item active" aria-current="page">전체 휴가 현황</li>
							</ol>
						</div>
					</div>
				</div>
			</div>

			<div class="app-content">
				<div class="container-fluid">

					<!-- 검색 조건 (GET 이라 주소 그대로 새로고침·공유 가능) -->
					<div class="card mb-3 mt-1">
						<div class="card-body">
							<form action="${ctx}/leaves/status" method="get" class="row g-2 align-items-end" id="search-form">
								<div class="col-md-3">
									<label class="form-label small text-secondary">휴가 기간</label>
									<div class="input-group">
										<input type="date" name="from" class="form-control" value="${search.from}" aria-label="시작일">
										<span class="input-group-text">~</span>
										<input type="date" name="to" class="form-control" value="${search.to}" aria-label="종료일">
									</div>
								</div>
								<div class="col-md-2">
									<label class="form-label small text-secondary">조직</label>
									<select name="departmentId" class="form-select">
										<option value="">전사</option>
										<c:forEach var="dept" items="${departments}">
											<option value="${dept.departmentId}" ${search.departmentId == dept.departmentId ? 'selected' : ''}><c:out value="${dept.indentName}" /></option>
										</c:forEach>
									</select>
								</div>
								<div class="col-md-2">
									<label class="form-label small text-secondary">휴가 유형</label>
									<select name="leaveTypeId" class="form-select">
										<option value="">전체</option>
										<c:forEach var="t" items="${leaveTypes}">
											<option value="${t.leaveTypeId}" ${search.leaveTypeId == t.leaveTypeId ? 'selected' : ''}><c:out value="${t.leaveTypeName}" /></option>
										</c:forEach>
									</select>
								</div>
								<div class="col-md-1">
									<label class="form-label small text-secondary">상태</label>
									<select name="status" class="form-select">
										<option value="">전체</option>
										<option value="REQUESTED" ${search.status == 'REQUESTED' ? 'selected' : ''}>대기</option>
										<option value="APPROVED" ${search.status == 'APPROVED' ? 'selected' : ''}>승인</option>
										<option value="REJECTED" ${search.status == 'REJECTED' ? 'selected' : ''}>반려</option>
										<option value="CANCELED" ${search.status == 'CANCELED' ? 'selected' : ''}>취소</option>
									</select>
								</div>
								<div class="col-md-2">
									<label class="form-label small text-secondary">이름 / 사번</label>
									<input type="text" name="keyword" class="form-control" value="<c:out value='${search.keyword}'/>" placeholder="예) 백미영, 20230003">
								</div>
								<div class="col-md-2 d-flex gap-2">
									<button type="submit" class="btn btn-primary flex-fill"><i class="bi bi-search"></i> 검색</button>
									<a href="${ctx}/leaves/status" class="btn btn-outline-secondary" title="초기화"><i class="bi bi-arrow-counterclockwise"></i></a>
								</div>
							</form>
						</div>
					</div>

					<!-- 상태별 (상태 조건은 빼고 집계) -->
					<div class="row g-3">
						<div class="col-sm-6 col-xl">
							<div class="hr-card hr-stat">
								<div class="hr-stat-icon is-blue"><i class="bi bi-file-earmark-text"></i></div>
								<div>
									<div class="hr-stat-label">전체 신청</div>
									<div class="hr-stat-value">${summary.totalCount}<small>건</small></div>
									<div class="hr-stat-sub">${search.from} ~ ${search.to}</div>
								</div>
							</div>
						</div>
						<div class="col-sm-6 col-xl">
							<div class="hr-card hr-stat">
								<div class="hr-stat-icon is-orange"><i class="bi bi-hourglass-split"></i></div>
								<div>
									<div class="hr-stat-label">대기</div>
									<div class="hr-stat-value">${summary.requestedCount}<small>건</small></div>
									<div class="hr-stat-sub">결재자 처리 전</div>
								</div>
							</div>
						</div>
						<div class="col-sm-6 col-xl">
							<div class="hr-card hr-stat">
								<div class="hr-stat-icon is-green"><i class="bi bi-check-circle-fill"></i></div>
								<div>
									<div class="hr-stat-label">승인</div>
									<div class="hr-stat-value">${summary.approvedCount}<small>건</small></div>
									<div class="hr-stat-sub">사용 일수 합 ${summary.approvedDaysText}일</div>
								</div>
							</div>
						</div>
						<div class="col-sm-6 col-xl">
							<div class="hr-card hr-stat">
								<div class="hr-stat-icon is-red"><i class="bi bi-x-circle-fill"></i></div>
								<div>
									<div class="hr-stat-label">반려</div>
									<div class="hr-stat-value">${summary.rejectedCount}<small>건</small></div>
									<div class="hr-stat-sub">사유와 함께 신청자에게 표시</div>
								</div>
							</div>
						</div>
						<div class="col-sm-6 col-xl">
							<div class="hr-card hr-stat">
								<div class="hr-stat-icon is-gray"><i class="bi bi-slash-circle"></i></div>
								<div>
									<div class="hr-stat-label">취소</div>
									<div class="hr-stat-value">${summary.canceledCount}<small>건</small></div>
									<div class="hr-stat-sub">신청자 본인 취소</div>
								</div>
							</div>
						</div>
					</div>

					<!-- 목록 -->
					<div class="card mt-3">
						<div class="card-header d-flex align-items-center">
							<h3 class="card-title mb-0">휴가 신청 목록</h3>
							<span class="ms-auto text-secondary small">총 <b>${pager.totalCount}</b>건</span>
							<c:url var="excelUrl" value="/leaves/status/excel">
								<c:param name="from" value="${search.from}" />
								<c:param name="to" value="${search.to}" />
								<c:param name="departmentId" value="${search.departmentId}" />
								<c:param name="leaveTypeId" value="${search.leaveTypeId}" />
								<c:param name="status" value="${search.status}" />
								<c:param name="keyword" value="${search.keyword}" />
							</c:url>
							<a href="${excelUrl}" class="btn btn-outline-secondary btn-sm ms-3"><i class="bi bi-file-earmark-spreadsheet"></i> 엑셀 다운로드</a>
						</div>
						<div class="card-body p-0 table-responsive">
							<table class="table table-hover align-middle mb-0">
								<thead>
									<tr>
										<th style="width: 60px">No</th>
										<th>신청번호</th>
										<th>신청자</th>
										<th>부서</th>
										<th>휴가 유형</th>
										<th>사용 기간</th>
										<th class="text-end">일수</th>
										<th>사유</th>
										<th>결재자</th>
										<th>신청일</th>
										<th>상태</th>
									</tr>
								</thead>
								<tbody>
									<c:forEach var="leave" items="${pager.list}" varStatus="st">
										<tr>
											<td class="text-secondary">${pager.num + st.index}</td>
											<td class="hr-time text-secondary">${leave.requestNo}</td>
											<td>
												<span class="fw-semibold"><c:out value="${leave.emp.ename}" /></span>
												<div class="text-secondary small"><c:out value="${leave.emp.empNo}" /> · <c:out value="${leave.emp.job.jobName}" /></div>
											</td>
											<td><c:out value="${leave.emp.department.departmentName}" /></td>
											<td><span class="hr-badge ${leave.typeTone}"><c:out value="${leave.leaveType.leaveTypeName}" /></span></td>
											<td class="hr-time">${leave.periodText}</td>
											<td class="text-end hr-time">${leave.daysText}일</td>
											<td><c:out value="${empty leave.reason ? '-' : leave.reason}" /></td>
											<td>
												<c:choose>
													<c:when test="${leave.approver != null}">
														<c:out value="${leave.approver.ename}" /> <span class="text-secondary small"><c:out value="${leave.approver.job.jobName}" /></span>
														<c:if test="${not empty leave.approvedAtText}"><div class="text-secondary small">${leave.approvedAtText}</div></c:if>
													</c:when>
													<c:otherwise><span class="text-secondary">결재 없음</span></c:otherwise>
												</c:choose>
											</td>
											<td class="hr-time text-secondary">${leave.requestDateText}</td>
											<td>
												<span class="hr-badge ${leave.statusTone}">${leave.statusName}</span>
												<c:if test="${not empty leave.rejectReason}"><div class="hr-reject"><c:out value="${leave.rejectReason}" /></div></c:if>
											</td>
										</tr>
									</c:forEach>
									<c:if test="${empty pager.list}">
										<tr><td colspan="11" class="text-center text-secondary py-5">조건에 맞는 휴가 신청이 없습니다.</td></tr>
									</c:if>
								</tbody>
							</table>
						</div>

						<!-- 페이징 (검색 조건을 유지한 채 페이지만 바꿈) -->
						<div class="card-footer clearfix">
							<span class="text-secondary small"><i class="bi bi-info-circle"></i> 휴가 기간이 조회 기간과 겹치는 신청을 보여줍니다. 조직을 고르면 하위 조직까지 포함합니다.</span>
							<ul class="pagination pagination-sm m-0 float-end">
								<c:forEach var="i" begin="${pager.firstPage - 1}" end="${pager.lastPage + 1}">
									<c:set var="isPrev" value="${i == pager.firstPage - 1}" />
									<c:set var="isNext" value="${i == pager.lastPage + 1}" />
									<c:set var="enabled" value="${isPrev ? pager.hasPrevBlock : (isNext ? pager.hasNextBlock : true)}" />
									<c:url var="pageUrl" value="/leaves/status">
										<c:param name="from" value="${search.from}" />
										<c:param name="to" value="${search.to}" />
										<c:param name="departmentId" value="${search.departmentId}" />
										<c:param name="leaveTypeId" value="${search.leaveTypeId}" />
										<c:param name="status" value="${search.status}" />
										<c:param name="keyword" value="${search.keyword}" />
										<c:param name="currentPage" value="${i}" />
									</c:url>
									<li class="page-item ${!enabled ? 'disabled' : ''} ${i == pager.currentPage ? 'active' : ''}">
										<a class="page-link" href="${enabled ? pageUrl : '#'}" ${enabled ? '' : 'tabindex="-1" aria-disabled="true"'}
										   ${isPrev ? 'aria-label="이전 블럭"' : (isNext ? 'aria-label="다음 블럭"' : '')}>${isPrev ? '&laquo;' : (isNext ? '&raquo;' : i)}</a>
									</li>
								</c:forEach>
							</ul>
						</div>
					</div>

				</div>
			</div>
		</main>

	</div>

	<%@ include file="/WEB-INF/views/common/scripts.jsp"%>
</body>
</html>
