<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%-- S-04 사원 등록 / 수정 (같은 화면을 함께 씀)
     수정 모드: 컨트롤러가 emp 를 담아 줌 → 입사일·부서·직급·계정은 바꿀 수 없어 읽기 전용으로 보여줌
     form:form + form:errors : 검사(@Valid)에 걸리면 입력값과 오류 문구를 그대로 다시 보여줌 --%>
<c:set var="isEdit" value="${not empty emp}" />
<c:set var="menuGroup" value="hr" />
<c:set var="menu" value="${isEdit ? 'employees' : 'empNew'}" />
<c:set var="pageName" value="${isEdit ? '사원 정보 수정' : '사원 등록'}" />
<!DOCTYPE html>
<html lang="ko">
<head>
<%@ include file="/WEB-INF/views/common/head.jsp"%>
<title>${pageName} | 규성컴퍼니 인사관리</title>
</head>

<body class="layout-fixed sidebar-expand-lg hr-app">
	<div class="app-wrapper">

		<%@ include file="/WEB-INF/views/common/header.jsp"%>
		<%@ include file="/WEB-INF/views/common/sidebar.jsp"%>

		<main class="app-main">
			<div class="app-content-header">
				<div class="container-fluid">
					<div class="row align-items-end">
						<div class="col-sm-8">
							<h2 class="hr-page-title">${pageName}</h2>
							<p class="hr-page-sub">
								<c:choose>
									<c:when test="${isEdit}"><c:out value="${emp.ename}" /> (<c:out value="${emp.empNo}" />) 사원의 기본 정보를 수정합니다.</c:when>
									<c:otherwise>새로운 사원의 기본 정보와 계정 정보를 입력하세요.</c:otherwise>
								</c:choose>
							</p>
						</div>
						<div class="col-sm-4">
							<ol class="breadcrumb float-sm-end mb-0">
								<li class="breadcrumb-item"><a href="${ctx}/dashboard">홈</a></li>
								<li class="breadcrumb-item">인사 관리</li>
								<li class="breadcrumb-item active" aria-current="page">${pageName}</li>
							</ol>
						</div>
					</div>
				</div>
			</div>

			<div class="app-content">
				<div class="container-fluid">
					<c:choose>
						<c:when test="${isEdit}">
							<c:url var="actionUrl" value="/employees/${emp.empId}/edit" />
							<c:url var="cancelUrl" value="/employees/${emp.empId}" />
						</c:when>
						<c:otherwise>
							<c:url var="actionUrl" value="/employees/new" />
							<c:url var="cancelUrl" value="/employees" />
						</c:otherwise>
					</c:choose>

					<%-- 파일을 보내려면 enctype="multipart/form-data" --%>
					<form:form modelAttribute="form" action="${actionUrl}" method="post" enctype="multipart/form-data"
							   cssClass="hr-card hr-form mt-3" novalidate="novalidate" id="emp-form">

						<div class="hr-section">
							<div class="hr-section-head">
								<div>
									<h3 class="hr-section-title">기본 정보</h3>
									<p class="hr-section-sub">사원의 인적 사항 및 소속 정보를 ${isEdit ? '수정' : '등록'}합니다. <span class="hr-req">*</span> 필수 입력</p>
								</div>
							</div>

							<form:errors path="*" element="div">
								<div class="alert alert-danger py-2 small">
									<i class="bi bi-exclamation-circle"></i> 입력값을 확인해 주세요. 사진을 골랐다면 다시 선택해야 합니다.
								</div>
							</form:errors>

							<div class="row g-4">
								<!-- 왼쪽: 입력 항목 -->
								<div class="col-lg-8">
									<div class="row g-3">
										<div class="col-md-6">
											<label for="ename" class="form-label">이름<span class="hr-req">*</span></label>
											<form:input path="ename" cssClass="form-control" cssErrorClass="form-control is-invalid" maxlength="30" placeholder="이름 입력" />
											<form:errors path="ename" cssClass="invalid-feedback" />
										</div>
										<div class="col-md-6">
											<label for="email" class="form-label">이메일</label>
											<form:input path="email" type="email" cssClass="form-control" cssErrorClass="form-control is-invalid" maxlength="100" placeholder="example@company.com" />
											<form:errors path="email" cssClass="invalid-feedback" />
										</div>

										<div class="col-md-6">
											<span class="form-label d-block">성별<span class="hr-req">*</span></span>
											<div class="hr-gender">
												<div class="form-check">
													<form:radiobutton path="gender" value="M" id="gender-m" cssClass="form-check-input" />
													<label class="form-check-label" for="gender-m">남</label>
												</div>
												<div class="form-check">
													<form:radiobutton path="gender" value="F" id="gender-f" cssClass="form-check-input" />
													<label class="form-check-label" for="gender-f">여</label>
												</div>
											</div>
											<form:errors path="gender" cssClass="invalid-feedback d-block" />
										</div>
										<div class="col-md-6">
											<label for="phone" class="form-label">연락처</label>
											<form:input path="phone" cssClass="form-control" cssErrorClass="form-control is-invalid" maxlength="13" placeholder="010-0000-0000" />
											<form:errors path="phone" cssClass="invalid-feedback" />
										</div>

										<c:choose>
											<%-- 수정: 입사일·부서·직급·계정은 읽기 전용 (name 이 없어 서버로 보내지지 않음) --%>
											<c:when test="${isEdit}">
												<div class="col-md-6">
													<label class="form-label">입사일</label>
													<input type="text" class="form-control" value="${emp.hiredate}" disabled>
												</div>
												<div class="col-md-6">
													<label class="form-label">부서</label>
													<input type="text" class="form-control" value="<c:out value='${emp.department.pathName}' />" disabled>
												</div>
												<div class="col-md-6">
													<label class="form-label">직급</label>
													<input type="text" class="form-control" value="<c:out value='${emp.job.jobName}' />" disabled>
												</div>
												<div class="col-md-6">
													<label class="form-label">직책</label>
													<input type="text" class="form-control" value="<c:out value='${emp.positionName != null ? emp.positionName : "-"}' />" disabled>
												</div>
											</c:when>

											<c:otherwise>
												<div class="col-md-6">
													<label for="hiredate" class="form-label">입사일<span class="hr-req">*</span></label>
													<form:input path="hiredate" type="date" cssClass="form-control" cssErrorClass="form-control is-invalid" />
													<form:errors path="hiredate" cssClass="invalid-feedback" />
												</div>
												<div class="col-md-6"></div>
												<div class="col-md-6">
													<label for="departmentId" class="form-label">부서<span class="hr-req">*</span></label>
													<form:select path="departmentId" cssClass="form-select" cssErrorClass="form-select is-invalid">
														<option value="">부서 선택</option>
														<c:forEach var="d" items="${departments}">
															<option value="${d.departmentId}" ${form.departmentId == d.departmentId ? 'selected' : ''}><c:out value="${d.indentName}" /></option>
														</c:forEach>
													</form:select>
													<form:errors path="departmentId" cssClass="invalid-feedback" />
												</div>
												<div class="col-md-6">
													<label for="jobId" class="form-label">직급<span class="hr-req">*</span></label>
													<form:select path="jobId" cssClass="form-select" cssErrorClass="form-select is-invalid">
														<option value="">직급 선택</option>
														<c:forEach var="j" items="${jobs}">
															<option value="${j.jobId}" ${form.jobId == j.jobId ? 'selected' : ''}><c:out value="${j.jobName}" /></option>
														</c:forEach>
													</form:select>
													<form:errors path="jobId" cssClass="invalid-feedback" />
												</div>
												<div class="col-md-6">
													<label for="loginId" class="form-label">로그인 아이디<span class="hr-req">*</span></label>
													<form:input path="loginId" cssClass="form-control" cssErrorClass="form-control is-invalid" maxlength="25" placeholder="영문 소문자·숫자 4~25자" autocomplete="off" />
													<form:errors path="loginId" cssClass="invalid-feedback" />
												</div>
												<div class="col-md-6">
													<label for="password" class="form-label">초기 비밀번호<span class="hr-req">*</span></label>
													<%-- 비밀번호는 오류로 돌아와도 다시 채우지 않음 (form:password 기본 동작) --%>
													<form:password path="password" cssClass="form-control" cssErrorClass="form-control is-invalid" maxlength="20" placeholder="4~20자" autocomplete="new-password" />
													<form:errors path="password" cssClass="invalid-feedback" />
												</div>
											</c:otherwise>
										</c:choose>
									</div>
								</div>

								<!-- 오른쪽: 증명사진 -->
								<div class="col-lg-4">
									<div class="hr-photo-box">
										<h4>증명사진</h4>
										<div class="hr-photo-frame" id="photo-frame">
											<c:choose>
												<c:when test="${isEdit and not empty emp.photoPath}">
													<img src="${ctx}/employees/${emp.empId}/photo" alt="현재 증명사진" id="photo-preview">
												</c:when>
												<c:otherwise>
													<i class="bi bi-person-fill" id="photo-empty"></i>
													<img alt="선택한 증명사진" id="photo-preview" hidden>
												</c:otherwise>
											</c:choose>
										</div>
										<input type="file" name="photo" id="photo-input" accept="image/jpeg,image/png" hidden>
										<button type="button" class="btn btn-outline-primary btn-sm px-3" id="bt-photo">
											<i class="bi bi-plus-lg"></i> 사진 ${isEdit and not empty emp.photoPath ? '변경' : '첨부'}
										</button>
										<form:errors path="photo" cssClass="invalid-feedback d-block" />
										<div class="hr-photo-help">
											3 × 4 비율 JPG / PNG, 2MB 이하<br>권장 크기 300 × 400px
											<c:if test="${isEdit}"><br>선택하지 않으면 지금 사진을 그대로 씁니다.</c:if>
										</div>
									</div>
								</div>
							</div>
						</div>

						<div class="hr-form-foot">
							<span>
								<i class="bi bi-info-circle text-primary"></i>
								<c:choose>
									<c:when test="${isEdit}">소속·직급은 인사팀에서, 아이디·비밀번호는 계정 관리에서 변경합니다.</c:when>
									<c:otherwise>저장하면 계정 발급, 기본 Role(일반 사원) 부여, 올해 연차 부여, 입사 발령 이력이 함께 기록됩니다.</c:otherwise>
								</c:choose>
							</span>
							<span class="d-flex gap-2 ms-auto">
								<a href="${cancelUrl}" class="btn btn-outline-secondary">취소</a>
								<button type="submit" class="btn btn-primary" id="bt-save">저장</button>
							</span>
						</div>
					</form:form>

				</div>
			</div>
		</main>

	</div>

	<%@ include file="/WEB-INF/views/common/scripts.jsp"%>
	<script>
		// 증명사진: 버튼 → 파일 선택 → 미리보기 (서버에서도 한 번 더 검사함)
		const MAX_PHOTO = 2 * 1024 * 1024;
		const photoInput = document.getElementById("photo-input");
		const preview = document.getElementById("photo-preview");
		const empty = document.getElementById("photo-empty");

		document.getElementById("bt-photo").addEventListener("click", () => photoInput.click());

		photoInput.addEventListener("change", () => {
			const file = photoInput.files[0];
			if (!file) {
				return;
			}
			if (file.type !== "image/jpeg" && file.type !== "image/png") {
				hrToast("JPG 또는 PNG 파일만 올릴 수 있습니다.", "danger");
				photoInput.value = "";
				return;
			}
			if (file.size > MAX_PHOTO) {
				hrToast("사진은 2MB 이하만 올릴 수 있습니다.", "danger");
				photoInput.value = "";
				return;
			}
			preview.src = URL.createObjectURL(file);
			preview.hidden = false;
			if (empty) {
				empty.hidden = true;
			}
		});

		// 두 번 눌러 두 번 등록되지 않도록
		document.getElementById("emp-form").addEventListener("submit", () => {
			document.getElementById("bt-save").disabled = true;
		});
	</script>
</body>
</html>
