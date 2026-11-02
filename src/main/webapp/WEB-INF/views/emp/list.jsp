<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<c:set var="menuGroup" value="hr" />
<c:set var="menu" value="employees" />
<!DOCTYPE html>
<html lang="ko">
<head>
<%@ include file="/WEB-INF/views/common/head.jsp"%>
<title>직원 목록 | 규성컴퍼니 인사관리</title>
</head>

<body class="layout-fixed sidebar-expand-lg hr-app">
	<div class="app-wrapper">

		<%@ include file="/WEB-INF/views/common/header.jsp"%>
		<%@ include file="/WEB-INF/views/common/sidebar.jsp"%>

		<main class="app-main">
			<div class="app-content-header">
				<div class="container-fluid">
					<div class="row align-items-end">
						<div class="col-sm-8">
							<h2 class="hr-page-title">직원 목록</h2>
							<p class="hr-page-sub">내 조회 범위 안의 직원을 조직·직급·재직 상태로 찾아볼 수 있습니다.</p>
						</div>
						<div class="col-sm-4">
							<ol class="breadcrumb float-sm-end mb-0">
								<li class="breadcrumb-item"><a href="${ctx}/dashboard">홈</a></li>
								<li class="breadcrumb-item">인사 관리</li>
								<li class="breadcrumb-item active" aria-current="page">직원 목록</li>
							</ol>
						</div>
					</div>
				</div>
			</div>

			<div class="app-content">
				<div class="container-fluid">

					<!-- 검색 조건 (GET 이라 검색 결과 주소를 그대로 공유·새로고침 가능) -->
					<div class="card mb-3 mt-3">
						<div class="card-body">
							<form action="${ctx}/employees" method="get" class="row g-2 align-items-end">
								<div class="col-md-3">
									<label class="form-label small text-secondary">조직</label>
									<select name="departmentId" class="form-select">
										<option value="">전체 (내 조회 범위)</option>
										<c:forEach var="dept" items="${departments}">
											<option value="${dept.departmentId}" ${search.departmentId == dept.departmentId ? 'selected' : ''}>
												<c:out value="${dept.indentName}" />
											</option>
										</c:forEach>
									</select>
								</div>
								<div class="col-md-2">
									<label class="form-label small text-secondary">직급</label>
									<select name="jobId" class="form-select">
										<option value="">전체</option>
										<c:forEach var="job" items="${jobs}">
											<option value="${job.jobId}" ${search.jobId == job.jobId ? 'selected' : ''}><c:out value="${job.jobName}" /></option>
										</c:forEach>
									</select>
								</div>
								<div class="col-md-2">
									<label class="form-label small text-secondary">재직 상태</label>
									<select name="status" class="form-select">
										<option value="ACTIVE" ${search.status == 'ACTIVE' ? 'selected' : ''}>재직</option>
										<option value="RESIGNED" ${search.status == 'RESIGNED' ? 'selected' : ''}>퇴직</option>
										<option value="" ${empty search.status ? 'selected' : ''}>전체</option>
									</select>
								</div>
								<div class="col-md-3">
									<label class="form-label small text-secondary">이름 / 사번</label>
									<input type="text" name="keyword" class="form-control" value="<c:out value='${search.keyword}'/>" placeholder="예) 김민수, EMP2020001">
								</div>
								<div class="col-md-2 d-flex gap-2">
									<button type="submit" class="btn btn-primary flex-fill"><i class="bi bi-search"></i> 검색</button>
									<a href="${ctx}/employees" class="btn btn-outline-secondary" title="초기화"><i class="bi bi-arrow-counterclockwise"></i></a>
								</div>
							</form>
						</div>
					</div>

					<!-- 목록 -->
					<div class="card">
						<div class="card-header d-flex align-items-center">
							<h3 class="card-title mb-0">
								조회 범위 <span class="badge text-bg-primary ms-1"><c:out value="${scope.label}" /></span>
							</h3>
							<span class="ms-auto text-secondary small">총 <b>${pager.totalCount}</b>명</span>
							<sec:authorize access="hasAuthority('PERMISSION_EMP_CREATE')">
								<a href="${ctx}/employees/new" class="btn btn-primary btn-sm ms-3"><i class="bi bi-plus-lg"></i> 사원 등록</a>
							</sec:authorize>
						</div>
						<div class="card-body p-0 table-responsive">
							<table class="table table-hover table-striped align-middle mb-0">
								<thead>
									<tr>
										<th style="width: 60px">No</th>
										<th>사번</th>
										<th>이름</th>
										<th>부서</th>
										<th>팀</th>
										<th>직급</th>
										<th>직책</th>
										<th>연락처</th>
										<th>이메일</th>
										<th>입사일</th>
										<th>상태</th>
									</tr>
								</thead>
								<tbody>
									<c:forEach var="emp" items="${pager.list}" varStatus="st">
										<c:set var="dept" value="${emp.department}" />
										<tr>
											<td class="text-secondary">${pager.num + st.index}</td>
											<td><c:out value="${emp.empNo}" /></td>
											<td class="fw-semibold"><a href="${ctx}/employees/${emp.empId}" class="hr-link"><c:out value="${emp.ename}" /></a></td>
											<%-- 팀(3단계) 소속이면 상위가 부, 부·본사 소속이면 팀 없음 --%>
											<td><c:out value="${dept.departmentLevel == 3 ? dept.parent.departmentName : dept.departmentName}" /></td>
											<td><c:out value="${dept.departmentLevel == 3 ? dept.departmentName : '-'}" /></td>
											<td><c:out value="${emp.job.jobName}" /></td>
											<%-- 직책은 조직장 여부로 계산 (Emp.getPositionName) --%>
											<td>
												<c:if test="${emp.positionName != null}">
													<span class="badge text-bg-info"><c:out value="${emp.positionName}" /></span>
												</c:if>
											</td>
											<td><c:out value="${emp.phone}" /></td>
											<td><c:out value="${emp.email}" /></td>
											<td>${emp.hiredate}</td>
											<td>
												<span class="badge ${emp.status == 'ACTIVE' ? 'text-bg-success' : 'text-bg-secondary'}">
													${emp.status == 'ACTIVE' ? '재직' : '퇴직'}
												</span>
											</td>
										</tr>
									</c:forEach>
									<c:if test="${empty pager.list}">
										<tr><td colspan="11" class="text-center text-secondary py-5">조건에 맞는 직원이 없습니다.</td></tr>
									</c:if>
								</tbody>
							</table>
						</div>

						<!-- 페이징 (검색 조건을 유지한 채 페이지만 바꿈) -->
						<div class="card-footer clearfix">
							<ul class="pagination pagination-sm m-0 float-end">
								<li class="page-item ${pager.hasPrevBlock ? '' : 'disabled'}">
									<c:url var="prevUrl" value="/employees">
										<c:param name="keyword" value="${search.keyword}" />
										<c:param name="departmentId" value="${search.departmentId}" />
										<c:param name="jobId" value="${search.jobId}" />
										<c:param name="status" value="${search.status}" />
										<c:param name="currentPage" value="${pager.firstPage - 1}" />
									</c:url>
									<a class="page-link" href="${pager.hasPrevBlock ? prevUrl : '#'}" aria-label="이전 블럭" ${pager.hasPrevBlock ? '' : 'tabindex="-1" aria-disabled="true"'}>&laquo;</a>
								</li>
								<c:forEach var="i" begin="${pager.firstPage}" end="${pager.lastPage}">
									<c:url var="pageUrl" value="/employees">
										<c:param name="keyword" value="${search.keyword}" />
										<c:param name="departmentId" value="${search.departmentId}" />
										<c:param name="jobId" value="${search.jobId}" />
										<c:param name="status" value="${search.status}" />
										<c:param name="currentPage" value="${i}" />
									</c:url>
									<li class="page-item ${i == pager.currentPage ? 'active' : ''}">
										<a class="page-link" href="${pageUrl}">${i}</a>
									</li>
								</c:forEach>
								<li class="page-item ${pager.hasNextBlock ? '' : 'disabled'}">
									<c:url var="nextUrl" value="/employees">
										<c:param name="keyword" value="${search.keyword}" />
										<c:param name="departmentId" value="${search.departmentId}" />
										<c:param name="jobId" value="${search.jobId}" />
										<c:param name="status" value="${search.status}" />
										<c:param name="currentPage" value="${pager.lastPage + 1}" />
									</c:url>
									<a class="page-link" href="${pager.hasNextBlock ? nextUrl : '#'}" aria-label="다음 블럭" ${pager.hasNextBlock ? '' : 'tabindex="-1" aria-disabled="true"'}>&raquo;</a>
								</li>
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
