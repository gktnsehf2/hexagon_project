<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<c:set var="menuGroup" value="att" />
<c:set var="menu" value="myAtt" />
<!DOCTYPE html>
<html lang="ko">
<head>
<%@ include file="/WEB-INF/views/common/head.jsp"%>
<title>내 근태 | HexagonGMI 인사관리</title>
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
							<h2 class="hr-page-title">내 근태</h2>
							<p class="hr-page-sub">월별 출퇴근 기록과 지각 · 조퇴 · 결근 현황을 확인합니다.</p>
						</div>
						<div class="col-md-6">
							<ol class="breadcrumb justify-content-md-end mb-2">
								<li class="breadcrumb-item"><a href="${ctx}/dashboard">홈</a></li>
								<li class="breadcrumb-item">근태 관리</li>
								<li class="breadcrumb-item active" aria-current="page">내 근태</li>
							</ol>
						</div>
					</div>
				</div>
			</div>

			<div class="app-content">
				<div class="container-fluid">

					<!-- 조회 기간 (GET 이라 주소를 그대로 새로고침·공유 가능) -->
					<div class="hr-card hr-filter mt-1 mb-3">
						<form action="${ctx}/attendance/my" method="get" class="d-flex flex-wrap align-items-end gap-2">
							<div>
								<label for="month" class="form-label small text-secondary mb-1">조회 기간</label>
								<div class="input-group">
									<c:choose>
										<c:when test="${att.prevMonth != null}">
											<a href="${ctx}/attendance/my?month=${att.prevMonth}" class="btn btn-outline-secondary" title="이전 달"><i class="bi bi-chevron-left"></i></a>
										</c:when>
										<c:otherwise><button type="button" class="btn btn-outline-secondary" disabled><i class="bi bi-chevron-left"></i></button></c:otherwise>
									</c:choose>
									<input type="month" id="month" name="month" class="form-control" value="${att.month}" min="${att.minMonth}" max="${att.maxMonth}" required>
									<c:choose>
										<c:when test="${att.nextMonth != null}">
											<a href="${ctx}/attendance/my?month=${att.nextMonth}" class="btn btn-outline-secondary" title="다음 달"><i class="bi bi-chevron-right"></i></a>
										</c:when>
										<c:otherwise><button type="button" class="btn btn-outline-secondary" disabled><i class="bi bi-chevron-right"></i></button></c:otherwise>
									</c:choose>
								</div>
							</div>
							<button type="submit" class="btn btn-primary"><i class="bi bi-search"></i> 조회</button>
							<a href="${ctx}/attendance/my/download?month=${att.month}" class="btn btn-outline-secondary ms-auto"><i class="bi bi-download"></i> 기록 다운로드</a>
						</form>
					</div>

					<!-- 요약 -->
					<div class="row g-3">
						<div class="col-sm-6 col-xl-3">
							<div class="hr-card hr-stat">
								<div class="hr-stat-icon is-green"><i class="bi bi-check-circle-fill"></i></div>
								<div>
									<div class="hr-stat-label">정상 출근</div>
									<div class="hr-stat-value">${att.normalCount}<small>일</small></div>
									<div class="hr-stat-sub">출근한 날 총 ${att.workDays}일</div>
								</div>
							</div>
						</div>
						<div class="col-sm-6 col-xl-3">
							<div class="hr-card hr-stat">
								<div class="hr-stat-icon is-orange"><i class="bi bi-alarm-fill"></i></div>
								<div>
									<div class="hr-stat-label">지각</div>
									<div class="hr-stat-value">${att.lateCount}<small>회</small></div>
									<div class="hr-stat-sub">09:01 이후 출근</div>
								</div>
							</div>
						</div>
						<div class="col-sm-6 col-xl-3">
							<div class="hr-card hr-stat">
								<div class="hr-stat-icon is-purple"><i class="bi bi-box-arrow-left"></i></div>
								<div>
									<div class="hr-stat-label">조퇴</div>
									<div class="hr-stat-value">${att.earlyLeaveCount}<small>회</small></div>
									<div class="hr-stat-sub">18:00 전 퇴근</div>
								</div>
							</div>
						</div>
						<div class="col-sm-6 col-xl-3">
							<div class="hr-card hr-stat">
								<div class="hr-stat-icon is-red"><i class="bi bi-x-circle-fill"></i></div>
								<div>
									<div class="hr-stat-label">결근</div>
									<div class="hr-stat-value">${att.absentCount}<small>일</small></div>
									<div class="hr-stat-sub">휴가 ${att.leaveDays}일은 제외</div>
								</div>
							</div>
						</div>
					</div>

					<!-- 일별 기록 -->
					<div class="hr-card hr-section mt-3">
						<div class="hr-section-head">
							<div>
								<h3 class="hr-section-title">근태 기록 <span class="hr-count">(${fn:length(att.days)})</span></h3>
								<p class="hr-section-sub">${att.monthText} · 총 근무 <b>${att.totalWorkText}</b> · 초과 근무 <b>${att.totalOvertimeText}</b></p>
							</div>
						</div>

						<c:choose>
							<c:when test="${empty att.days}">
								<p class="hr-empty mb-0">이 달에는 근태 기록이 없습니다.</p>
							</c:when>
							<c:otherwise>
								<div class="table-responsive">
									<table class="table hr-table hr-att-table align-middle mb-0">
										<thead>
											<tr>
												<th>날짜</th>
												<th>출근</th>
												<th>퇴근</th>
												<th>상태</th>
												<th>근무 시간</th>
												<th>비고</th>
												<sec:authorize access="hasAuthority('PERMISSION_ATT_CORRECT_REQUEST')"><th class="text-end">정정</th></sec:authorize>
											</tr>
										</thead>
										<tbody>
											<c:forEach var="day" items="${att.daysDesc}">
												<tr class="${day.today ? 'is-today' : ''} ${day.dayStatus == 'HOLIDAY' || day.weekend ? 'is-off' : ''}">
													<td class="hr-name">
														${day.workDateText}
														<c:if test="${day.today}"><span class="hr-badge is-blue ms-1">오늘</span></c:if>
													</td>
													<td class="hr-time">${day.checkInTime}</td>
													<td class="hr-time">${day.checkOutTime}</td>
													<td><span class="hr-badge ${day.statusTone}">${day.statusName}</span></td>
													<td class="hr-time">
														${day.workTimeText}
														<c:if test="${day.overtimeMinutes > 0}"><span class="text-secondary small ms-1">(초과 ${day.overtimeText})</span></c:if>
													</td>
													<td class="text-secondary"><c:out value="${empty day.note ? '-' : day.note}" /></td>
													<sec:authorize access="hasAuthority('PERMISSION_ATT_CORRECT_REQUEST')">
														<td class="text-end">
															<c:if test="${day.correctable}">
																<%-- 근태 정정 신청(S-09) 화면은 아직 없어 data-soon --%>
																<a href="#" class="btn btn-sm btn-outline-secondary" data-soon>정정 신청</a>
															</c:if>
														</td>
													</sec:authorize>
												</tr>
											</c:forEach>
										</tbody>
									</table>
								</div>
							</c:otherwise>
						</c:choose>

						<p class="hr-help">
							<i class="bi bi-info-circle text-primary"></i> 근무 시간은 점심 1시간을 뺀 시간이고, 초과 근무는 18:00 이후를 30분 단위로 셉니다.
							출근 기록이 없는 평일은 결근으로 표시되며 주말 · 공휴일 · 승인된 휴가일은 제외됩니다.
							출퇴근 시각이 잘못 기록됐다면 정정 신청을 하세요. 결재가 승인되면 이 화면에 반영됩니다.
						</p>
					</div>

				</div>
			</div>
		</main>

	</div>

	<%@ include file="/WEB-INF/views/common/scripts.jsp"%>
</body>
</html>
