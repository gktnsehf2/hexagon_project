<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%-- S-14 내 휴가 내역: 연차 현황 + 휴가 신청 내역, 대기 중인 신청은 본인이 취소 --%>
<c:set var="menuGroup" value="leave" />
<c:set var="menu" value="myLeave" />
<!DOCTYPE html>
<html lang="ko">
<head>
<%@ include file="/WEB-INF/views/common/head.jsp"%>
<title>내 휴가 내역 | HexagonGMI 인사관리</title>
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
							<h2 class="hr-page-title">내 휴가 내역</h2>
							<p class="hr-page-sub">연차 현황과 휴가 신청 내역을 확인하고, 결재 대기 중인 신청을 취소합니다.</p>
						</div>
						<div class="col-md-6">
							<ol class="breadcrumb justify-content-md-end mb-2">
								<li class="breadcrumb-item"><a href="${ctx}/dashboard">홈</a></li>
								<li class="breadcrumb-item">휴가 관리</li>
								<li class="breadcrumb-item active" aria-current="page">내 휴가 내역</li>
							</ol>
						</div>
					</div>
				</div>
			</div>

			<div class="app-content">
				<div class="container-fluid">

					<!-- 연차 현황 -->
					<div class="row g-3 mt-1">
						<div class="col-md-4">
							<div class="hr-card hr-stat">
								<div class="hr-stat-icon is-blue"><i class="bi bi-calendar-plus"></i></div>
								<div>
									<div class="hr-stat-label">부여 연차</div>
									<div class="hr-stat-value">
										<c:choose>
											<c:when test="${annualLeave != null}"><fmt:formatNumber value="${annualLeave.grantedDays}" pattern="0.#" /><small>일</small></c:when>
											<c:otherwise>-</c:otherwise>
										</c:choose>
									</div>
									<div class="hr-stat-sub">${year}년 부여</div>
								</div>
							</div>
						</div>
						<div class="col-md-4">
							<div class="hr-card hr-stat">
								<div class="hr-stat-icon is-teal"><i class="bi bi-calendar-check"></i></div>
								<div>
									<div class="hr-stat-label">사용 연차</div>
									<div class="hr-stat-value">
										<c:choose>
											<c:when test="${annualLeave != null}"><fmt:formatNumber value="${annualLeave.usedDays}" pattern="0.#" /><small>일</small></c:when>
											<c:otherwise>-</c:otherwise>
										</c:choose>
									</div>
									<div class="hr-stat-sub">승인된 연차 · 반차 (병가 · 경조사 · 공가는 차감 안 함)</div>
								</div>
							</div>
						</div>
						<div class="col-md-4">
							<div class="hr-card hr-stat">
								<div class="hr-stat-icon is-green"><i class="bi bi-calendar-heart"></i></div>
								<div>
									<div class="hr-stat-label">잔여 연차</div>
									<div class="hr-stat-value">
										<c:choose>
											<c:when test="${annualLeave != null}"><fmt:formatNumber value="${annualLeave.remainDays}" pattern="0.#" /><small>일</small></c:when>
											<c:otherwise>-</c:otherwise>
										</c:choose>
									</div>
									<div class="hr-stat-sub">
										<c:choose>
											<c:when test="${annualLeave == null}">${year}년 연차 부여 내역이 없습니다</c:when>
											<c:when test="${pendingDays > 0}">결재 대기 <fmt:formatNumber value="${pendingDays}" pattern="0.#" />일이 승인되면 <fmt:formatNumber value="${annualLeave.remainDays - pendingDays}" pattern="0.#" />일</c:when>
											<c:otherwise>지금 사용할 수 있는 일수</c:otherwise>
										</c:choose>
									</div>
								</div>
							</div>
						</div>
					</div>

					<!-- 신청 내역 -->
					<div class="hr-card hr-section mt-3">
						<div class="hr-section-head flex-wrap">
							<div>
								<h3 class="hr-section-title">휴가 신청 내역 <span class="hr-count">(${totalCount})</span></h3>
								<p class="hr-section-sub">${year}년에 걸친 신청 · 최근 신청 순</p>
							</div>
							<div class="d-flex gap-2 align-items-center">
								<form action="${ctx}/leaves/my" method="get">
									<input type="hidden" name="status" value="<c:out value='${status}' />">
									<select name="year" class="form-select form-select-sm" aria-label="조회 연도" onchange="this.form.submit()">
										<c:forEach var="y" items="${years}">
											<option value="${y}" ${y == year ? 'selected' : ''}>${y}년</option>
										</c:forEach>
									</select>
								</form>
								<sec:authorize access="hasAuthority('PERMISSION_LEAVE_REQUEST')">
									<%-- 휴가 신청(S-13) 화면은 아직 없어 data-soon --%>
									<a href="#" class="btn btn-sm btn-primary text-nowrap" data-soon><i class="bi bi-plus-lg"></i> 휴가 신청</a>
								</sec:authorize>
							</div>
						</div>

						<!-- 상태 필터 -->
						<nav class="hr-tabs" aria-label="상태 필터">
							<a href="${ctx}/leaves/my?year=${year}" class="${empty status ? 'active' : ''}">전체<b>${totalCount}</b></a>
							<c:forEach var="entry" items="${statusCounts}">
								<c:choose>
									<c:when test="${entry.key == 'REQUESTED'}"><c:set var="statusName" value="대기" /></c:when>
									<c:when test="${entry.key == 'APPROVED'}"><c:set var="statusName" value="승인" /></c:when>
									<c:when test="${entry.key == 'REJECTED'}"><c:set var="statusName" value="반려" /></c:when>
									<c:otherwise><c:set var="statusName" value="취소" /></c:otherwise>
								</c:choose>
								<a href="${ctx}/leaves/my?year=${year}&amp;status=${entry.key}" class="${status == entry.key ? 'active' : ''}">${statusName}<b>${entry.value}</b></a>
							</c:forEach>
						</nav>

						<c:choose>
							<c:when test="${empty leaves}">
								<p class="hr-empty mb-0">${empty status ? '휴가 신청 내역이 없습니다.' : '해당 상태의 신청이 없습니다.'}</p>
							</c:when>
							<c:otherwise>
								<div class="table-responsive">
									<table class="table hr-table align-middle mb-0">
										<thead>
											<tr>
												<th>신청번호</th>
												<th>휴가 유형</th>
												<th>사용 기간</th>
												<th class="text-end">일수</th>
												<th>사유</th>
												<th>결재자</th>
												<th>신청일</th>
												<th>상태</th>
												<th class="text-end">관리</th>
											</tr>
										</thead>
										<tbody>
											<c:forEach var="leave" items="${leaves}">
												<tr>
													<td class="hr-time text-secondary">${leave.requestNo}</td>
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
														<c:if test="${not empty leave.rejectReason}">
															<div class="hr-reject"><c:out value="${leave.rejectReason}" /></div>
														</c:if>
													</td>
													<td class="text-end">
														<c:if test="${leave.cancelable}">
															<sec:authorize access="hasAuthority('PERMISSION_LEAVE_CANCEL')">
																<button type="button" class="btn btn-sm btn-outline-danger bt-cancel"
																		data-id="${leave.leaveRequestId}" data-no="${leave.requestNo}"
																		data-desc="<c:out value='${leave.leaveType.leaveTypeName}' /> · ${leave.periodText} (${leave.daysText}일)">취소</button>
															</sec:authorize>
														</c:if>
													</td>
												</tr>
											</c:forEach>
										</tbody>
									</table>
								</div>
							</c:otherwise>
						</c:choose>

						<p class="hr-help">
							<i class="bi bi-info-circle text-primary"></i> 휴가는 소속 부서 조직장이 한 번에 결재합니다. 결재 대기 중인 신청만 직접 취소할 수 있고,
							승인된 휴가를 취소하려면 결재자에게 문의하세요. 연차 · 반차는 승인될 때 사용 연차에 더해집니다.
						</p>
					</div>

				</div>
			</div>
		</main>

	</div>

	<!-- 휴가 취소 확인 팝업 -->
	<div class="modal fade hr-modal" id="cancel-modal" tabindex="-1" aria-labelledby="cancel-title" aria-hidden="true">
		<div class="modal-dialog modal-dialog-centered" style="max-width: 420px">
			<div class="modal-content">
				<div class="modal-body p-4">
					<button type="button" class="btn-close float-end" data-bs-dismiss="modal" aria-label="닫기"></button>
					<div class="text-center">
						<span class="hr-modal-icon"><i class="bi bi-calendar-x"></i></span>
						<h3 class="h5 fw-bold mt-3 mb-1" id="cancel-title">휴가 신청 취소</h3>
						<p class="text-secondary small mb-0"><b id="cancel-no"></b><br><span id="cancel-desc"></span></p>
					</div>
					<div class="hr-modal-warn mt-3">
						취소한 신청은 되돌릴 수 없습니다. 다시 쉬려면 휴가를 새로 신청해야 합니다.
					</div>
					<div class="d-flex gap-2 mt-4">
						<button type="button" class="btn btn-outline-secondary flex-fill" data-bs-dismiss="modal">닫기</button>
						<button type="button" class="btn btn-danger flex-fill" id="bt-cancel-ok">신청 취소</button>
					</div>
				</div>
			</div>
		</div>
	</div>

	<%@ include file="/WEB-INF/views/common/scripts.jsp"%>
	<script>
		const ctx = "${ctx}";
		const cancelModalEl = document.getElementById("cancel-modal");
		const btCancelOk = document.getElementById("bt-cancel-ok");
		let cancelId = null;

		// 목록의 취소 버튼 → 확인 팝업
		document.querySelectorAll(".bt-cancel").forEach(bt => {
			bt.addEventListener("click", () => {
				cancelId = bt.dataset.id;
				document.getElementById("cancel-no").textContent = bt.dataset.no;
				document.getElementById("cancel-desc").textContent = bt.dataset.desc;
				bootstrap.Modal.getOrCreateInstance(cancelModalEl).show();
			});
		});

		// 확인 → fetch 로 POST → RestLeaveController → 결과 JSON
		btCancelOk.addEventListener("click", async () => {
			if (!cancelId) {
				return;
			}
			btCancelOk.disabled = true;
			try {
				const response = await fetch(ctx + "/api/leaves/" + cancelId + "/cancel", { method: "POST" });
				if (response.redirected) { // 세션이 끝나 로그인 화면으로 보내진 경우
					location.href = response.url;
					return;
				}
				let result;
				try {
					result = await response.json();
				} catch (e) {
					result = { message: response.status === 403 ? "휴가 취소 권한이 없습니다." : "요청을 처리하지 못했습니다." };
				}
				if (response.ok) {
					bootstrap.Modal.getInstance(cancelModalEl).hide();
					hrToast(result.message, "success");
					setTimeout(() => location.reload(), 900);
					return;
				}
				hrToast(result.message, "danger");
			} catch (error) {
				console.log("휴가 취소 요청 실패", error);
				hrToast("서버와 통신할 수 없습니다. 잠시 후 다시 시도해 주세요.", "danger");
			}
			btCancelOk.disabled = false;
		});
	</script>
</body>
</html>
