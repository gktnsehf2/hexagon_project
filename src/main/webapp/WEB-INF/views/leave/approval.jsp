<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%-- S-15 휴가 결재: 내가 결재자인 대기 건 승인 · 반려(P-03 사유), 처리 완료 내역 --%>
<c:set var="menuGroup" value="leave" />
<c:set var="menu" value="leaveApproval" />
<!DOCTYPE html>
<html lang="ko">
<head>
<%@ include file="/WEB-INF/views/common/head.jsp"%>
<title>휴가 결재 | HexagonGMI 인사관리</title>
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
							<h2 class="hr-page-title">휴가 결재</h2>
							<p class="hr-page-sub">나에게 온 휴가 신청을 승인하거나 반려합니다.</p>
						</div>
						<div class="col-md-6">
							<ol class="breadcrumb justify-content-md-end mb-2">
								<li class="breadcrumb-item"><a href="${ctx}/dashboard">홈</a></li>
								<li class="breadcrumb-item">휴가 관리</li>
								<li class="breadcrumb-item active" aria-current="page">휴가 결재</li>
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
								<h3 class="hr-section-title">${tab == 'done' ? '처리 완료' : '결재 대기'} <span class="hr-count">(${fn:length(leaves)})</span></h3>
								<p class="hr-section-sub">${tab == 'done' ? '내가 승인 · 반려한 휴가 (최근 100건)' : '휴가 시작일이 가까운 순'}</p>
							</div>
							<a href="${ctx}/calendar" class="btn btn-sm btn-outline-secondary"><i class="bi bi-calendar3"></i> 휴가 캘린더</a>
						</div>

						<nav class="hr-tabs" aria-label="결재 구분">
							<a href="${ctx}/leaves/approvals" class="${tab == 'pending' ? 'active' : ''}">결재 대기<b>${pendingCount}</b></a>
							<a href="${ctx}/leaves/approvals?tab=done" class="${tab == 'done' ? 'active' : ''}">처리 완료<b>${doneCount}</b></a>
						</nav>

						<c:choose>
							<c:when test="${empty leaves}">
								<p class="hr-empty mb-0">${tab == 'done' ? '처리한 휴가가 없습니다.' : '결재할 휴가 신청이 없습니다.'}</p>
							</c:when>
							<c:otherwise>
								<div class="table-responsive">
									<table class="table hr-table align-middle mb-0">
										<thead>
											<tr>
												<th>신청번호</th>
												<th>신청자</th>
												<th>휴가 유형</th>
												<th>사용 기간</th>
												<th class="text-end">일수</th>
												<c:if test="${tab == 'pending'}"><th class="text-end">잔여 연차</th></c:if>
												<th>사유</th>
												<th>신청일</th>
												<c:choose>
													<c:when test="${tab == 'pending'}"><th class="text-end">처리</th></c:when>
													<c:otherwise><th>결과</th><th>처리일</th></c:otherwise>
												</c:choose>
											</tr>
										</thead>
										<tbody>
											<c:forEach var="leave" items="${leaves}">
												<tr>
													<td class="hr-time text-secondary">${leave.requestNo}</td>
													<td>
														<span class="hr-name"><c:out value="${leave.emp.ename}" /></span>
														<div class="text-secondary small"><c:out value="${leave.emp.department.departmentName}" /> · <c:out value="${leave.emp.job.jobName}" /></div>
													</td>
													<td><span class="hr-badge ${leave.typeTone}"><c:out value="${leave.leaveType.leaveTypeName}" /></span></td>
													<td class="hr-time">${leave.periodText}</td>
													<td class="text-end hr-time">${leave.daysText}일</td>
													<c:if test="${tab == 'pending'}">
														<td class="text-end hr-time">
															<c:choose>
																<c:when test="${!leave.deduct}"><span class="text-secondary small">차감 안 함</span></c:when>
																<c:when test="${leave.remainDays == null}"><span class="text-danger small">부여 내역 없음</span></c:when>
																<c:otherwise><span class="${leave.shortOfLeave ? 'text-danger fw-semibold' : ''}">${leave.remainDaysText}일</span></c:otherwise>
															</c:choose>
														</td>
													</c:if>
													<td><c:out value="${empty leave.reason ? '-' : leave.reason}" /></td>
													<td class="hr-time text-secondary">${leave.requestDateText}</td>
													<c:choose>
														<c:when test="${tab == 'pending'}">
															<td class="text-end text-nowrap">
																<button type="button" class="btn btn-sm btn-primary bt-approve"
																		data-id="${leave.leaveRequestId}" data-no="${leave.requestNo}"
																		data-desc="<c:out value='${leave.emp.ename}' /> · <c:out value='${leave.leaveType.leaveTypeName}' /> · ${leave.periodText} (${leave.daysText}일)"
																		data-deduct="${leave.deduct ? leave.daysText : ''}"
																		${leave.shortOfLeave ? 'disabled title="잔여 연차가 부족합니다"' : ''}>승인</button>
																<button type="button" class="btn btn-sm btn-outline-danger bt-reject"
																		data-id="${leave.leaveRequestId}" data-no="${leave.requestNo}"
																		data-desc="<c:out value='${leave.emp.ename}' /> · <c:out value='${leave.leaveType.leaveTypeName}' /> · ${leave.periodText} (${leave.daysText}일)">반려</button>
															</td>
														</c:when>
														<c:otherwise>
															<td>
																<span class="hr-badge ${leave.statusTone}">${leave.statusName}</span>
																<c:if test="${not empty leave.rejectReason}"><div class="hr-reject"><c:out value="${leave.rejectReason}" /></div></c:if>
															</td>
															<td class="hr-time text-secondary">${leave.approvedAtText}</td>
														</c:otherwise>
													</c:choose>
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
		</main>

	</div>

	<!-- 승인 확인 -->
	<div class="modal fade hr-modal" id="approve-modal" tabindex="-1" aria-labelledby="approve-title" aria-hidden="true">
		<div class="modal-dialog modal-dialog-centered" style="max-width: 420px">
			<div class="modal-content">
				<div class="modal-body p-4">
					<button type="button" class="btn-close float-end" data-bs-dismiss="modal" aria-label="닫기"></button>
					<div class="text-center">
						<span class="hr-modal-icon is-ok"><i class="bi bi-check2-circle"></i></span>
						<h3 class="h5 fw-bold mt-3 mb-1" id="approve-title">휴가 승인</h3>
						<p class="text-secondary small mb-0"><b class="js-no"></b><br><span class="js-desc"></span></p>
					</div>
					<div class="hr-modal-info mt-3" id="approve-deduct"></div>
					<div class="d-flex gap-2 mt-4">
						<button type="button" class="btn btn-outline-secondary flex-fill" data-bs-dismiss="modal">닫기</button>
						<button type="button" class="btn btn-primary flex-fill" id="bt-approve-ok">승인</button>
					</div>
				</div>
			</div>
		</div>
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
						<p class="text-secondary small"><b class="js-no"></b><br><span class="js-desc"></span></p>
					</div>
					<label for="reject-reason" class="form-label fw-semibold small">반려 사유 <span class="text-danger">*</span></label>
					<textarea id="reject-reason" class="form-control" rows="3" maxlength="200" placeholder="신청자에게 보여집니다. 예) 프로젝트 마감 주간이라 일정 조정 부탁드립니다."></textarea>
					<div class="text-end text-secondary small mt-1"><span id="reject-len">0</span> / 200</div>
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
		/*
		 휴가 결재: 화면 탐색 · 이벤트는 jQuery, 서버 통신은 fetch + async/await
		 승인/반려 → RestLeaveController → ApiResponse JSON
		*/
		const ctx = "${ctx}";
		const $approveModal = $("#approve-modal");
		const $rejectModal = $("#reject-modal");
		const $reason = $("#reject-reason");
		let targetId = null;

		function fill($modal, $bt) {
			targetId = $bt.data("id");
			$modal.find(".js-no").text($bt.data("no"));
			$modal.find(".js-desc").text($bt.data("desc"));
		}

		// 이벤트 위임: 목록의 모든 승인 · 반려 버튼을 한 번에 처리
		$(document).on("click", ".bt-approve", function () {
			const $bt = $(this);
			const deduct = $bt.data("deduct");
			fill($approveModal, $bt);
			$("#approve-deduct").text(deduct
				? "승인하면 신청자의 연차에서 " + deduct + "일이 차감되고, 휴가 캘린더에 바로 표시됩니다."
				: "연차에서 차감하지 않는 휴가입니다. 승인하면 휴가 캘린더에 바로 표시됩니다.");
			bootstrap.Modal.getOrCreateInstance($approveModal[0]).show();
		});

		$(document).on("click", ".bt-reject", function () {
			fill($rejectModal, $(this));
			$reason.val("");
			$("#reject-len").text("0");
			bootstrap.Modal.getOrCreateInstance($rejectModal[0]).show();
		});
		$rejectModal.on("shown.bs.modal", () => $reason.trigger("focus"));
		$reason.on("input", () => $("#reject-len").text($reason.val().length));

		async function send($button, $modal, url, body) {
			$button.prop("disabled", true);
			try {
				const response = await fetch(url, { method: "POST", body: body });
				if (response.redirected) { // 세션이 끝나 로그인 화면으로 보내진 경우
					location.href = response.url;
					return;
				}
				let result;
				try {
					result = await response.json();
				} catch (e) {
					result = { message: response.status === 403 ? "휴가 결재 권한이 없습니다." : "요청을 처리하지 못했습니다." };
				}
				if (response.ok) {
					bootstrap.Modal.getInstance($modal[0]).hide();
					hrToast(result.message, "success");
					setTimeout(() => location.reload(), 900);
					return;
				}
				hrToast(result.message, "danger");
			} catch (error) {
				console.log("휴가 결재 요청 실패", error);
				hrToast("서버와 통신할 수 없습니다. 잠시 후 다시 시도해 주세요.", "danger");
			}
			$button.prop("disabled", false);
		}

		$("#bt-approve-ok").on("click", function () {
			send($(this), $approveModal, ctx + "/api/leaves/" + targetId + "/approve");
		});

		$("#bt-reject-ok").on("click", function () {
			const reason = $reason.val().trim();
			if (!reason) {
				hrToast("반려 사유를 입력하세요.", "danger");
				$reason.trigger("focus");
				return;
			}
			send($(this), $rejectModal, ctx + "/api/leaves/" + targetId + "/reject", new URLSearchParams({ rejectReason: reason }));
		});
	</script>
</body>
</html>
