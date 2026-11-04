<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%-- S-10 근태 결재: 나에게 온 정정 신청 승인(→ 근태 반영) · 반려(P-03 사유), 처리 완료 내역 --%>
<c:set var="menuGroup" value="att" />
<c:set var="menu" value="attApproval" />
<!DOCTYPE html>
<html lang="ko">
<head>
<%@ include file="/WEB-INF/views/common/head.jsp"%>
<title>근태 결재 | HexagonGMI 인사관리</title>
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
							<h2 class="hr-page-title">근태 결재</h2>
							<p class="hr-page-sub">나에게 온 근태 정정 신청을 승인하거나 반려합니다.</p>
						</div>
						<div class="col-md-6">
							<ol class="breadcrumb justify-content-md-end mb-2">
								<li class="breadcrumb-item"><a href="${ctx}/dashboard">홈</a></li>
								<li class="breadcrumb-item">근태 관리</li>
								<li class="breadcrumb-item active" aria-current="page">근태 결재</li>
							</ol>
						</div>
					</div>
				</div>
			</div>

			<div class="app-content">
				<div class="container-fluid">
					<div class="hr-card hr-section mt-1">
						<div class="hr-section-head">
							<div>
								<h3 class="hr-section-title">${tab == 'done' ? '처리 완료' : '결재 대기'} <span class="hr-count">(${fn:length(corrections)})</span></h3>
								<p class="hr-section-sub">${tab == 'done' ? '내가 승인 · 반려한 정정 (최근 100건)' : '근무일이 오래된 순'}</p>
							</div>
						</div>

						<nav class="hr-tabs" aria-label="결재 구분">
							<a href="${ctx}/attendance/corrections/approvals" class="${tab == 'pending' ? 'active' : ''}">결재 대기<b>${pendingCount}</b></a>
							<a href="${ctx}/attendance/corrections/approvals?tab=done" class="${tab == 'done' ? 'active' : ''}">처리 완료<b>${doneCount}</b></a>
						</nav>

						<c:choose>
							<c:when test="${empty corrections}">
								<p class="hr-empty mb-0">${tab == 'done' ? '처리한 정정 신청이 없습니다.' : '결재할 정정 신청이 없습니다.'}</p>
							</c:when>
							<c:otherwise>
								<div class="table-responsive">
									<table class="table hr-table align-middle mb-0">
										<thead>
											<tr>
												<th>신청번호</th>
												<th>신청자</th>
												<th>근무일</th>
												<th>${tab == 'done' ? '현재 기록' : '현재 기록 → 정정 요청'}</th>
												<th>사유</th>
												<th>신청일</th>
												<c:choose>
													<c:when test="${tab == 'pending'}"><th class="text-end">처리</th></c:when>
													<c:otherwise><th>결과</th><th>처리일</th></c:otherwise>
												</c:choose>
											</tr>
										</thead>
										<tbody>
											<c:forEach var="c" items="${corrections}">
												<tr>
													<td class="hr-time text-secondary">${c.requestNo}</td>
													<td>
														<span class="hr-name"><c:out value="${c.emp.ename}" /></span>
														<div class="text-secondary small"><c:out value="${c.emp.department.departmentName}" /> · <c:out value="${c.emp.job.jobName}" /></div>
													</td>
													<td class="hr-time">${c.workDateText}</td>
													<td class="hr-time">
														<c:choose>
															<c:when test="${tab == 'pending'}">
																<span class="text-secondary">${c.currentInTime} ~ ${c.currentOutTime}</span>
																<i class="bi bi-arrow-right mx-1"></i>
																<b>${c.requestCheckInAt != null ? c.requestInTime : c.currentInTime} ~ ${c.requestCheckOutAt != null ? c.requestOutTime : c.currentOutTime}</b>
															</c:when>
															<c:otherwise>${c.currentInTime} ~ ${c.currentOutTime}</c:otherwise>
														</c:choose>
													</td>
													<td><c:out value="${c.reason}" /></td>
													<td class="hr-time text-secondary">${c.requestDateText}</td>
													<c:choose>
														<c:when test="${tab == 'pending'}">
															<td class="text-end text-nowrap">
																<button type="button" class="btn btn-sm btn-primary bt-approve" data-id="${c.attendanceCorrectionId}" data-no="${c.requestNo}">승인</button>
																<button type="button" class="btn btn-sm btn-outline-danger bt-reject" data-id="${c.attendanceCorrectionId}" data-no="${c.requestNo}"
																		data-desc="<c:out value='${c.emp.ename}' /> · ${c.workDateText}">반려</button>
															</td>
														</c:when>
														<c:otherwise>
															<td>
																<span class="hr-badge ${c.statusTone}">${c.statusName}</span>
																<c:if test="${not empty c.rejectReason}"><div class="hr-reject"><c:out value="${c.rejectReason}" /></div></c:if>
															</td>
															<td class="hr-time text-secondary">${c.approvedAtText}</td>
														</c:otherwise>
													</c:choose>
												</tr>
											</c:forEach>
										</tbody>
									</table>
								</div>
							</c:otherwise>
						</c:choose>

						<p class="hr-help">
							<i class="bi bi-info-circle text-primary"></i> 승인하면 요청한 시각이 그날 근태에 바로 반영되고, 지각 · 조퇴가 다시 판정됩니다(비운 쪽은 기존 기록 유지).
							처리한 뒤에는 되돌릴 수 없습니다.
						</p>
					</div>
				</div>
			</div>
		</main>

	</div>

	<!-- P-03 반려 사유 입력 -->
	<div class="modal fade hr-modal" id="reject-modal" tabindex="-1" aria-labelledby="reject-title" aria-hidden="true">
		<div class="modal-dialog modal-dialog-centered" style="max-width: 440px">
			<div class="modal-content">
				<div class="modal-body p-4">
					<button type="button" class="btn-close float-end" data-bs-dismiss="modal" aria-label="닫기"></button>
					<div class="text-center">
						<span class="hr-modal-icon"><i class="bi bi-x-circle"></i></span>
						<h3 class="h5 fw-bold mt-3 mb-1" id="reject-title">반려 사유 입력</h3>
						<p class="text-secondary small"><b id="reject-no"></b><br><span id="reject-desc"></span></p>
					</div>
					<label for="reject-reason" class="form-label fw-semibold small">반려 사유 <span class="text-danger">*</span></label>
					<textarea id="reject-reason" class="form-control" rows="3" maxlength="200" placeholder="신청자에게 보여집니다. 예) 출입 기록과 맞지 않습니다."></textarea>
					<div class="d-flex gap-2 mt-3">
						<button type="button" class="btn btn-outline-secondary flex-fill" data-bs-dismiss="modal">닫기</button>
						<button type="button" class="btn btn-danger flex-fill" id="bt-reject-ok">반려</button>
					</div>
				</div>
			</div>
		</div>
	</div>

	<%@ include file="/WEB-INF/views/common/scripts.jsp"%>
	<script>
		const ctx = "${ctx}";
		const rejectModalEl = document.getElementById("reject-modal");
		const reasonEl = document.getElementById("reject-reason");
		let targetId = null;

		async function send(button, url, body) {
			button.disabled = true;
			try {
				const response = await fetch(url, { method: "POST", body: body });
				if (response.redirected) {
					location.href = response.url;
					return;
				}
				let result;
				try {
					result = await response.json();
				} catch (e) {
					result = { message: response.status === 403 ? "근태 결재 권한이 없습니다." : "요청을 처리하지 못했습니다." };
				}
				if (response.ok) {
					const modal = bootstrap.Modal.getInstance(rejectModalEl);
					if (modal) {
						modal.hide();
					}
					hrToast(result.message, "success");
					setTimeout(() => location.reload(), 1200);
					return;
				}
				hrToast(result.message, "danger");
			} catch (e) {
				console.log("근태 결재 요청 실패", e);
				hrToast("서버와 통신할 수 없습니다. 잠시 후 다시 시도해 주세요.", "danger");
			}
			button.disabled = false;
		}

		document.querySelectorAll(".bt-approve").forEach(bt => bt.addEventListener("click", () => {
			if (confirm(bt.dataset.no + " 정정을 승인할까요? 그날 근태에 바로 반영됩니다.")) {
				send(bt, ctx + "/api/attendance/corrections/" + bt.dataset.id + "/approve");
			}
		}));

		document.querySelectorAll(".bt-reject").forEach(bt => bt.addEventListener("click", () => {
			targetId = bt.dataset.id;
			document.getElementById("reject-no").textContent = bt.dataset.no;
			document.getElementById("reject-desc").textContent = bt.dataset.desc;
			reasonEl.value = "";
			bootstrap.Modal.getOrCreateInstance(rejectModalEl).show();
		}));
		rejectModalEl.addEventListener("shown.bs.modal", () => reasonEl.focus());

		const btRejectOk = document.getElementById("bt-reject-ok");
		btRejectOk.addEventListener("click", () => {
			const reason = reasonEl.value.trim();
			if (!reason) {
				hrToast("반려 사유를 입력하세요.", "danger");
				reasonEl.focus();
				return;
			}
			send(btRejectOk, ctx + "/api/attendance/corrections/" + targetId + "/reject", new URLSearchParams({ rejectReason: reason }));
		});
	</script>
</body>
</html>
