<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%-- S-26 계정 관리: 상태별 계정 목록 + 잠금 해제 · 비밀번호 초기화(P-07) · 사용/중지 --%>
<c:set var="menuGroup" value="auth" />
<c:set var="menu" value="accounts" />
<sec:authorize access="hasAuthority('PERMISSION_ACCOUNT_UNLOCK')" var="canUnlock" />
<sec:authorize access="hasAuthority('PERMISSION_ACCOUNT_PASSWORD_RESET')" var="canReset" />
<sec:authorize access="hasAuthority('PERMISSION_ACCOUNT_ENABLE')" var="canEnable" />
<sec:authentication property="principal.account.accountId" var="myAccountId" />
<!DOCTYPE html>
<html lang="ko">
<head>
<%@ include file="/WEB-INF/views/common/head.jsp"%>
<title>계정 관리 | HexagonGMI 인사관리</title>
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
							<h2 class="hr-page-title">계정 관리</h2>
							<p class="hr-page-sub">잠긴 계정을 풀고, 비밀번호를 초기화하고, 계정 사용 여부를 바꿉니다.</p>
						</div>
						<div class="col-md-6">
							<ol class="breadcrumb justify-content-md-end mb-2">
								<li class="breadcrumb-item"><a href="${ctx}/dashboard">홈</a></li>
								<li class="breadcrumb-item">권한 관리</li>
								<li class="breadcrumb-item active" aria-current="page">계정 관리</li>
							</ol>
						</div>
					</div>
				</div>
			</div>

			<div class="app-content">
				<div class="container-fluid">
					<div class="hr-card hr-section mt-1">
						<div class="d-flex flex-wrap align-items-center gap-2 mb-3">
							<div class="hr-tabs mb-0">
								<c:url var="allUrl" value="/accounts"><c:param name="keyword" value="${keyword}" /></c:url>
								<a href="${allUrl}" class="${empty status ? 'active' : ''}">전체<b>${counts.ALL}</b></a>
								<c:url var="activeUrl" value="/accounts"><c:param name="status" value="ACTIVE" /><c:param name="keyword" value="${keyword}" /></c:url>
								<a href="${activeUrl}" class="${status == 'ACTIVE' ? 'active' : ''}">사용<b>${counts.ACTIVE}</b></a>
								<c:url var="lockedUrl" value="/accounts"><c:param name="status" value="LOCKED" /><c:param name="keyword" value="${keyword}" /></c:url>
								<a href="${lockedUrl}" class="${status == 'LOCKED' ? 'active' : ''}">잠김<b>${counts.LOCKED}</b></a>
								<c:url var="disabledUrl" value="/accounts"><c:param name="status" value="DISABLED" /><c:param name="keyword" value="${keyword}" /></c:url>
								<a href="${disabledUrl}" class="${status == 'DISABLED' ? 'active' : ''}">사용 중지<b>${counts.DISABLED}</b></a>
							</div>
							<form action="${ctx}/accounts" method="get" class="d-flex gap-2 ms-auto">
								<input type="hidden" name="status" value="${status}">
								<input type="text" name="keyword" class="form-control form-control-sm" style="width: 220px"
									   value="<c:out value='${keyword}' />" placeholder="이름 · 사번 · 아이디">
								<button type="submit" class="btn btn-sm btn-primary text-nowrap"><i class="bi bi-search"></i> 검색</button>
							</form>
						</div>

						<div class="table-responsive">
							<table class="table hr-table align-middle mb-0">
								<thead>
									<tr>
										<th style="width: 56px">No</th><th>사번</th><th>이름</th><th>부서</th><th>직급</th><th>아이디</th>
										<th>상태</th><th class="text-center">실패</th><th>마지막 로그인</th><th class="text-end">관리</th>
									</tr>
								</thead>
								<tbody>
									<c:forEach var="a" items="${pager.list}" varStatus="st">
										<c:set var="self" value="${a.accountId == myAccountId}" />
										<tr>
											<td class="text-secondary">${pager.num + st.index}</td>
											<td class="hr-time text-secondary"><c:out value="${a.empNo}" /></td>
											<td class="fw-semibold">
												<c:out value="${a.ename}" />
												<c:if test="${self}"><span class="hr-badge is-blue ms-1">나</span></c:if>
												<c:if test="${a.resigned}"><span class="hr-badge is-gray ms-1">퇴직</span></c:if>
											</td>
											<td><c:out value="${a.departmentName}" /></td>
											<td><c:out value="${a.jobName}" /></td>
											<td class="font-monospace small"><c:out value="${a.loginId}" /></td>
											<td>
												<span class="hr-badge ${a.status == 'ACTIVE' ? 'is-green' : (a.status == 'LOCKED' ? 'is-orange' : 'is-gray')}">${a.statusName}</span>
												<c:if test="${a.status == 'LOCKED'}"><div class="small text-secondary hr-time">${a.lockedText}</div></c:if>
											</td>
											<td class="text-center ${a.failCount > 0 ? 'text-danger fw-semibold' : 'text-secondary'}">${a.failCount}</td>
											<td class="hr-time">${a.lastLoginText}</td>
											<td class="text-end text-nowrap">
												<c:if test="${canUnlock && a.status == 'LOCKED'}">
													<button type="button" class="btn btn-sm btn-outline-primary" data-action="unlock"
															data-id="${a.accountId}" data-name="<c:out value='${a.ename}' />" data-login="<c:out value='${a.loginId}' />">
														<i class="bi bi-unlock"></i> 잠금 해제</button>
												</c:if>
												<c:if test="${canReset && a.enabled == 1 && !self}">
													<button type="button" class="btn btn-sm btn-outline-secondary" data-action="reset"
															data-id="${a.accountId}" data-name="<c:out value='${a.ename}' />" data-login="<c:out value='${a.loginId}' />">
														<i class="bi bi-key"></i> 비밀번호 초기화</button>
												</c:if>
												<c:if test="${canEnable && !self}">
													<c:choose>
														<c:when test="${a.enabled == 1}">
															<button type="button" class="btn btn-sm btn-outline-danger" data-action="disable"
																	data-id="${a.accountId}" data-name="<c:out value='${a.ename}' />" data-login="<c:out value='${a.loginId}' />">
																<i class="bi bi-slash-circle"></i> 사용 중지</button>
														</c:when>
														<c:when test="${!a.resigned}">
															<button type="button" class="btn btn-sm btn-outline-success" data-action="enable"
																	data-id="${a.accountId}" data-name="<c:out value='${a.ename}' />" data-login="<c:out value='${a.loginId}' />">
																<i class="bi bi-check-circle"></i> 사용</button>
														</c:when>
													</c:choose>
												</c:if>
											</td>
										</tr>
									</c:forEach>
									<c:if test="${empty pager.list}">
										<tr><td colspan="10" class="text-center text-secondary py-5">조건에 맞는 계정이 없습니다.</td></tr>
									</c:if>
								</tbody>
							</table>
						</div>

						<c:if test="${pager.totalPage > 1}">
							<ul class="pagination pagination-sm justify-content-end mt-3 mb-0">
								<li class="page-item ${pager.hasPrevBlock ? '' : 'disabled'}">
									<c:url var="prevUrl" value="/accounts">
										<c:param name="status" value="${status}" /><c:param name="keyword" value="${keyword}" /><c:param name="page" value="${pager.firstPage - 1}" />
									</c:url>
									<a class="page-link" href="${pager.hasPrevBlock ? prevUrl : '#'}" aria-label="이전 블럭">&laquo;</a>
								</li>
								<c:forEach var="i" begin="${pager.firstPage}" end="${pager.lastPage}">
									<c:url var="pageUrl" value="/accounts">
										<c:param name="status" value="${status}" /><c:param name="keyword" value="${keyword}" /><c:param name="page" value="${i}" />
									</c:url>
									<li class="page-item ${i == pager.currentPage ? 'active' : ''}"><a class="page-link" href="${pageUrl}">${i}</a></li>
								</c:forEach>
								<li class="page-item ${pager.hasNextBlock ? '' : 'disabled'}">
									<c:url var="nextUrl" value="/accounts">
										<c:param name="status" value="${status}" /><c:param name="keyword" value="${keyword}" /><c:param name="page" value="${pager.lastPage + 1}" />
									</c:url>
									<a class="page-link" href="${pager.hasNextBlock ? nextUrl : '#'}" aria-label="다음 블럭">&raquo;</a>
								</li>
							</ul>
						</c:if>

						<p class="hr-help">
							<i class="bi bi-info-circle text-primary"></i>
							비밀번호를 5회 틀리면 계정이 잠깁니다. 비밀번호 초기화는 임시 비밀번호를 만들어 <b>한 번만</b> 보여 주니 본인에게 바로 전달하세요.
							내 계정은 중지 · 초기화할 수 없고, 퇴직자 계정은 다시 사용할 수 없습니다.
						</p>
					</div>
				</div>
			</div>
		</main>

	</div>

	<!-- 잠금 해제 · 사용 · 사용 중지 확인 -->
	<div class="modal fade hr-modal" id="confirm-modal" tabindex="-1" aria-labelledby="confirm-title" aria-hidden="true">
		<div class="modal-dialog modal-dialog-centered" style="max-width: 420px">
			<div class="modal-content">
				<div class="modal-body p-4">
					<button type="button" class="btn-close float-end" data-bs-dismiss="modal" aria-label="닫기"></button>
					<div class="text-center">
						<span class="hr-modal-icon" id="confirm-icon"><i class="bi"></i></span>
						<h3 class="h5 fw-bold mt-3 mb-1" id="confirm-title"></h3>
						<p class="text-secondary small mb-0" id="confirm-target"></p>
					</div>
					<div class="mt-3" id="confirm-text"></div>
					<div class="d-flex gap-2 mt-4">
						<button type="button" class="btn btn-outline-secondary flex-fill" data-bs-dismiss="modal">닫기</button>
						<button type="button" class="btn flex-fill" id="bt-confirm"></button>
					</div>
				</div>
			</div>
		</div>
	</div>

	<!-- P-07 비밀번호 초기화 확인 -->
	<div class="modal fade hr-modal" id="reset-modal" tabindex="-1" aria-labelledby="reset-title" aria-hidden="true" data-bs-backdrop="static">
		<div class="modal-dialog modal-dialog-centered" style="max-width: 420px">
			<div class="modal-content">
				<div class="modal-body p-4">
					<div class="text-center">
						<span class="hr-modal-icon is-ok"><i class="bi bi-key"></i></span>
						<h3 class="h5 fw-bold mt-3 mb-1" id="reset-title">비밀번호 초기화</h3>
						<p class="text-secondary small mb-0" id="reset-target"></p>
					</div>
					<!-- 1단계: 확인 -->
					<div id="reset-step-confirm">
						<div class="hr-modal-warn mt-3">
							지금 비밀번호는 더 이상 쓸 수 없게 되고, 영문 · 숫자 10자리 임시 비밀번호가 만들어집니다.
							임시 비밀번호는 이 창에서 <b>한 번만</b> 보여 줍니다.
						</div>
						<div class="d-flex gap-2 mt-4">
							<button type="button" class="btn btn-outline-secondary flex-fill" data-bs-dismiss="modal">취소</button>
							<button type="button" class="btn btn-primary flex-fill" id="bt-reset">초기화</button>
						</div>
					</div>
					<!-- 2단계: 결과 -->
					<div id="reset-step-done" hidden>
						<div class="temp-pw mt-3">
							<span id="temp-pw"></span>
							<button type="button" class="btn btn-sm btn-outline-primary" id="bt-copy" title="복사"><i class="bi bi-clipboard"></i></button>
						</div>
						<div class="hr-modal-info mt-3" id="reset-note">
							본인에게 전달하고, 로그인한 뒤 비밀번호를 바꾸도록 안내하세요.
						</div>
						<button type="button" class="btn btn-primary w-100 mt-4" id="bt-reset-close">확인</button>
					</div>
				</div>
			</div>
		</div>
	</div>

	<%@ include file="/WEB-INF/views/common/scripts.jsp"%>
	<script>
		const ctx = "${ctx}";

		async function send(url, body) {
			const options = { method: "POST", headers: {} };
			if (body) {
				options.headers["Content-Type"] = "application/json";
				options.body = JSON.stringify(body);
			}
			try {
				const response = await fetch(url, options);
				if (response.redirected) {
					location.href = response.url;
					return null;
				}
				let result;
				try {
					result = await response.json();
				} catch (e) {
					result = { message: response.status === 403 ? "권한이 없습니다." : "요청을 처리하지 못했습니다." };
				}
				if (response.ok) {
					return result;
				}
				hrToast(result.message, "danger");
			} catch (e) {
				console.log("계정 요청 실패", e);
				hrToast("서버와 통신할 수 없습니다. 잠시 후 다시 시도해 주세요.", "danger");
			}
			return null;
		}

		// 잠금 해제 · 사용 · 사용 중지
		const CONFIRM = {
			unlock:  { title: "잠금 해제", icon: "bi-unlock", ok: true, button: "btn-primary", label: "잠금 해제",
					   text: "로그인 실패 횟수를 0으로 되돌리고 잠금을 풉니다. 비밀번호는 그대로입니다.",
					   url: id => "/api/accounts/" + id + "/unlock" },
			disable: { title: "계정 사용 중지", icon: "bi-slash-circle", ok: false, button: "btn-danger", label: "사용 중지",
					   text: "이 계정으로는 로그인할 수 없게 됩니다. 이미 로그인한 상태라면 다음 로그인부터 막힙니다.",
					   url: id => "/api/accounts/" + id + "/enable", body: { enabled: 0 } },
			enable:  { title: "계정 사용", icon: "bi-check-circle", ok: true, button: "btn-primary", label: "사용",
					   text: "이 계정으로 다시 로그인할 수 있게 됩니다.",
					   url: id => "/api/accounts/" + id + "/enable", body: { enabled: 1 } }
		};
		const confirmModalEl = document.getElementById("confirm-modal");
		const confirmModal = new bootstrap.Modal(confirmModalEl);
		const btConfirm = document.getElementById("bt-confirm");
		let pending = null;

		document.querySelectorAll("[data-action]").forEach(button => {
			button.addEventListener("click", () => {
				const target = button.dataset.name + " · " + button.dataset.login;
				if (button.dataset.action === "reset") {
					openReset(button.dataset.id, target);
					return;
				}
				const c = CONFIRM[button.dataset.action];
				pending = { id: button.dataset.id, c: c };
				const icon = document.getElementById("confirm-icon");
				icon.className = "hr-modal-icon" + (c.ok ? " is-ok" : "");
				icon.innerHTML = '<i class="bi ' + c.icon + '"></i>';
				document.getElementById("confirm-title").textContent = c.title;
				document.getElementById("confirm-target").textContent = target;
				const text = document.getElementById("confirm-text");
				text.className = "mt-3 " + (c.ok ? "hr-modal-info" : "hr-modal-warn");
				text.textContent = c.text;
				btConfirm.className = "btn flex-fill " + c.button;
				btConfirm.textContent = c.label;
				btConfirm.disabled = false;
				confirmModal.show();
			});
		});

		btConfirm.addEventListener("click", async () => {
			if (!pending) {
				return;
			}
			btConfirm.disabled = true;
			const result = await send(ctx + pending.c.url(pending.id), pending.c.body);
			if (result) {
				confirmModal.hide();
				hrToast(result.message, "success");
				setTimeout(() => location.reload(), 1000);
				return;
			}
			btConfirm.disabled = false;
		});

		// P-07 비밀번호 초기화
		const resetModalEl = document.getElementById("reset-modal");
		const resetModal = new bootstrap.Modal(resetModalEl);
		const btReset = document.getElementById("bt-reset");
		let resetId = null;
		let resetDone = false;

		function openReset(id, target) {
			resetId = id;
			resetDone = false;
			document.getElementById("reset-target").textContent = target;
			document.getElementById("reset-step-confirm").hidden = false;
			document.getElementById("reset-step-done").hidden = true;
			document.getElementById("temp-pw").textContent = "";
			btReset.disabled = false;
			resetModal.show();
		}

		btReset.addEventListener("click", async () => {
			btReset.disabled = true;
			const result = await send(ctx + "/api/accounts/" + resetId + "/reset-password");
			if (result) {
				resetDone = true;
				document.getElementById("temp-pw").textContent = result.tempPassword;
				document.getElementById("reset-step-confirm").hidden = true;
				document.getElementById("reset-step-done").hidden = false;
				hrToast(result.message, "success");
				return;
			}
			btReset.disabled = false;
		});

		document.getElementById("bt-copy").addEventListener("click", async () => {
			const text = document.getElementById("temp-pw").textContent;
			try {
				await navigator.clipboard.writeText(text);
				hrToast("임시 비밀번호를 복사했습니다.", "success");
			} catch (e) {
				// http 환경 등 clipboard API 를 못 쓰면 글자를 선택해 둔다
				const range = document.createRange();
				range.selectNodeContents(document.getElementById("temp-pw"));
				const selection = window.getSelection();
				selection.removeAllRanges();
				selection.addRange(range);
				hrToast("Ctrl + C 로 복사하세요.", "info");
			}
		});

		document.getElementById("bt-reset-close").addEventListener("click", () => {
			resetModal.hide();
		});
		// 결과를 본 뒤 닫으면 화면에서 임시 비밀번호를 지우고 목록 새로고침 (실패 횟수 0 반영)
		resetModalEl.addEventListener("hidden.bs.modal", () => {
			document.getElementById("temp-pw").textContent = "";
			if (resetDone) {
				location.reload();
			}
		});
	</script>
</body>
</html>
