<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%-- S-09 근태 현황: 조회 범위(팀/부서/전사) 재직자의 월별 출근·지각·조퇴·결근·초과근무 --%>
<c:set var="menuGroup" value="att" />
<c:set var="menu" value="attStatus" />
<!DOCTYPE html>
<html lang="ko">
<head>
<%@ include file="/WEB-INF/views/common/head.jsp"%>
<title>근태 현황 | HexagonGMI 인사관리</title>
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
							<h2 class="hr-page-title">근태 현황</h2>
							<p class="hr-page-sub">조회 범위 안 직원들의 월별 출근 · 지각 · 조퇴 · 결근 · 초과근무를 확인합니다.</p>
						</div>
						<div class="col-md-6">
							<ol class="breadcrumb justify-content-md-end mb-2">
								<li class="breadcrumb-item"><a href="${ctx}/dashboard">홈</a></li>
								<li class="breadcrumb-item">근태 관리</li>
								<li class="breadcrumb-item active" aria-current="page">근태 현황</li>
							</ol>
						</div>
					</div>
				</div>
			</div>

			<div class="app-content">
				<div class="container-fluid">

					<!-- 검색 조건 -->
					<div class="card mb-3 mt-1">
						<div class="card-body">
							<form action="${ctx}/attendance/status" method="get" class="row g-2 align-items-end">
								<div class="col-md-3">
									<label class="form-label small text-secondary" for="month">조회 월</label>
									<div class="input-group">
										<c:url var="prevUrl" value="/attendance/status"><c:param name="month" value="${att.prevMonth}" /><c:param name="departmentId" value="${departmentId}" /><c:param name="keyword" value="${keyword}" /></c:url>
										<c:url var="nextUrl" value="/attendance/status"><c:param name="month" value="${att.nextMonth}" /><c:param name="departmentId" value="${departmentId}" /><c:param name="keyword" value="${keyword}" /></c:url>
										<a href="${att.prevMonth != null ? prevUrl : '#'}" class="btn btn-outline-secondary ${att.prevMonth == null ? 'disabled' : ''}" title="이전 달"><i class="bi bi-chevron-left"></i></a>
										<input type="month" id="month" name="month" class="form-control" value="${att.month}" min="${att.minMonth}" max="${att.maxMonth}">
										<a href="${att.nextMonth != null ? nextUrl : '#'}" class="btn btn-outline-secondary ${att.nextMonth == null ? 'disabled' : ''}" title="다음 달"><i class="bi bi-chevron-right"></i></a>
									</div>
								</div>
								<div class="col-md-3">
									<label class="form-label small text-secondary">조직</label>
									<select name="departmentId" class="form-select">
										<option value="">전체 (내 조회 범위)</option>
										<c:forEach var="dept" items="${departments}">
											<option value="${dept.departmentId}" ${departmentId == dept.departmentId ? 'selected' : ''}><c:out value="${dept.indentName}" /></option>
										</c:forEach>
									</select>
								</div>
								<div class="col-md-3">
									<label class="form-label small text-secondary">이름 / 사번</label>
									<input type="text" name="keyword" class="form-control" value="<c:out value='${keyword}'/>" placeholder="예) 백미영, 20230003">
								</div>
								<div class="col-md-3 d-flex gap-2">
									<button type="submit" class="btn btn-primary flex-fill"><i class="bi bi-search"></i> 조회</button>
									<a href="${ctx}/attendance/status" class="btn btn-outline-secondary" title="초기화"><i class="bi bi-arrow-counterclockwise"></i></a>
								</div>
							</form>
						</div>
					</div>

					<!-- 합계 -->
					<div class="row g-3">
						<div class="col-sm-6 col-xl-3">
							<div class="hr-card hr-stat">
								<div class="hr-stat-icon is-blue"><i class="bi bi-people-fill"></i></div>
								<div>
									<div class="hr-stat-label">대상 인원</div>
									<div class="hr-stat-value">${att.empCount}<small>명</small></div>
									<div class="hr-stat-sub">근태 이상 ${att.issueEmpCount}명</div>
								</div>
							</div>
						</div>
						<div class="col-sm-6 col-xl-3">
							<div class="hr-card hr-stat">
								<div class="hr-stat-icon is-orange"><i class="bi bi-alarm-fill"></i></div>
								<div>
									<div class="hr-stat-label">지각 · 조퇴</div>
									<div class="hr-stat-value">${att.lateTotal}<small>회</small> · ${att.earlyLeaveTotal}<small>회</small></div>
									<div class="hr-stat-sub">09:01 이후 출근 · 18:00 전 퇴근</div>
								</div>
							</div>
						</div>
						<div class="col-sm-6 col-xl-3">
							<div class="hr-card hr-stat">
								<div class="hr-stat-icon is-red"><i class="bi bi-x-circle-fill"></i></div>
								<div>
									<div class="hr-stat-label">결근</div>
									<div class="hr-stat-value">${att.absentTotal}<small>일</small></div>
									<div class="hr-stat-sub">주말 · 공휴일 · 휴가 제외</div>
								</div>
							</div>
						</div>
						<div class="col-sm-6 col-xl-3">
							<div class="hr-card hr-stat">
								<div class="hr-stat-icon is-purple"><i class="bi bi-moon-stars-fill"></i></div>
								<div>
									<div class="hr-stat-label">초과근무 합계</div>
									<div class="hr-stat-value" style="font-size: 1.35rem">${att.overtimeTotalText}</div>
									<div class="hr-stat-sub">18:00 이후, 30분 단위</div>
								</div>
							</div>
						</div>
					</div>

					<!-- 목록 -->
					<div class="card mt-3">
						<div class="card-header d-flex align-items-center">
							<h3 class="card-title mb-0">
								${att.monthText} · 조회 범위 <span class="badge text-bg-primary ms-1"><c:out value="${scope.label}" /></span>
							</h3>
							<span class="ms-auto text-secondary small">총 <b>${att.empCount}</b>명</span>
							<c:url var="excelUrl" value="/attendance/status/excel">
								<c:param name="month" value="${att.month}" />
								<c:param name="departmentId" value="${departmentId}" />
								<c:param name="keyword" value="${keyword}" />
							</c:url>
							<a href="${excelUrl}" class="btn btn-outline-secondary btn-sm ms-3"><i class="bi bi-file-earmark-spreadsheet"></i> 엑셀 다운로드</a>
						</div>
						<div class="card-body p-0 table-responsive">
							<table class="table table-hover align-middle mb-0">
								<thead>
									<tr>
										<th style="width: 60px">No</th>
										<th>사번</th>
										<th>이름</th>
										<th>부서</th>
										<th>직급</th>
										<th class="text-end">출근일</th>
										<th class="text-end">정상</th>
										<th class="text-end">지각</th>
										<th class="text-end">조퇴</th>
										<th class="text-end">결근</th>
										<th class="text-end">휴가</th>
										<th class="text-end">총 근무</th>
										<th class="text-end">초과근무</th>
									</tr>
								</thead>
								<tbody>
									<c:forEach var="r" items="${att.rows}" varStatus="st">
										<tr>
											<td class="text-secondary">${st.count}</td>
											<td class="hr-time"><c:out value="${r.emp.empNo}" /></td>
											<td class="fw-semibold"><c:out value="${r.emp.ename}" /></td>
											<td><c:out value="${r.emp.department.departmentName}" /></td>
											<td><c:out value="${r.emp.job.jobName}" /></td>
											<td class="text-end hr-time">${r.workDays}</td>
											<td class="text-end hr-time">${r.normalCount}</td>
											<td class="text-end hr-time ${r.lateCount > 0 ? 'text-warning fw-semibold' : 'text-secondary'}">${r.lateCount}</td>
											<td class="text-end hr-time ${r.earlyLeaveCount > 0 ? 'text-purple fw-semibold' : 'text-secondary'}">${r.earlyLeaveCount}</td>
											<td class="text-end hr-time ${r.absentCount > 0 ? 'text-danger fw-semibold' : 'text-secondary'}">${r.absentCount}</td>
											<td class="text-end hr-time">${r.leaveDays}</td>
											<td class="text-end hr-time">${r.totalWorkText}</td>
											<td class="text-end hr-time">${r.overtimeText}</td>
										</tr>
									</c:forEach>
									<c:if test="${empty att.rows}">
										<tr><td colspan="13" class="text-center text-secondary py-5">조건에 맞는 직원이 없습니다.</td></tr>
									</c:if>
								</tbody>
							</table>
						</div>
						<div class="card-footer">
							<span class="text-secondary small">
								<i class="bi bi-info-circle"></i> 하루 판정은 내 근태와 같습니다. 기록 없는 평일 = 결근(주말 · 공휴일 · 승인된 휴가 · 오늘 제외),
								총 근무는 점심 1시간 제외, 초과근무는 18:00 이후를 30분 단위로 셉니다. 승인된 근태 정정은 바로 반영됩니다.
							</span>
						</div>
					</div>

				</div>
			</div>
		</main>

	</div>

	<%@ include file="/WEB-INF/views/common/scripts.jsp"%>
</body>
</html>
