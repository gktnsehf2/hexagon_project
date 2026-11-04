<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%-- S-07 부서 등록 / 수정: 코드 · 이름 · 상위 조직(단계 자동) · 조직장(수정) · 사용 여부(수정) --%>
<c:set var="isEdit" value="${dept != null}" />
<c:set var="menuGroup" value="dept" />
<c:set var="menu" value="${isEdit ? 'departments' : 'deptNew'}" />
<c:set var="pageName" value="${isEdit ? '부서 수정' : '부서 등록'}" />
<!DOCTYPE html>
<html lang="ko">
<head>
<%@ include file="/WEB-INF/views/common/head.jsp"%>
<title>${pageName} | HexagonGMI 인사관리</title>
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
							<h2 class="hr-page-title">${pageName}</h2>
							<p class="hr-page-sub">
								<c:choose>
									<c:when test="${isEdit}"><c:out value="${dept.departmentName}" /> (<c:out value="${dept.departmentCode}" />) 조직 정보를 수정합니다.</c:when>
									<c:otherwise>새 부 · 팀을 만듭니다. 단계는 상위 조직에 따라 정해집니다.</c:otherwise>
								</c:choose>
							</p>
						</div>
						<div class="col-md-6">
							<ol class="breadcrumb justify-content-md-end mb-2">
								<li class="breadcrumb-item"><a href="${ctx}/dashboard">홈</a></li>
								<li class="breadcrumb-item"><a href="${ctx}/departments">조직도</a></li>
								<li class="breadcrumb-item active" aria-current="page">${pageName}</li>
							</ol>
						</div>
					</div>
				</div>
			</div>

			<div class="app-content">
				<div class="container-fluid">
					<c:choose>
						<c:when test="${isEdit}"><c:url var="actionUrl" value="/departments/${dept.departmentId}/edit" /></c:when>
						<c:otherwise><c:url var="actionUrl" value="/departments/new" /></c:otherwise>
					</c:choose>
					<c:url var="cancelUrl" value="/departments">
						<c:if test="${isEdit}"><c:param name="id" value="${dept.departmentId}" /></c:if>
					</c:url>
					<form action="${actionUrl}" method="post" class="hr-card hr-form mt-1" id="dept-form" style="max-width: 860px">
						<div class="hr-section">
							<div class="hr-section-head">
								<div>
									<h3 class="hr-section-title">조직 정보</h3>
									<p class="hr-section-sub"><span class="hr-req">*</span> 필수 입력</p>
								</div>
							</div>

							<c:if test="${not empty formError}">
								<div class="alert alert-danger py-2 small"><i class="bi bi-exclamation-circle"></i> <c:out value="${formError}" /></div>
							</c:if>

							<div class="row g-3">
								<div class="col-md-6">
									<label for="departmentName" class="form-label">부서명<span class="hr-req">*</span></label>
									<input type="text" id="departmentName" name="departmentName" class="form-control" maxlength="60" required
										   value="<c:out value='${form.departmentName}' />" placeholder="예) 개발3팀">
								</div>
								<div class="col-md-6">
									<label for="departmentCode" class="form-label">부서 코드<span class="hr-req">*</span></label>
									<input type="text" id="departmentCode" name="departmentCode" class="form-control text-uppercase" maxlength="20" required
										   pattern="[A-Za-z0-9_]{2,20}" value="<c:out value='${form.departmentCode}' />" placeholder="예) DEV3_TEAM">
									<div class="form-text">영문 대문자 · 숫자 · _ 2~20자, 다른 조직과 겹치면 안 됩니다.</div>
								</div>

								<div class="col-md-6">
									<label for="parentId" class="form-label">상위 조직<span class="hr-req">*</span></label>
									<c:choose>
										<c:when test="${isEdit && dept.departmentLevel == 1}">
											<input type="text" class="form-control" value="없음 (최상위)" disabled>
										</c:when>
										<c:otherwise>
											<select id="parentId" name="parentId" class="form-select" required>
												<option value="">상위 조직 선택</option>
												<c:forEach var="p" items="${parents}">
													<c:if test="${!isEdit || p.departmentId != dept.departmentId}">
														<option value="${p.departmentId}" data-level="${p.departmentLevel}" ${form.parentId == p.departmentId ? 'selected' : ''}><c:out value="${p.indentName}" /></option>
													</c:if>
												</c:forEach>
											</select>
										</c:otherwise>
									</c:choose>
								</div>
								<div class="col-md-6">
									<span class="form-label d-block">단계</span>
									<div class="hr-current" id="level-text">
										${isEdit && dept.departmentLevel == 1 ? '본사' : '상위 조직을 고르면 정해집니다'}
									</div>
								</div>

								<c:if test="${isEdit}">
									<div class="col-md-6">
										<label for="managerId" class="form-label">조직장 (${dept.managerTitle})</label>
										<select id="managerId" name="managerId" class="form-select">
											<option value="">미지정</option>
											<c:forEach var="m" items="${members}">
												<option value="${m.empId}" ${form.managerId == m.empId ? 'selected' : ''}><c:out value="${m.ename}" /> · <c:out value="${m.job.jobName}" /> (<c:out value="${m.empNo}" />)</option>
											</c:forEach>
										</select>
										<div class="form-text">이 조직의 직속 재직자 중에서 고릅니다${empty members ? ' — 지금은 직속 재직자가 없습니다' : ''}.</div>
									</div>
									<div class="col-md-6">
										<span class="form-label d-block">사용 여부</span>
										<c:choose>
											<c:when test="${dept.departmentLevel == 1}">
												<input type="hidden" name="isActive" value="1">
												<div class="hr-current">사용 (본사는 바꿀 수 없음)</div>
											</c:when>
											<c:otherwise>
												<div class="hr-gender">
													<div class="form-check">
														<input class="form-check-input" type="radio" name="isActive" id="active-1" value="1" ${form.isActive == 1 ? 'checked' : ''}>
														<label class="form-check-label" for="active-1">사용</label>
													</div>
													<div class="form-check">
														<input class="form-check-input" type="radio" name="isActive" id="active-0" value="0" ${form.isActive == 0 ? 'checked' : ''}>
														<label class="form-check-label" for="active-0">사용 안 함</label>
													</div>
												</div>
												<div class="form-text">소속 재직자 · 사용 중인 하위 조직이 없을 때만 '사용 안 함'으로 바꿀 수 있습니다.</div>
											</c:otherwise>
										</c:choose>
									</div>
								</c:if>
							</div>
						</div>

						<div class="hr-form-foot">
							<span>
								<i class="bi bi-info-circle text-primary"></i>
								${isEdit ? '조직장을 바꾸면 직책 표시와 휴가 · 근태 결재자가 바로 바뀝니다 (이미 신청된 건의 결재자는 그대로).' : '조직장은 사원을 이 조직으로 옮긴 뒤 수정에서 지정합니다.'}
							</span>
							<span class="d-flex gap-2 ms-auto">
								<a href="${cancelUrl}" class="btn btn-outline-secondary">취소</a>
								<button type="submit" class="btn btn-primary" id="bt-save">저장</button>
							</span>
						</div>
					</form>
				</div>
			</div>
		</main>

	</div>

	<%@ include file="/WEB-INF/views/common/scripts.jsp"%>
	<script>
		// 상위 조직에 따라 단계 미리 보기 (본사 아래 = 부, 부 아래 = 팀)
		const parentSel = document.getElementById("parentId");
		if (parentSel) {
			const showLevel = () => {
				const opt = parentSel.selectedOptions[0];
				const level = opt && opt.dataset.level ? Number(opt.dataset.level) + 1 : 0;
				document.getElementById("level-text").textContent = level === 2 ? "부" : (level === 3 ? "팀" : "상위 조직을 고르면 정해집니다");
			};
			parentSel.addEventListener("change", showLevel);
			showLevel();
		}
		const codeEl = document.getElementById("departmentCode");
		codeEl.addEventListener("input", () => codeEl.value = codeEl.value.toUpperCase());
		document.getElementById("dept-form").addEventListener("submit", () => {
			document.getElementById("bt-save").disabled = true;
		});
	</script>
</body>
</html>
