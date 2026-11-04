<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%-- S-06 조직도: 본사 > 부 > 팀 트리 + 선택한 조직의 정보 · 하위 조직 · 소속 사원 / 삭제 확인(P-04) --%>
<c:set var="menuGroup" value="dept" />
<c:set var="menu" value="departments" />
<!DOCTYPE html>
<html lang="ko">
<head>
<%@ include file="/WEB-INF/views/common/head.jsp"%>
<title>조직도 | HexagonGMI 인사관리</title>
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
							<h2 class="hr-page-title">조직도</h2>
							<p class="hr-page-sub">본사 · 부 · 팀 구조와 조직장, 소속 사원을 확인합니다.</p>
						</div>
						<div class="col-md-6">
							<ol class="breadcrumb justify-content-md-end mb-2">
								<li class="breadcrumb-item"><a href="${ctx}/dashboard">홈</a></li>
								<li class="breadcrumb-item">부서 관리</li>
								<li class="breadcrumb-item active" aria-current="page">조직도</li>
							</ol>
						</div>
					</div>
				</div>
			</div>

			<div class="app-content">
				<div class="container-fluid">
					<div class="row g-3 mt-1">

						<!-- 트리 -->
						<div class="col-lg-4">
							<div class="hr-card hr-section h-100">
								<div class="hr-section-head">
									<div>
										<h3 class="hr-section-title">조직 <span class="hr-count">(${fn:length(tree)})</span></h3>
										<p class="hr-section-sub">괄호 안은 하위 조직까지 합친 재직 인원</p>
									</div>
									<sec:authorize access="hasAuthority('PERMISSION_DEPT_CREATE')">
										<a href="${ctx}/departments/new" class="btn btn-sm btn-primary text-nowrap"><i class="bi bi-plus-lg"></i> 부서 등록</a>
									</sec:authorize>
								</div>
								<sec:authorize access="hasAuthority('PERMISSION_DEPT_UPDATE')">
									<div class="form-check form-switch small mb-2">
										<input class="form-check-input" type="checkbox" id="show-all" ${includeInactive ? 'checked' : ''}
											   onchange="location.href='${ctx}/departments?id=${selected.departmentId}' + (this.checked ? '&all=1' : '')">
										<label class="form-check-label text-secondary" for="show-all">사용 안 함 조직도 보기</label>
									</div>
								</sec:authorize>
								<ul class="hr-tree">
									<c:forEach var="d" items="${tree}">
										<li class="lv-${d.departmentLevel} ${d.departmentId == selected.departmentId ? 'is-selected' : ''} ${d.active ? '' : 'is-off'}">
											<a href="${ctx}/departments?id=${d.departmentId}${includeInactive ? '&all=1' : ''}">
												<i class="bi ${d.departmentLevel == 1 ? 'bi-building' : (d.departmentLevel == 2 ? 'bi-diagram-3' : 'bi-people')}"></i>
												<span class="hr-tree-name"><c:out value="${d.departmentName}" /></span>
												<span class="hr-tree-count">${d.totalCount}</span>
												<c:if test="${!d.active}"><span class="hr-badge is-gray">사용 안 함</span></c:if>
											</a>
											<c:if test="${not empty d.managerName}">
												<div class="hr-tree-manager"><c:out value="${d.managerTitle}" /> <c:out value="${d.managerName}" /></div>
											</c:if>
										</li>
									</c:forEach>
								</ul>
							</div>
						</div>

						<!-- 선택한 조직 -->
						<div class="col-lg-8">
							<c:choose>
								<c:when test="${selected == null}">
									<div class="hr-card hr-section"><p class="hr-empty mb-0">조직이 없습니다.</p></div>
								</c:when>
								<c:otherwise>
									<div class="hr-card hr-section">
										<div class="hr-section-head">
											<div>
												<h3 class="hr-section-title">
													<c:out value="${selected.departmentName}" />
													<span class="hr-badge is-blue ms-1">${selected.levelName}</span>
													<c:if test="${!selected.active}"><span class="hr-badge is-gray">사용 안 함</span></c:if>
												</h3>
												<p class="hr-section-sub">
													<c:choose>
														<c:when test="${parent != null}">상위 조직 · <a href="${ctx}/departments?id=${parent.departmentId}" class="hr-link"><c:out value="${parent.departmentName}" /></a></c:when>
														<c:otherwise>최상위 조직</c:otherwise>
													</c:choose>
												</p>
											</div>
											<div class="d-flex gap-2">
												<c:if test="${selected.departmentLevel < 3 && selected.active}">
													<sec:authorize access="hasAuthority('PERMISSION_DEPT_CREATE')">
														<a href="${ctx}/departments/new?parentId=${selected.departmentId}" class="btn btn-sm btn-outline-secondary text-nowrap"><i class="bi bi-plus-lg"></i> 하위 조직</a>
													</sec:authorize>
												</c:if>
												<sec:authorize access="hasAuthority('PERMISSION_DEPT_UPDATE')">
													<a href="${ctx}/departments/${selected.departmentId}/edit" class="btn btn-sm btn-outline-secondary"><i class="bi bi-pencil"></i> 수정</a>
												</sec:authorize>
												<c:if test="${selected.departmentLevel > 1 && selected.active}">
													<sec:authorize access="hasAuthority('PERMISSION_DEPT_DELETE')">
														<button type="button" class="btn btn-sm btn-outline-danger" data-bs-toggle="modal" data-bs-target="#delete-modal"><i class="bi bi-trash"></i> 삭제</button>
													</sec:authorize>
												</c:if>
											</div>
										</div>

										<div class="row g-3 mb-1">
											<div class="col-sm-6 col-xl-3"><div class="hr-mini"><span>부서 코드</span><b><c:out value="${selected.departmentCode}" /></b></div></div>
											<div class="col-sm-6 col-xl-3">
												<div class="hr-mini"><span>조직장 (${selected.managerTitle})</span>
													<b><c:out value="${empty selected.managerName ? '미지정' : selected.managerName}" /></b>
												</div>
											</div>
											<div class="col-sm-6 col-xl-3"><div class="hr-mini"><span>직속 인원</span><b>${selected.memberCount}명</b></div></div>
											<div class="col-sm-6 col-xl-3"><div class="hr-mini"><span>하위 포함 인원</span><b>${selected.totalCount}명</b></div></div>
										</div>

										<c:if test="${not empty children}">
											<div class="mt-3">
												<div class="small text-secondary mb-2">하위 조직 ${fn:length(children)}개</div>
												<div class="hr-chips">
													<c:forEach var="ch" items="${children}">
														<a href="${ctx}/departments?id=${ch.departmentId}${includeInactive ? '&all=1' : ''}" class="hr-chip ${ch.active ? '' : 'is-off'}">
															<c:out value="${ch.departmentName}" /> <span>${ch.totalCount}명</span>
														</a>
													</c:forEach>
												</div>
											</div>
										</c:if>
									</div>

									<!-- 소속 사원 -->
									<div class="hr-card hr-section mt-3">
										<div class="hr-section-head">
											<div>
												<h3 class="hr-section-title">소속 사원 <span class="hr-count">(${fn:length(members)})</span></h3>
												<p class="hr-section-sub">이 조직에 직접 소속된 재직자 · 조직장 먼저, 직급 높은 순</p>
											</div>
										</div>
										<c:choose>
											<c:when test="${empty members}">
												<p class="hr-empty mb-0">직속 재직자가 없습니다.${not empty children ? ' 하위 조직을 선택해 보세요.' : ''}</p>
											</c:when>
											<c:otherwise>
												<div class="table-responsive">
													<table class="table hr-table align-middle mb-0">
														<thead><tr><th>사번</th><th>이름</th><th>직급</th><th>직책</th><th>연락처</th><th>이메일</th><th>입사일</th></tr></thead>
														<tbody>
															<c:forEach var="e" items="${members}">
																<tr>
																	<td class="hr-time text-secondary"><c:out value="${e.empNo}" /></td>
																	<td class="hr-name"><a href="${ctx}/employees/${e.empId}" class="hr-link"><c:out value="${e.ename}" /></a></td>
																	<td><c:out value="${e.job.jobName}" /></td>
																	<td><c:if test="${e.positionName != null}"><span class="hr-badge is-blue"><c:out value="${e.positionName}" /></span></c:if></td>
																	<td class="hr-time"><c:out value="${empty e.phone ? '-' : e.phone}" /></td>
																	<td><c:out value="${empty e.email ? '-' : e.email}" /></td>
																	<td class="hr-time">${e.hiredate}</td>
																</tr>
															</c:forEach>
														</tbody>
													</table>
												</div>
											</c:otherwise>
										</c:choose>
										<p class="hr-help">
											<i class="bi bi-info-circle text-primary"></i> 이름을 누르면 사원 상세로 갑니다(내 조회 범위 안의 사원만). 소속을 옮기려면 사원 수정에서 부서를 바꾸세요.
										</p>
									</div>
								</c:otherwise>
							</c:choose>
						</div>
					</div>
				</div>
			</div>
		</main>

	</div>

	<!-- P-04 삭제 확인 -->
	<c:if test="${selected != null && selected.departmentLevel > 1}">
	<sec:authorize access="hasAuthority('PERMISSION_DEPT_DELETE')">
	<div class="modal fade hr-modal" id="delete-modal" tabindex="-1" aria-labelledby="delete-title" aria-hidden="true">
		<div class="modal-dialog modal-dialog-centered" style="max-width: 420px">
			<div class="modal-content">
				<div class="modal-body p-4">
					<button type="button" class="btn-close float-end" data-bs-dismiss="modal" aria-label="닫기"></button>
					<div class="text-center">
						<span class="hr-modal-icon"><i class="bi bi-trash"></i></span>
						<h3 class="h5 fw-bold mt-3 mb-1" id="delete-title">조직 삭제</h3>
						<p class="text-secondary small mb-0"><b><c:out value="${selected.departmentName}" /></b> (<c:out value="${selected.departmentCode}" />)</p>
					</div>
					<div class="hr-modal-warn mt-3">
						소속 재직자나 사용 중인 하위 조직이 있으면 삭제할 수 없습니다.
						퇴직자 · 인사 이력 등 사용 기록이 있는 조직은 지우지 않고 '사용 안 함'으로 바뀝니다.
					</div>
					<div class="d-flex gap-2 mt-4">
						<button type="button" class="btn btn-outline-secondary flex-fill" data-bs-dismiss="modal">닫기</button>
						<button type="button" class="btn btn-danger flex-fill" id="bt-delete">삭제</button>
					</div>
				</div>
			</div>
		</div>
	</div>
	</sec:authorize>
	</c:if>

	<span id="flash-message" data-message="<c:out value='${message}' />" hidden></span>

	<%@ include file="/WEB-INF/views/common/scripts.jsp"%>
	<script>
		const ctx = "${ctx}";
		const flash = document.getElementById("flash-message").dataset.message;
		if (flash) {
			hrToast(flash, "success");
		}

		const btDelete = document.getElementById("bt-delete");
		if (btDelete) {
			btDelete.addEventListener("click", async () => {
				btDelete.disabled = true;
				try {
					const response = await fetch(ctx + "/api/departments/${selected.departmentId}", { method: "DELETE" });
					if (response.redirected) {
						location.href = response.url;
						return;
					}
					let result;
					try {
						result = await response.json();
					} catch (e) {
						result = { message: response.status === 403 ? "부서 삭제 권한이 없습니다." : "요청을 처리하지 못했습니다." };
					}
					if (response.ok) {
						bootstrap.Modal.getInstance(document.getElementById("delete-modal")).hide();
						hrToast(result.message, "success");
						setTimeout(() => location.href = ctx + "/departments" + (result.deleted ? "" : "?id=${selected.departmentId}&all=1"), 1200);
						return;
					}
					hrToast(result.message, "danger");
				} catch (e) {
					console.log("부서 삭제 실패", e);
					hrToast("서버와 통신할 수 없습니다. 잠시 후 다시 시도해 주세요.", "danger");
				}
				btDelete.disabled = false;
			});
		}
	</script>
</body>
</html>
