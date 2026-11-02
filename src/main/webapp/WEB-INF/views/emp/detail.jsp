<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<c:set var="menuGroup" value="hr" />
<c:set var="menu" value="employees" />
<!DOCTYPE html>
<html lang="ko">
<head>
<%@ include file="/WEB-INF/views/common/head.jsp"%>
<title><c:out value="${emp.ename}" /> | 사원 상세 | 규성컴퍼니 인사관리</title>
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
							<h2 class="hr-page-title">사원 상세</h2>
							<p class="hr-page-sub">사원의 인사 정보와 계정, 발령 이력을 확인합니다.</p>
						</div>
						<div class="col-md-6">
							<ol class="breadcrumb justify-content-md-end mb-2">
								<li class="breadcrumb-item"><a href="${ctx}/dashboard">홈</a></li>
								<li class="breadcrumb-item"><a href="${ctx}/employees">직원 목록</a></li>
								<li class="breadcrumb-item active" aria-current="page">사원 상세</li>
							</ol>
							<%-- 버튼은 권한이 있을 때만 (URL 권한은 SecurityConfig 에서 한 번 더 검사) --%>
							<div class="hr-head-actions">
								<a href="${ctx}/employees" class="btn btn-outline-secondary"><i class="bi bi-list-ul"></i> 목록</a>
								<sec:authorize access="hasAuthority('PERMISSION_EMP_UPDATE')">
									<a href="${ctx}/employees/${emp.empId}/edit" class="btn btn-primary"><i class="bi bi-pencil"></i> 수정</a>
								</sec:authorize>
								<sec:authorize access="hasAuthority('PERMISSION_EMP_RESIGN')">
									<c:if test="${emp.active}">
										<button type="button" class="btn btn-outline-danger" data-bs-toggle="modal" data-bs-target="#resign-modal">
											<i class="bi bi-person-x"></i> 퇴직 처리
										</button>
									</c:if>
								</sec:authorize>
							</div>
						</div>
					</div>
				</div>
			</div>

			<div class="app-content">
				<div class="container-fluid">
					<c:set var="dept" value="${emp.department}" />
					<sec:authorize access="hasAnyAuthority('PERMISSION_EMP_UPDATE','PERMISSION_EMP_CREATE')" var="isHr" />

					<div class="row g-3 mt-1">
						<!-- 프로필 -->
						<div class="col-lg-4">
							<div class="hr-card hr-profile h-100">
								<span class="hr-avatar">
									<c:choose>
										<c:when test="${not empty emp.photoPath}">
											<img src="${ctx}/employees/${emp.empId}/photo" alt="<c:out value='${emp.ename}' /> 증명사진">
										</c:when>
										<c:otherwise><c:out value="${fn:substring(emp.ename, 0, 1)}" /></c:otherwise>
									</c:choose>
								</span>
								<h3 class="hr-profile-name"><c:out value="${emp.ename}" /></h3>
								<div class="hr-profile-sub">
									<c:out value="${dept.pathName}" /> · <c:out value="${emp.job.jobName}" />
									<c:if test="${emp.hrPosition != null}"> · <c:out value="${emp.hrPosition.positionName}" /></c:if>
								</div>
								<div class="mt-2">
									<span class="hr-badge ${emp.active ? 'is-green' : 'is-gray'}">${emp.statusName}</span>
								</div>
								<dl class="hr-dl">
									<dt>사번</dt>
									<dd><c:out value="${emp.empNo}" /></dd>
									<dt>입사일</dt>
									<dd>${emp.hiredate}</dd>
									<c:if test="${not emp.active}">
										<dt>퇴직일</dt>
										<dd>${emp.resignDate}</dd>
									</c:if>
									<dt>근속</dt>
									<dd>${tenure}</dd>
									<dt>올해 연차</dt>
									<dd>
										<c:choose>
											<c:when test="${annualLeave != null}">
												잔여 <b><fmt:formatNumber value="${annualLeave.remainDays}" pattern="0.#" />일</b>
												<span class="text-secondary small">(부여 <fmt:formatNumber value="${annualLeave.grantedDays}" pattern="0.#" /> · 사용 <fmt:formatNumber value="${annualLeave.usedDays}" pattern="0.#" />)</span>
											</c:when>
											<c:otherwise><span class="text-secondary">부여 내역 없음</span></c:otherwise>
										</c:choose>
									</dd>
								</dl>
							</div>
						</div>

						<!-- 기본 정보 -->
						<div class="col-lg-8">
							<div class="hr-card hr-section h-100">
								<div class="hr-section-head">
									<div>
										<h3 class="hr-section-title">기본 정보</h3>
										<p class="hr-section-sub">인적 사항과 소속 정보</p>
									</div>
								</div>
								<div class="row g-0 column-gap-lg-5">
									<dl class="hr-dl col-lg">
										<dt>이름</dt>
										<dd><c:out value="${emp.ename}" /></dd>
										<dt>성별</dt>
										<dd>${emp.genderName}</dd>
										<dt>이메일</dt>
										<dd><c:out value="${empty emp.email ? '-' : emp.email}" /></dd>
										<dt>연락처</dt>
										<dd><c:out value="${empty emp.phone ? '-' : emp.phone}" /></dd>
									</dl>
									<dl class="hr-dl col-lg">
										<dt>소속</dt>
										<dd><c:out value="${dept.pathName}" /></dd>
										<dt>직급</dt>
										<dd><c:out value="${emp.job.jobName}" /></dd>
										<dt>직책</dt>
										<dd><c:out value="${emp.hrPosition != null ? emp.hrPosition.positionName : '-'}" /></dd>
										<dt>재직 상태</dt>
										<dd><span class="hr-badge ${emp.active ? 'is-green' : 'is-gray'}">${emp.statusName}</span></dd>
									</dl>
								</div>
								<p class="text-secondary small mt-3 mb-0">
									<i class="bi bi-info-circle text-primary"></i> 소속·직급·직책 변경은 이력이 남도록 인사 발령으로 처리합니다.
								</p>
							</div>
						</div>

						<!-- 계정 정보 (인사 담당만, 컨트롤러도 이 경우에만 account 를 담음) -->
						<c:if test="${isHr}">
						<div class="col-lg-4">
							<div class="hr-card hr-section h-100">
								<div class="hr-section-head">
									<div>
										<h3 class="hr-section-title">계정 정보</h3>
										<p class="hr-section-sub">로그인 계정과 보유 Role</p>
									</div>
								</div>
								<c:choose>
									<c:when test="${account != null}">
										<dl class="hr-dl">
											<dt>로그인 아이디</dt>
											<dd><c:out value="${account.loginId}" /></dd>
											<dt>계정 상태</dt>
											<dd>
												<span class="hr-badge ${account.statusName == '사용' ? 'is-green' : (account.statusName == '잠김' ? 'is-orange' : 'is-gray')}">${account.statusName}</span>
											</dd>
											<dt>마지막 로그인</dt>
											<dd>${account.lastLoginText}</dd>
											<dt>보유 Role</dt>
											<dd>
												<div class="hr-roles">
													<c:forEach var="role" items="${account.roles}">
														<span class="hr-badge is-blue" title="<c:out value='${role.roleCode}' />"><c:out value="${role.roleName}" /></span>
													</c:forEach>
													<c:if test="${empty account.roles}"><span class="text-secondary">없음</span></c:if>
												</div>
											</dd>
										</dl>
									</c:when>
									<c:otherwise>
										<p class="hr-empty mb-0">발급된 계정이 없습니다.</p>
									</c:otherwise>
								</c:choose>
							</div>
						</div>
						</c:if>

						<!-- 발령 이력 -->
						<div class="${isHr ? 'col-lg-8' : 'col-12'}">
							<div class="hr-card hr-list">
								<div class="hr-list-head">
									<h3>발령 이력 <span class="hr-count">(${fn:length(appointments)})</span></h3>
								</div>
								<div class="table-responsive">
									<table class="table hr-table text-nowrap">
										<thead>
											<tr>
												<th>발령일</th>
												<th>구분</th>
												<th>소속</th>
												<th>직급</th>
												<th>직책</th>
												<th>사유</th>
												<th>등록자</th>
											</tr>
										</thead>
										<tbody>
											<%-- 바뀐 항목만 "전 → 후" 로, 그대로면 현재 값만 --%>
											<c:forEach var="a" items="${appointments}">
												<tr>
													<td class="text-nowrap">${a.effectiveDate}</td>
													<td>
														<span class="hr-badge ${a.appointmentType == 'HIRE' ? 'is-green' : (a.appointmentType == 'RESIGN' ? 'is-gray' : (a.appointmentType == 'PROMOTION' ? 'is-blue' : 'is-purple'))}">${a.typeName}</span>
													</td>
													<td><c:out value="${a.beforeDepartment.departmentName}" /><c:if test="${a.beforeDepartment != null and a.afterDepartment != null and a.beforeDepartment.departmentId != a.afterDepartment.departmentId}"> → </c:if><c:if test="${a.beforeDepartment == null or a.afterDepartment == null or a.beforeDepartment.departmentId != a.afterDepartment.departmentId}"><c:out value="${a.afterDepartment.departmentName}" /></c:if></td>
													<td><c:out value="${a.beforeJob.jobName}" /><c:if test="${a.beforeJob != null and a.afterJob != null and a.beforeJob.jobId != a.afterJob.jobId}"> → </c:if><c:if test="${a.beforeJob == null or a.afterJob == null or a.beforeJob.jobId != a.afterJob.jobId}"><c:out value="${a.afterJob.jobName}" /></c:if></td>
													<td>
														<c:choose>
															<c:when test="${a.beforeHrPosition == null and a.afterHrPosition == null}">-</c:when>
															<c:otherwise><c:out value="${a.beforeHrPosition != null ? a.beforeHrPosition.positionName : '없음'}" /> → <c:out value="${a.afterHrPosition != null ? a.afterHrPosition.positionName : '없음'}" /></c:otherwise>
														</c:choose>
													</td>
													<td><c:out value="${a.reason}" /></td>
													<td class="text-nowrap"><c:out value="${a.register != null ? a.register.ename : '-'}" /></td>
												</tr>
											</c:forEach>
											<c:if test="${empty appointments}">
												<tr><td colspan="7" class="hr-empty">발령 이력이 없습니다.</td></tr>
											</c:if>
										</tbody>
									</table>
								</div>
							</div>
						</div>
					</div>

				</div>
			</div>
		</main>

	</div>

	<!-- P-02 퇴직 처리 팝업 -->
	<sec:authorize access="hasAuthority('PERMISSION_EMP_RESIGN')">
	<c:if test="${emp.active}">
	<div class="modal fade hr-modal" id="resign-modal" tabindex="-1" aria-labelledby="resign-title" aria-hidden="true">
		<div class="modal-dialog modal-dialog-centered" style="max-width: 440px">
			<div class="modal-content">
				<div class="modal-body p-4">
					<button type="button" class="btn-close float-end" data-bs-dismiss="modal" aria-label="닫기"></button>
					<div class="text-center">
						<span class="hr-modal-icon"><i class="bi bi-person-x"></i></span>
						<h3 class="h5 fw-bold mt-3 mb-1" id="resign-title">퇴직 처리</h3>
						<p class="text-secondary small">
							<c:out value="${emp.ename}" /> (<c:out value="${emp.empNo}" /> · <c:out value="${dept.departmentName}" />) 사원을 퇴직 처리합니다.
						</p>
					</div>
					<label for="resign-date" class="form-label fw-semibold small">퇴직일</label>
					<input type="date" id="resign-date" class="form-control" value="${today}" min="${emp.hiredate}">
					<div class="hr-modal-warn mt-3">
						재직 상태가 '퇴직'으로 바뀌고 로그인 계정이 사용 중지됩니다. 사원 데이터는 삭제되지 않습니다.
					</div>
					<div class="d-flex gap-2 mt-4">
						<button type="button" class="btn btn-outline-secondary flex-fill" data-bs-dismiss="modal">취소</button>
						<button type="button" class="btn btn-danger flex-fill" id="bt-resign">퇴직 처리</button>
					</div>
				</div>
			</div>
		</div>
	</div>
	</c:if>
	</sec:authorize>

	<%-- 등록·수정 후 돌아왔을 때 안내 문구 (data 속성에 넣어야 따옴표·태그가 섞여도 안전) --%>
	<span id="flash-message" data-message="<c:out value='${message}' />" hidden></span>

	<%@ include file="/WEB-INF/views/common/scripts.jsp"%>
	<script>
		const ctx = "${ctx}";

		const flash = document.getElementById("flash-message").dataset.message;
		if (flash) {
			hrToast(flash, "success");
		}

		// 퇴직 처리: fetch 로 POST → RestEmpController → 결과 JSON
		const btResign = document.getElementById("bt-resign");
		if (btResign) {
			btResign.addEventListener("click", async () => {
				const resignDate = document.getElementById("resign-date").value;
				if (!resignDate) {
					hrToast("퇴직일을 입력하세요.", "danger");
					return;
				}
				btResign.disabled = true;
				try {
					const response = await fetch(ctx + "/api/employees/${emp.empId}/resign", {
						method: "POST",
						body: new URLSearchParams({ resignDate: resignDate })
					});
					if (response.redirected) { // 세션이 끝나 로그인 화면으로 보내진 경우
						location.href = response.url;
						return;
					}
					let result;
					try {
						result = await response.json();
					} catch (e) {
						result = { message: response.status === 403 ? "퇴직 처리 권한이 없습니다." : "요청을 처리하지 못했습니다." };
					}
					if (response.ok) {
						bootstrap.Modal.getInstance(document.getElementById("resign-modal")).hide();
						hrToast(result.message, "success");
						setTimeout(() => location.reload(), 900);
						return;
					}
					hrToast(result.message, "danger");
				} catch (error) {
					console.log("퇴직 처리 요청 실패", error);
					hrToast("서버와 통신할 수 없습니다. 잠시 후 다시 시도해 주세요.", "danger");
				}
				btResign.disabled = false;
			});
		}
	</script>
</body>
</html>
