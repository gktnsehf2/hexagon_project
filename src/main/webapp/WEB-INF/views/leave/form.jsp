<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%-- S-13 휴가 신청: 유형·기간·사유 → 사용 일수·잔여 연차·결재자 미리보기(/api/leaves/plan) → 신청(/api/leaves) → P-06 신청 완료 --%>
<c:set var="menuGroup" value="leave" />
<c:set var="menu" value="leaveNew" />
<!DOCTYPE html>
<html lang="ko">
<head>
<%@ include file="/WEB-INF/views/common/head.jsp"%>
<title>휴가 신청 | HexagonGMI 인사관리</title>
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
							<h2 class="hr-page-title">휴가 신청</h2>
							<p class="hr-page-sub">휴가 유형과 기간을 고르면 사용 일수와 결재자가 자동으로 정해집니다.</p>
						</div>
						<div class="col-md-6">
							<ol class="breadcrumb justify-content-md-end mb-2">
								<li class="breadcrumb-item"><a href="${ctx}/dashboard">홈</a></li>
								<li class="breadcrumb-item">휴가 관리</li>
								<li class="breadcrumb-item active" aria-current="page">휴가 신청</li>
							</ol>
						</div>
					</div>
				</div>
			</div>

			<div class="app-content">
				<div class="container-fluid">
					<div class="row g-3 mt-1">
						<!-- 입력 -->
						<div class="col-lg-8">
							<form id="leave-form" class="hr-card hr-form" novalidate>
								<div class="hr-section">
									<div class="hr-section-head">
										<div>
											<h3 class="hr-section-title">신청 내용</h3>
											<p class="hr-section-sub"><span class="hr-req">*</span> 필수 입력</p>
										</div>
									</div>

									<span class="form-label d-block">휴가 유형<span class="hr-req">*</span></span>
									<div class="hr-type-pick mb-4">
										<c:forEach var="t" items="${leaveTypes}" varStatus="st">
											<input type="radio" class="btn-check" name="leaveTypeId" id="type-${t.leaveTypeId}" value="${t.leaveTypeId}"
												   data-half="${t.halfDay}" data-deduct="${t.isDeduct == 1}" ${st.first ? 'checked' : ''}>
											<label class="btn" for="type-${t.leaveTypeId}">
												<c:out value="${t.leaveTypeName}" />
												<small>${t.isDeduct == 1 ? (t.halfDay ? '연차 0.5일' : '연차 차감') : '차감 안 함'}</small>
											</label>
										</c:forEach>
									</div>

									<div class="row g-3">
										<div class="col-md-6">
											<label for="startDate" class="form-label" id="start-label">시작일<span class="hr-req">*</span></label>
											<input type="date" id="startDate" name="startDate" class="form-control" min="${today}" required>
										</div>
										<div class="col-md-6" id="end-wrap">
											<label for="endDate" class="form-label">종료일<span class="hr-req">*</span></label>
											<input type="date" id="endDate" name="endDate" class="form-control" min="${today}" required>
										</div>
										<div class="col-12">
											<label for="reason" class="form-label">사유</label>
											<textarea id="reason" name="reason" class="form-control" rows="3" maxlength="200" placeholder="예) 가족 여행, 병원 진료"></textarea>
											<div class="text-end text-secondary small mt-1"><span id="reason-len">0</span> / 200</div>
										</div>
									</div>

									<div class="alert alert-danger py-2 small mt-3 mb-0" id="plan-error" hidden></div>
								</div>
								<div class="hr-form-foot">
									<div class="d-flex gap-2 ms-auto">
										<a href="${ctx}/leaves/my" class="btn btn-outline-secondary">취소</a>
										<button type="submit" class="btn btn-primary" id="bt-submit" disabled>신청</button>
									</div>
								</div>
							</form>
						</div>

						<!-- 요약 -->
						<div class="col-lg-4">
							<div class="hr-card hr-section hr-leave-summary">
								<div class="hr-section-head">
									<div>
										<h3 class="hr-section-title">신청 요약</h3>
										<p class="hr-section-sub">입력에 따라 바로 계산됩니다</p>
									</div>
								</div>
								<dl class="hr-dl">
									<dt>사용 일수</dt>
									<dd><b id="sum-days">-</b></dd>
									<dt>잔여 연차</dt>
									<dd>
										<c:choose>
											<c:when test="${annualLeave != null}"><span id="sum-remain"><fmt:formatNumber value="${annualLeave.remainDays}" pattern="0.#" /></span>일</c:when>
											<c:otherwise><span id="sum-remain" class="text-secondary">부여 내역 없음</span></c:otherwise>
										</c:choose>
									</dd>
									<dt>결재 대기</dt>
									<dd><span id="sum-pending"><fmt:formatNumber value="${pendingDays}" pattern="0.#" /></span>일</dd>
									<dt>신청 후 잔여</dt>
									<dd><b id="sum-after">-</b></dd>
									<dt>결재자</dt>
									<dd id="sum-approver">
										<c:choose>
											<c:when test="${approver != null}"><c:out value="${approver.ename}" /> <span class="text-secondary"><c:out value="${approver.job.jobName}" /></span></c:when>
											<c:otherwise><span class="text-secondary">없음 (바로 승인)</span></c:otherwise>
										</c:choose>
									</dd>
								</dl>
								<p class="hr-help">
									<i class="bi bi-info-circle text-primary"></i> 결재자는 소속 조직의 조직장이고, 본인이 조직장이면 상위 조직장입니다.
									<br>대기 중인 신청은 내 휴가 내역에서 취소할 수 있습니다.
								</p>
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
					<div class="hr-modal-info mt-3 text-start" id="done-detail"></div>
					<div class="d-flex gap-2 mt-4">
						<a href="${ctx}/leaves/new" class="btn btn-outline-secondary flex-fill">추가 신청</a>
						<a href="${ctx}/leaves/my" class="btn btn-primary flex-fill">내 휴가 내역</a>
					</div>
				</div>
			</div>
		</div>
	</div>

	<%@ include file="/WEB-INF/views/common/scripts.jsp"%>
	<script>
		const ctx = "${ctx}";
		const form = document.getElementById("leave-form");
		const startEl = document.getElementById("startDate");
		const endEl = document.getElementById("endDate");
		const reasonEl = document.getElementById("reason");
		const errorEl = document.getElementById("plan-error");
		const btSubmit = document.getElementById("bt-submit");
		let lastPlan = null;

		function selectedType() {
			return form.querySelector("input[name=leaveTypeId]:checked");
		}

		// 반차는 날짜 하나만
		function applyType() {
			const half = selectedType().dataset.half === "true";
			document.getElementById("end-wrap").hidden = half;
			document.getElementById("start-label").firstChild.textContent = half ? "날짜" : "시작일";
			if (half) {
				endEl.value = startEl.value;
			}
		}

		function showError(message) {
			errorEl.textContent = message;
			errorEl.hidden = !message;
		}

		function setSummary(plan) {
			document.getElementById("sum-days").textContent = plan ? plan.days + "일" : "-";
			if (plan && plan.remainDays != null) {
				document.getElementById("sum-remain").textContent = plan.remainDays;
			}
			if (plan) {
				document.getElementById("sum-pending").textContent = plan.pendingDays;
				document.getElementById("sum-approver").textContent = plan.approverName || "없음 (바로 승인)";
			}
			const after = document.getElementById("sum-after");
			if (!plan) {
				after.textContent = "-";
			} else if (!plan.deduct) {
				after.textContent = "차감 안 함";
			} else {
				after.textContent = (plan.afterDays != null ? plan.afterDays : "-") + "일";
			}
		}

		// 입력이 바뀔 때마다 서버에서 같은 규칙으로 계산 (주말·공휴일·잔여 연차·겹치는 신청)
		let timer = null;
		function refreshPlan() {
			clearTimeout(timer);
			timer = setTimeout(async () => {
				applyType();
				lastPlan = null;
				btSubmit.disabled = true;
				if (!startEl.value || !endEl.value) {
					setSummary(null);
					showError("");
					return;
				}
				const params = new URLSearchParams(new FormData(form));
				try {
					const response = await fetch(ctx + "/api/leaves/plan?" + params);
					if (response.redirected) {
						location.href = response.url;
						return;
					}
					const result = await response.json();
					if (response.ok) {
						lastPlan = result;
						setSummary(result);
						showError("");
						btSubmit.disabled = false;
					} else {
						setSummary(null);
						showError(result.message);
					}
				} catch (e) {
					console.log("휴가 계산 실패", e);
					showError("서버와 통신할 수 없습니다. 잠시 후 다시 시도해 주세요.");
				}
			}, 200);
		}

		form.querySelectorAll("input[name=leaveTypeId]").forEach(r => r.addEventListener("change", refreshPlan));
		startEl.addEventListener("change", () => {
			// 종료일이 비었거나 시작일보다 빠르면 시작일로 맞춤
			if (!endEl.value || endEl.value < startEl.value) {
				endEl.value = startEl.value;
			}
			endEl.min = startEl.value;
			refreshPlan();
		});
		endEl.addEventListener("change", refreshPlan);
		reasonEl.addEventListener("input", () => document.getElementById("reason-len").textContent = reasonEl.value.length);

		form.addEventListener("submit", async event => {
			event.preventDefault();
			if (!lastPlan) {
				return;
			}
			applyType();
			btSubmit.disabled = true;
			try {
				const response = await fetch(ctx + "/api/leaves", { method: "POST", body: new URLSearchParams(new FormData(form)) });
				if (response.redirected) {
					location.href = response.url;
					return;
				}
				let result;
				try {
					result = await response.json();
				} catch (e) {
					result = { message: response.status === 403 ? "휴가 신청 권한이 없습니다." : "요청을 처리하지 못했습니다." };
				}
				if (response.ok) {
					const typeName = selectedType().nextElementSibling.firstChild.textContent.trim();
					const period = startEl.value === endEl.value ? startEl.value : startEl.value + " ~ " + endEl.value;
					document.getElementById("done-message").textContent = result.message;
					document.getElementById("done-detail").textContent = typeName + " · " + period + " (" + lastPlan.days + "일)"
						+ (result.status === "APPROVED" ? "" : " · 결재자 " + (lastPlan.approverName || "-"));
					bootstrap.Modal.getOrCreateInstance(document.getElementById("done-modal")).show();
					return;
				}
				showError(result.message);
				hrToast(result.message, "danger");
			} catch (e) {
				console.log("휴가 신청 실패", e);
				hrToast("서버와 통신할 수 없습니다. 잠시 후 다시 시도해 주세요.", "danger");
			}
			btSubmit.disabled = false;
		});

		applyType();
	</script>
</body>
</html>
