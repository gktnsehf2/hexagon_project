<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%-- S-10 근태 정정 신청: 근무일·정정 시각·사유 → 결재자 승인 시 근태에 반영 / 내 신청 내역·취소 --%>
<c:set var="menuGroup" value="att" />
<c:set var="menu" value="attCorrection" />
<!DOCTYPE html>
<html lang="ko">
<head>
<%@ include file="/WEB-INF/views/common/head.jsp"%>
<title>근태 정정 신청 | HexagonGMI 인사관리</title>
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
							<h2 class="hr-page-title">근태 정정 신청</h2>
							<p class="hr-page-sub">잘못 기록된 출퇴근 시각의 정정을 신청합니다. 결재자가 승인하면 근태에 반영됩니다.</p>
						</div>
						<div class="col-md-6">
							<ol class="breadcrumb justify-content-md-end mb-2">
								<li class="breadcrumb-item"><a href="${ctx}/dashboard">홈</a></li>
								<li class="breadcrumb-item">근태 관리</li>
								<li class="breadcrumb-item active" aria-current="page">근태 정정 신청</li>
							</ol>
						</div>
					</div>
				</div>
			</div>

			<div class="app-content">
				<div class="container-fluid">
					<div class="row g-3 mt-1">
						<!-- 신청 -->
						<div class="col-lg-8">
							<form id="corr-form" class="hr-card hr-form" novalidate>
								<div class="hr-section">
									<div class="hr-section-head">
										<div>
											<h3 class="hr-section-title">정정 내용</h3>
											<p class="hr-section-sub">바꿀 시각만 입력하세요. 비운 쪽은 지금 기록이 그대로 남습니다. <span class="hr-req">*</span> 필수</p>
										</div>
									</div>
									<div class="row g-3">
										<div class="col-md-4">
											<label for="workDate" class="form-label">근무일<span class="hr-req">*</span></label>
											<input type="date" id="workDate" name="workDate" class="form-control" value="${date}" min="${minDate}" max="${today}" required>
										</div>
										<div class="col-md-8">
											<span class="form-label d-block">현재 기록</span>
											<div class="hr-current" id="current">
												<c:choose>
													<c:when test="${empty date}"><span class="text-secondary">근무일을 고르면 그날 기록이 보입니다.</span></c:when>
													<c:when test="${record == null}"><span class="text-secondary">기록 없음 (출근 시각을 함께 입력하세요)</span></c:when>
													<c:otherwise>출근 <b>${record.checkInTime}</b> · 퇴근 <b>${record.checkOutTime}</b> <span class="text-secondary">(${record.statusName})</span></c:otherwise>
												</c:choose>
											</div>
										</div>
										<div class="col-md-4">
											<label for="checkIn" class="form-label">정정 출근 시각</label>
											<input type="time" id="checkIn" name="checkIn" class="form-control">
										</div>
										<div class="col-md-4">
											<label for="checkOut" class="form-label">정정 퇴근 시각</label>
											<input type="time" id="checkOut" name="checkOut" class="form-control">
										</div>
										<div class="col-md-4">
											<span class="form-label d-block">결재자</span>
											<div class="hr-current">
												<c:choose>
													<c:when test="${approver != null}"><c:out value="${approver.ename}" /> <span class="text-secondary"><c:out value="${approver.job.jobName}" /></span></c:when>
													<c:otherwise><span class="text-secondary">없음 (바로 반영)</span></c:otherwise>
												</c:choose>
											</div>
										</div>
										<div class="col-12">
											<label for="reason" class="form-label">정정 사유<span class="hr-req">*</span></label>
											<textarea id="reason" name="reason" class="form-control" rows="3" maxlength="200" placeholder="예) 출입 단말기 오류로 출근이 늦게 찍힘, 퇴근 체크 누락"></textarea>
											<div class="text-end text-secondary small mt-1"><span id="reason-len">0</span> / 200</div>
										</div>
									</div>
									<div class="alert alert-danger py-2 small mt-2 mb-0" id="form-error" hidden></div>
								</div>
								<div class="hr-form-foot">
									<span class="text-secondary small"><i class="bi bi-info-circle"></i> 승인되면 지각 · 조퇴가 정정된 시각으로 다시 판정됩니다.</span>
									<div class="d-flex gap-2 ms-auto">
										<a href="${ctx}/attendance/my" class="btn btn-outline-secondary">내 근태</a>
										<button type="submit" class="btn btn-primary" id="bt-submit">신청</button>
									</div>
								</div>
							</form>
						</div>

						<!-- 안내 -->
						<div class="col-lg-4">
							<div class="hr-card hr-section h-100">
								<div class="hr-section-head">
									<div>
										<h3 class="hr-section-title">정정 규칙</h3>
									</div>
								</div>
								<ul class="hr-rules">
									<li>오늘까지의 날짜만 신청할 수 있습니다. (근태 시스템 도입일 ${minDate} 이후)</li>
									<li>같은 날짜에는 결재 대기 중인 신청이 하나만 있을 수 있습니다.</li>
									<li>출근 기록이 없는 날(결근)은 출근 시각을 꼭 입력하세요.</li>
									<li>결재자는 소속 조직장이고, 본인이 조직장이면 상위 조직장입니다.</li>
									<li>대기 중인 신청은 아래 목록에서 취소할 수 있습니다.</li>
								</ul>
							</div>
						</div>

						<!-- 내 신청 내역 -->
						<div class="col-12">
							<div class="hr-card hr-section">
								<div class="hr-section-head">
									<div>
										<h3 class="hr-section-title">내 정정 신청 <span class="hr-count">(${fn:length(corrections)})</span></h3>
										<p class="hr-section-sub">최근 신청 순 · 최대 100건</p>
									</div>
								</div>
								<c:choose>
									<c:when test="${empty corrections}">
										<p class="hr-empty mb-0">정정 신청 내역이 없습니다.</p>
									</c:when>
									<c:otherwise>
										<div class="table-responsive">
											<table class="table hr-table align-middle mb-0">
												<thead>
													<tr>
														<th>신청번호</th>
														<th>근무일</th>
														<th>정정 요청</th>
														<th>현재 기록</th>
														<th>사유</th>
														<th>결재자</th>
														<th>신청일</th>
														<th>상태</th>
														<th class="text-end">관리</th>
													</tr>
												</thead>
												<tbody>
													<c:forEach var="c" items="${corrections}">
														<tr>
															<td class="hr-time text-secondary">${c.requestNo}</td>
															<td class="hr-time">${c.workDateText}</td>
															<td class="hr-time">출근 <b>${c.requestInTime}</b> · 퇴근 <b>${c.requestOutTime}</b></td>
															<td class="hr-time text-secondary">${c.currentInTime} ~ ${c.currentOutTime}</td>
															<td><c:out value="${c.reason}" /></td>
															<td>
																<c:choose>
																	<c:when test="${c.approver != null}"><c:out value="${c.approver.ename}" /> <span class="text-secondary small"><c:out value="${c.approver.job.jobName}" /></span></c:when>
																	<c:otherwise><span class="text-secondary">결재 없음</span></c:otherwise>
																</c:choose>
															</td>
															<td class="hr-time text-secondary">${c.requestDateText}</td>
															<td>
																<span class="hr-badge ${c.statusTone}">${c.statusName}</span>
																<c:if test="${not empty c.rejectReason}"><div class="hr-reject"><c:out value="${c.rejectReason}" /></div></c:if>
															</td>
															<td class="text-end">
																<c:if test="${c.cancelable}">
																	<button type="button" class="btn btn-sm btn-outline-danger bt-cancel" data-id="${c.attendanceCorrectionId}" data-no="${c.requestNo}">취소</button>
																</c:if>
															</td>
														</tr>
													</c:forEach>
												</tbody>
											</table>
										</div>
									</c:otherwise>
								</c:choose>
							</div>
						</div>
					</div>
				</div>
			</div>
		</main>

	</div>

	<!-- P-06 신청 완료 -->
	<div class="modal fade hr-modal" id="done-modal" tabindex="-1" aria-labelledby="done-title" aria-hidden="true" data-bs-backdrop="static">
		<div class="modal-dialog modal-dialog-centered" style="max-width: 400px">
			<div class="modal-content">
				<div class="modal-body p-4 text-center">
					<span class="hr-modal-icon is-ok"><i class="bi bi-check2-circle"></i></span>
					<h3 class="h5 fw-bold mt-3 mb-1" id="done-title">신청 완료</h3>
					<p class="text-secondary small mb-0" id="done-message"></p>
					<div class="d-flex gap-2 mt-4">
						<a href="${ctx}/attendance/my" class="btn btn-outline-secondary flex-fill">내 근태</a>
						<a href="${ctx}/attendance/corrections" class="btn btn-primary flex-fill">확인</a>
					</div>
				</div>
			</div>
		</div>
	</div>

	<%@ include file="/WEB-INF/views/common/scripts.jsp"%>
	<script>
		const ctx = "${ctx}";
		const form = document.getElementById("corr-form");
		const dateEl = document.getElementById("workDate");
		const reasonEl = document.getElementById("reason");
		const errorEl = document.getElementById("form-error");
		const btSubmit = document.getElementById("bt-submit");

		function showError(message) {
			errorEl.textContent = message || "";
			errorEl.hidden = !message;
		}

		// fetch 공통: 성공이면 결과 JSON, 실패면 안내 문구로 throw
		async function post(url, body) {
			const response = await fetch(url, { method: "POST", body: body });
			if (response.redirected) {
				location.href = response.url;
				throw new Error("");
			}
			let result;
			try {
				result = await response.json();
			} catch (e) {
				result = { message: response.status === 403 ? "권한이 없습니다." : "요청을 처리하지 못했습니다." };
			}
			if (!response.ok) {
				throw new Error(result.message);
			}
			return result;
		}

		// 근무일을 바꾸면 그날 현재 기록을 다시 읽음
		dateEl.addEventListener("change", async () => {
			const current = document.getElementById("current");
			if (!dateEl.value) {
				current.innerHTML = '<span class="text-secondary">근무일을 고르면 그날 기록이 보입니다.</span>';
				return;
			}
			try {
				const response = await fetch(ctx + "/api/attendance/corrections/record?date=" + dateEl.value);
				const body = await response.json();
				if (!response.ok) {
					throw new Error(body.message);
				}
				const r = body.data;
				current.textContent = "";
				if (!r.checkIn && !r.checkOut) {
					current.innerHTML = '<span class="text-secondary">기록 없음 (출근 시각을 함께 입력하세요)</span>';
				} else {
					current.append("출근 ");
					current.append(Object.assign(document.createElement("b"), { textContent: r.checkIn || "-" }));
					current.append(" · 퇴근 ");
					current.append(Object.assign(document.createElement("b"), { textContent: r.checkOut || "-" }));
					current.append(Object.assign(document.createElement("span"), { className: "text-secondary", textContent: " (" + r.statusName + ")" }));
				}
			} catch (e) {
				current.textContent = "기록을 불러오지 못했습니다.";
			}
		});
		reasonEl.addEventListener("input", () => document.getElementById("reason-len").textContent = reasonEl.value.length);

		form.addEventListener("submit", async event => {
			event.preventDefault();
			showError("");
			btSubmit.disabled = true;
			try {
				const result = await post(ctx + "/api/attendance/corrections", new URLSearchParams(new FormData(form)));
				document.getElementById("done-message").textContent = result.message;
				bootstrap.Modal.getOrCreateInstance(document.getElementById("done-modal")).show();
				return;
			} catch (e) {
				if (e.message) {
					showError(e.message);
				}
			}
			btSubmit.disabled = false;
		});

		// 대기 중인 신청 취소
		document.querySelectorAll(".bt-cancel").forEach(bt => bt.addEventListener("click", async () => {
			if (!confirm(bt.dataset.no + " 정정 신청을 취소할까요?")) {
				return;
			}
			bt.disabled = true;
			try {
				const result = await post(ctx + "/api/attendance/corrections/" + bt.dataset.id + "/cancel");
				hrToast(result.message, "success");
				setTimeout(() => location.reload(), 800);
			} catch (e) {
				if (e.message) {
					hrToast(e.message, "danger");
				}
				bt.disabled = false;
			}
		}));
	</script>
</body>
</html>
