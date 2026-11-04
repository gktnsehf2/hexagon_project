<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<c:set var="menu" value="dashboard" />
<c:set var="myToday" value="${dash.myToday}" />
<!DOCTYPE html>
<html lang="ko">
<head>
<%@ include file="/WEB-INF/views/common/head.jsp"%>
<title>메인 대시보드 | HexagonGMI 인사관리</title>
</head>

<body class="layout-fixed sidebar-expand-lg hr-app">
	<div class="app-wrapper">

		<%@ include file="/WEB-INF/views/common/header.jsp"%>
		<%@ include file="/WEB-INF/views/common/sidebar.jsp"%>

		<main class="app-main">
			<!-- 제목 + 시계·출퇴근 -->
			<div class="app-content-header">
				<div class="container-fluid">
					<div class="hr-dash-head">
						<div>
							<h2 class="hr-page-title">메인 대시보드</h2>
							<p class="hr-page-sub">오늘의 근무 현황과 주요 업무를 확인할 수 있습니다.</p>
						</div>

						<div class="hr-card hr-clock">
							<div>
								<div class="hr-clock-time" id="hr-clock">&nbsp;</div>
								<div class="hr-clock-state">
									<c:choose>
										<c:when test="${myToday.state == 'WORKING'}">
											<span class="hr-pill ${myToday.status == 'LATE' ? 'is-orange' : 'is-green'}">
												<i class="bi bi-circle-fill"></i>${myToday.status == 'LATE' ? '지각' : '근무 중'} · ${myToday.checkInTime} 출근
											</span>
										</c:when>
										<c:when test="${myToday.state == 'DONE'}">
											<span class="hr-pill ${myToday.status == 'EARLY_LEAVE' ? 'is-orange' : 'is-blue'}">
												<i class="bi bi-circle-fill"></i>${myToday.status == 'EARLY_LEAVE' ? '조퇴' : '퇴근'} · ${myToday.checkInTime} ~ ${myToday.checkOutTime}
											</span>
										</c:when>
										<c:when test="${myToday.state == 'LEAVE'}">
											<span class="hr-pill is-purple"><i class="bi bi-circle-fill"></i>휴가 (<c:out value="${myToday.leaveTypeName}" />)</span>
										</c:when>
										<c:otherwise>
											<span class="hr-pill is-gray"><i class="bi bi-circle-fill"></i>출근 전</span>
										</c:otherwise>
									</c:choose>
									<%-- 반차인 날은 기준 시각이 달라서 같이 보여줌 --%>
									<c:if test="${not empty myToday.leaveTypeName and myToday.state != 'LEAVE'}">
										<span class="hr-pill is-teal"><c:out value="${myToday.leaveTypeName}" /> · ${myToday.workStartText}~${myToday.workEndText}</span>
									</c:if>
								</div>
							</div>
							<sec:authorize access="hasAuthority('PERMISSION_ATT_CHECK')">
							<div class="hr-clock-actions">
								<button type="button" class="btn btn-primary" id="bt-check-in" ${myToday.canCheckIn ? '' : 'disabled'}>출근</button>
								<button type="button" class="btn btn-outline-primary" id="bt-check-out" ${myToday.canCheckOut ? '' : 'disabled'}>퇴근</button>
							</div>
							</sec:authorize>
						</div>
					</div>
				</div>
			</div>

			<div class="app-content">
				<div class="container-fluid">

					<!-- ==================== 통계 카드 4개 ==================== -->
					<div class="row g-3 mt-1">
						<c:choose>
							<%-- 근태 현황 권한이 있으면: 내 범위(팀/부서/전사)의 오늘 현황 --%>
							<c:when test="${dash.teamView}">
								<div class="col-sm-6 col-xl-3">
									<div class="hr-card hr-stat">
										<div class="hr-stat-icon is-blue"><i class="bi bi-people-fill"></i></div>
										<div>
											<div class="hr-stat-label">재직 인원</div>
											<div class="hr-stat-value">${dash.activeCount}<small>명</small></div>
											<div class="hr-stat-sub"><c:out value="${dash.scope.label}" /> 기준</div>
										</div>
									</div>
								</div>
								<div class="col-sm-6 col-xl-3">
									<div class="hr-card hr-stat">
										<div class="hr-stat-icon is-green"><i class="bi bi-person-check-fill"></i></div>
										<div>
											<div class="hr-stat-label">오늘 출근</div>
											<div class="hr-stat-value">${dash.checkedInCount}<small>명</small></div>
											<div class="hr-stat-sub">출근 완료</div>
										</div>
									</div>
								</div>
								<div class="col-sm-6 col-xl-3">
									<div class="hr-card hr-stat">
										<div class="hr-stat-icon is-orange"><i class="bi bi-alarm-fill"></i></div>
										<div>
											<div class="hr-stat-label">지각·결근</div>
											<div class="hr-stat-value">${dash.lateCount + dash.absentCount}<small>명</small></div>
											<div class="hr-stat-sub">지각 ${dash.lateCount}명 | 결근 ${dash.absentCount}명</div>
										</div>
									</div>
								</div>
								<div class="col-sm-6 col-xl-3">
									<div class="hr-card hr-stat">
										<div class="hr-stat-icon is-purple"><i class="bi bi-airplane-fill"></i></div>
										<div>
											<div class="hr-stat-label">오늘 휴가자</div>
											<div class="hr-stat-value">${fn:length(dash.todayLeaves)}<small>명</small></div>
											<div class="hr-stat-sub"><c:out value="${dash.leaveSummary}" /></div>
										</div>
									</div>
								</div>
							</c:when>

							<%-- 일반 사원: 내 현황 --%>
							<c:otherwise>
								<div class="col-sm-6 col-xl-3">
									<div class="hr-card hr-stat">
										<div class="hr-stat-icon is-blue"><i class="bi bi-calendar2-check-fill"></i></div>
										<div>
											<div class="hr-stat-label">이번 달 출근</div>
											<div class="hr-stat-value">${dash.myMonth.workDays}<small>일</small></div>
											<div class="hr-stat-sub">지각 ${dash.myMonth.lateCount}회 | 조퇴 ${dash.myMonth.earlyLeaveCount}회</div>
										</div>
									</div>
								</div>
								<div class="col-sm-6 col-xl-3">
									<div class="hr-card hr-stat">
										<div class="hr-stat-icon is-purple"><i class="bi bi-airplane-fill"></i></div>
										<div>
											<div class="hr-stat-label">잔여 연차</div>
											<c:choose>
												<c:when test="${dash.annualLeave != null}">
													<div class="hr-stat-value"><fmt:formatNumber value="${dash.annualLeave.remainDays}" pattern="0.#" /><small>일</small></div>
													<fmt:formatNumber var="grantedDays" value="${dash.annualLeave.grantedDays}" pattern="0.#" />
													<fmt:formatNumber var="usedDays" value="${dash.annualLeave.usedDays}" pattern="0.#" />
													<div class="hr-stat-sub">부여 ${grantedDays}일 | 사용 ${usedDays}일</div>
												</c:when>
												<c:otherwise>
													<div class="hr-stat-value">-</div>
													<div class="hr-stat-sub">올해 부여된 연차가 없습니다</div>
												</c:otherwise>
											</c:choose>
										</div>
									</div>
								</div>
								<div class="col-sm-6 col-xl-3">
									<div class="hr-card hr-stat">
										<div class="hr-stat-icon is-orange"><i class="bi bi-hourglass-split"></i></div>
										<div>
											<div class="hr-stat-label">이번 달 초과근무</div>
											<div class="hr-stat-value"><fmt:formatNumber value="${dash.myOvertimeHours}" pattern="0.#" /><small>시간</small></div>
											<div class="hr-stat-sub">18:00 이후 근무 시간</div>
										</div>
									</div>
								</div>
								<div class="col-sm-6 col-xl-3">
									<div class="hr-card hr-stat">
										<div class="hr-stat-icon is-green"><i class="bi bi-send-fill"></i></div>
										<div>
											<div class="hr-stat-label">${dash.approver ? '내 결재 대기' : '진행 중인 신청'}</div>
											<div class="hr-stat-value">${fn:length(dash.requests)}<small>건</small></div>
											<div class="hr-stat-sub">휴가·근태 정정</div>
										</div>
									</div>
								</div>
							</c:otherwise>
						</c:choose>
					</div>

					<!-- ==================== 목록 카드 3개 ==================== -->
					<div class="row g-3 mt-1">
						<c:choose>
							<c:when test="${dash.teamView}">
								<!-- 오늘 휴가자 -->
								<div class="col-lg-4">
									<div class="hr-card hr-list">
										<div class="hr-list-head">
											<h3>오늘 휴가자 <span class="hr-count">(${fn:length(dash.todayLeaves)})</span></h3>
											<a href="${ctx}/calendar" class="hr-more">전체 보기 <i class="bi bi-chevron-right"></i></a>
										</div>
										<c:choose>
											<c:when test="${empty dash.todayLeaves}">
												<div class="hr-empty">오늘 휴가자가 없습니다.</div>
											</c:when>
											<c:otherwise>
												<table class="table hr-table">
													<thead><tr><th>이름</th><th>부서</th><th>유형</th></tr></thead>
													<tbody>
														<c:forEach var="leave" items="${dash.todayLeaves}" end="4">
															<c:set var="tone" value="${leave.leaveTypeCode == 'ANNUAL' ? 'is-blue' : (leave.leaveTypeCode == 'SICK' ? 'is-red' : (fn:startsWith(leave.leaveTypeCode, 'HALF') ? 'is-teal' : 'is-purple'))}" />
															<tr>
																<td class="hr-name"><c:out value="${leave.ename}" /></td>
																<td><c:out value="${leave.departmentName}" /></td>
																<td><span class="hr-badge ${tone}"><c:out value="${leave.leaveTypeName}" /></span></td>
															</tr>
														</c:forEach>
													</tbody>
												</table>
												<c:if test="${fn:length(dash.todayLeaves) > 5}">
													<div class="hr-list-more">외 ${fn:length(dash.todayLeaves) - 5}명</div>
												</c:if>
											</c:otherwise>
										</c:choose>
									</div>
								</div>

								<!-- 근태 이상자 -->
								<div class="col-lg-4">
									<div class="hr-card hr-list">
										<div class="hr-list-head">
											<h3>근태 이상자 (지각·결근) <span class="hr-count">(${fn:length(dash.issues)})</span></h3>
											<a href="${ctx}/attendance/status" class="hr-more">전체 보기 <i class="bi bi-chevron-right"></i></a>
										</div>
										<c:choose>
											<c:when test="${empty dash.issues}">
												<div class="hr-empty">지각·결근자가 없습니다.</div>
											</c:when>
											<c:otherwise>
												<table class="table hr-table">
													<thead><tr><th>이름</th><th>부서</th><th>구분</th></tr></thead>
													<tbody>
														<c:forEach var="issue" items="${dash.issues}" end="4">
															<tr>
																<td class="hr-name"><c:out value="${issue.ename}" /></td>
																<td><c:out value="${issue.departmentName}" /></td>
																<td><span class="hr-badge ${issue.issueType == 'LATE' ? 'is-orange' : 'is-red'}">${issue.issueText}</span></td>
															</tr>
														</c:forEach>
													</tbody>
												</table>
												<c:if test="${fn:length(dash.issues) > 5}">
													<div class="hr-list-more">외 ${fn:length(dash.issues) - 5}명</div>
												</c:if>
											</c:otherwise>
										</c:choose>
									</div>
								</div>
							</c:when>

							<c:otherwise>
								<!-- 내 최근 근태 -->
								<div class="col-lg-4">
									<div class="hr-card hr-list">
										<div class="hr-list-head">
											<h3>내 최근 근태</h3>
											<a href="${ctx}/attendance/my" class="hr-more">전체 보기 <i class="bi bi-chevron-right"></i></a>
										</div>
										<c:choose>
											<c:when test="${empty dash.myAttendances}">
												<div class="hr-empty">근태 기록이 없습니다.</div>
											</c:when>
											<c:otherwise>
												<table class="table hr-table">
													<thead><tr><th>일자</th><th>출근</th><th>퇴근</th><th>상태</th></tr></thead>
													<tbody>
														<c:forEach var="att" items="${dash.myAttendances}">
															<c:set var="tone" value="${att.status == 'NORMAL' ? 'is-green' : (att.status == 'LATE' or att.status == 'EARLY_LEAVE' ? 'is-orange' : (att.status == 'ON_LEAVE' ? 'is-purple' : 'is-red'))}" />
															<tr>
																<td class="hr-name">${att.workDateText}</td>
																<td>${att.checkInTime}</td>
																<td>${att.checkOutTime}</td>
																<td><span class="hr-badge ${tone}">${att.statusName}</span></td>
															</tr>
														</c:forEach>
													</tbody>
												</table>
											</c:otherwise>
										</c:choose>
									</div>
								</div>

								<!-- 예정된 내 휴가 -->
								<div class="col-lg-4">
									<div class="hr-card hr-list">
										<div class="hr-list-head">
											<h3>예정된 내 휴가 <span class="hr-count">(${fn:length(dash.myLeaves)})</span></h3>
											<a href="${ctx}/leaves/my" class="hr-more">전체 보기 <i class="bi bi-chevron-right"></i></a>
										</div>
										<c:choose>
											<c:when test="${empty dash.myLeaves}">
												<div class="hr-empty">예정된 휴가가 없습니다.</div>
											</c:when>
											<c:otherwise>
												<table class="table hr-table">
													<thead><tr><th>기간</th><th>유형</th><th>상태</th></tr></thead>
													<tbody>
														<c:forEach var="leave" items="${dash.myLeaves}">
															<tr>
																<td class="hr-name">${leave.dateText}</td>
																<td><c:out value="${leave.detail}" /></td>
																<td><span class="hr-badge is-green">${leave.statusName}</span></td>
															</tr>
														</c:forEach>
													</tbody>
												</table>
											</c:otherwise>
										</c:choose>
									</div>
								</div>
							</c:otherwise>
						</c:choose>

						<!-- 결재 권한이 있으면 내 결재 대기, 없으면 진행 중인 내 신청 -->
						<div class="col-lg-4">
							<div class="hr-card hr-list">
								<div class="hr-list-head">
									<h3>${dash.approver ? '내 결재 대기' : '진행 중인 내 신청'} <span class="hr-count">(${fn:length(dash.requests)})</span></h3>
									<a href="${ctx}${dash.approver ? '/leaves/approvals' : '/leaves/my'}" class="hr-more">전체 보기 <i class="bi bi-chevron-right"></i></a>
								</div>
								<c:choose>
									<c:when test="${empty dash.requests}">
										<div class="hr-empty">${dash.approver ? '결재할 문서가 없습니다.' : '진행 중인 신청이 없습니다.'}</div>
									</c:when>
									<c:otherwise>
										<table class="table hr-table">
											<thead>
												<tr>
													<th>${dash.approver ? '신청자' : '구분'}</th>
													<th>${dash.approver ? '구분' : '내용'}</th>
													<th>${dash.approver ? '일자' : '일자 / 상태'}</th>
												</tr>
											</thead>
											<tbody>
												<c:forEach var="item" items="${dash.requests}" end="4">
													<c:set var="tone" value="${item.approvalType == 'LEAVE' ? 'is-blue' : 'is-orange'}" />
													<tr>
														<c:choose>
															<c:when test="${dash.approver}">
																<td class="hr-name"><c:out value="${item.ename}" /></td>
																<td><span class="hr-badge ${tone}" title="<c:out value='${item.detail}'/>">${item.typeName}</span></td>
																<td>${item.dateText}</td>
															</c:when>
															<c:otherwise>
																<td><span class="hr-badge ${tone}">${item.typeName}</span></td>
																<td><c:out value="${item.detail}" /></td>
																<td>${item.dateText} <span class="text-secondary small">· ${item.statusName}</span></td>
															</c:otherwise>
														</c:choose>
													</tr>
												</c:forEach>
											</tbody>
										</table>
										<c:if test="${fn:length(dash.requests) > 5}">
											<div class="hr-list-more">외 ${fn:length(dash.requests) - 5}건</div>
										</c:if>
									</c:otherwise>
								</c:choose>
							</div>
						</div>
					</div>

					<!-- 하단 안내 -->
					<div class="hr-card hr-foot mt-3 mb-2">
						<span><i class="bi bi-info-circle"></i>근태·휴가 현황은 보유 권한에 따라 팀 / 부서 / 전사 범위로 표시됩니다.</span>
						<a href="${ctx}/calendar" class="btn btn-outline-secondary btn-sm"><i class="bi bi-calendar3 me-1"></i>휴가 캘린더 보기</a>
					</div>

				</div>
			</div>
		</main>

	</div>

	<%@ include file="/WEB-INF/views/common/scripts.jsp"%>
	<script>
		/*
		 시계: 서버 시각 기준으로 1초마다 갱신 (PC 시계가 틀려도 출퇴근 판단 기준과 같은 시각이 보이도록)
		 출근/퇴근: fetch 로 POST → RestAttendanceController → 결과 JSON
		 주의: JSP 안이라 자바스크립트 백틱 템플릿 문자열을 쓰지 않는다
		*/
		const ctx = "${ctx}";
		const serverOffset = ${dash.serverTime} - Date.now();
		const workEnd = "${myToday.workEndText}";
		const weekdays = ["일", "월", "화", "수", "목", "금", "토"];
		const clockBox = document.getElementById("hr-clock");

		function pad(n) {
			return String(n).padStart(2, "0");
		}

		// 한국 시간 = UTC + 9시간 (브라우저 시간대 설정과 상관없이 계산하려고 getUTC* 사용)
		function koreaNow() {
			return new Date(Date.now() + serverOffset + 9 * 60 * 60 * 1000);
		}

		function renderClock() {
			const d = koreaNow();
			clockBox.textContent = d.getUTCFullYear() + "." + pad(d.getUTCMonth() + 1) + "." + pad(d.getUTCDate())
				+ " (" + weekdays[d.getUTCDay()] + ") "
				+ pad(d.getUTCHours()) + ":" + pad(d.getUTCMinutes()) + ":" + pad(d.getUTCSeconds());
		}
		renderClock();
		setInterval(renderClock, 1000);

		async function attend(action, button) {
			button.disabled = true;
			try {
				const response = await fetch(ctx + "/api/attendance/" + action, { method: "POST" });
				if (response.redirected) { // 세션이 끝나 로그인 화면으로 보내진 경우
					location.href = response.url;
					return;
				}
				let result;
				try {
					result = await response.json();
				} catch (e) {
					result = { message: response.status === 403 ? "출퇴근 기록 권한이 없습니다." : "요청을 처리하지 못했습니다." };
				}
				if (response.ok) {
					hrToast(result.message, "success");
					setTimeout(() => location.reload(), 800);
					return;
				}
				hrToast(result.message, "danger");
			} catch (error) {
				console.log("출퇴근 요청 실패", error);
				hrToast("서버와 통신할 수 없습니다. 잠시 후 다시 시도해 주세요.", "danger");
			}
			button.disabled = false;
		}

		const btCheckIn = document.getElementById("bt-check-in");
		const btCheckOut = document.getElementById("bt-check-out");
		if (btCheckIn) {
			btCheckIn.addEventListener("click", () => attend("check-in", btCheckIn));
		}
		if (btCheckOut) {
			btCheckOut.addEventListener("click", () => {
				const d = koreaNow();
				const now = pad(d.getUTCHours()) + ":" + pad(d.getUTCMinutes());
				if (now < workEnd && !confirm("퇴근 기준 시각(" + workEnd + ") 전입니다. 조퇴로 기록될 수 있습니다.\n퇴근할까요?")) {
					return;
				}
				attend("check-out", btCheckOut);
			});
		}
	</script>
</body>
</html>
