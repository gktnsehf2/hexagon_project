<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="sec"
	uri="http://www.springframework.org/security/tags"%>
<%-- S-19 내 정보: 본인 인사 정보 · 계정 정보 · 보유 Role 과 권한 (조회만) --%>
<c:set var="menuGroup" value="hr" />
<c:set var="menu" value="me" />
<!DOCTYPE html>
<html lang="ko">
<head>
<%@ include file="/WEB-INF/views/common/head.jsp"%>
<title>내 정보 | HexagonGMI 인사관리</title>
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
							<h2 class="hr-page-title">내 정보</h2>
							<p class="hr-page-sub">내 인사 정보와 계정 · 보유 Role을 확인합니다.</p>
						</div>
						<div class="col-md-6">
							<ol class="breadcrumb justify-content-md-end mb-2">
								<li class="breadcrumb-item"><a href="${ctx}/dashboard">홈</a></li>
								<li class="breadcrumb-item">인사 관리</li>
								<li class="breadcrumb-item active" aria-current="page">내 정보</li>
							</ol>
						</div>
					</div>
				</div>
			</div>

			<div class="app-content">
				<div class="container-fluid">
					<c:set var="dept" value="${emp.department}" />

					<div class="row g-3 mt-1">
						<!-- 프로필 -->
						<div class="col-lg-4">
							<div class="hr-card hr-profile h-100">
								<span class="hr-avatar"> <c:choose>
										<c:when test="${not empty emp.photoPath}">
											<img src="${ctx}/employees/${emp.empId}/photo"
												alt="<c:out value='${emp.ename}' /> 증명사진">
										</c:when>
										<c:otherwise>
											<c:out value="${fn:substring(emp.ename, 0, 1)}" />
										</c:otherwise>
									</c:choose>
								</span>
								<h3 class="hr-profile-name">
									<c:out value="${emp.ename}" />
								</h3>
								<div class="hr-profile-sub">
									<c:out value="${dept.pathName}" />
									·
									<c:out value="${emp.job.jobName}" />
									<c:if test="${emp.positionName != null}"> · <c:out
											value="${emp.positionName}" />
									</c:if>
								</div>
								<div class="mt-2">
									<span class="hr-badge ${emp.active ? 'is-green' : 'is-gray'}">${emp.statusName}</span>
								</div>
								<dl class="hr-dl">
									<dt>사번</dt>
									<dd>
										<c:out value="${emp.empNo}" />
									</dd>
									<dt>입사일</dt>
									<dd>${emp.hiredate}</dd>
									<dt>근속</dt>
									<dd>${tenure}</dd>
									<dt>올해 연차</dt>
									<dd>
										<c:choose>
											<c:when test="${annualLeave != null}">
												잔여 <b><fmt:formatNumber
														value="${annualLeave.remainDays}" pattern="0.#" />일</b>
												<span class="text-secondary small">(부여 <fmt:formatNumber
														value="${annualLeave.grantedDays}" pattern="0.#" /> · 사용
													<fmt:formatNumber value="${annualLeave.usedDays}"
														pattern="0.#" />)
												</span>
											</c:when>
											<c:otherwise>
												<span class="text-secondary">부여 내역 없음</span>
											</c:otherwise>
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
									<div class="d-flex gap-2">
										<sec:authorize access="hasAuthority('PERMISSION_MY_ATT_READ')">
											<a href="${ctx}/attendance/my"
												class="btn btn-sm btn-outline-secondary"><i
												class="bi bi-clock-history"></i> 내 근태 보기</a>
										</sec:authorize>
										<sec:authorize
											access="hasAuthority('PERMISSION_MY_LEAVE_READ')">
											<a href="${ctx}/leaves/my"
												class="btn btn-sm btn-outline-secondary"><i
												class="bi bi-calendar-check"></i> 내 휴가 내역</a>
										</sec:authorize>
									</div>
								</div>
								<div class="row g-0 column-gap-lg-5">
									<dl class="hr-dl col-lg">
										<dt>이름</dt>
										<dd>
											<c:out value="${emp.ename}" />
										</dd>
										<dt>성별</dt>
										<dd>${emp.genderName}</dd>
										<dt>이메일</dt>
										<dd>
											<c:out value="${empty emp.email ? '-' : emp.email}" />
										</dd>
										<dt>연락처</dt>
										<dd>
											<c:out value="${empty emp.phone ? '-' : emp.phone}" />
										</dd>
									</dl>
									<dl class="hr-dl col-lg">
										<dt>소속</dt>
										<dd>
											<c:out value="${dept.pathName}" />
										</dd>
										<dt>직급</dt>
										<dd>
											<c:out value="${emp.job.jobName}" />
										</dd>
										<dt>직책</dt>
										<dd>
											<c:out
												value="${emp.positionName != null ? emp.positionName : '-'}" />
										</dd>
										<dt>재직 상태</dt>
										<dd>
											<span class="hr-badge ${emp.active ? 'is-green' : 'is-gray'}">${emp.statusName}</span>
										</dd>
									</dl>
								</div>
								<p class="hr-help">
									<i class="bi bi-info-circle text-primary"></i> 인사 정보 변경은 인사팀에
									요청하세요. (사원 수정은 인사 담당자만 할 수 있습니다)
								</p>
							</div>
						</div>

						<!-- 계정 정보 -->
						<div class="col-lg-5">
							<div class="hr-card hr-section h-100">
								<div class="hr-section-head">
									<div>
										<h3 class="hr-section-title">계정 정보</h3>
										<p class="hr-section-sub">로그인 계정 상태</p>
									</div>
								</div>
								<c:choose>
									<c:when test="${account != null}">
										<dl class="hr-dl">
											<dt>로그인 아이디</dt>
											<dd>
												<c:out value="${account.loginId}" />
											</dd>
											<dt>계정 상태</dt>
											<dd>
												<span
													class="hr-badge ${account.statusName == '사용' ? 'is-green' : (account.statusName == '잠김' ? 'is-orange' : 'is-gray')}">${account.statusName}</span>
											</dd>
											<dt>마지막 로그인</dt>
											<dd>${account.lastLoginText}</dd>
											<dt>로그인 실패</dt>
											<dd>
												<span
													class="${account.failCount > 0 ? 'text-danger fw-semibold' : ''}">${account.failCount}</span>
												/ ${maxFailCount}회
											</dd>
											<dt>계정 발급일</dt>
											<dd>${account.createdText}</dd>
										</dl>
										<p class="hr-help">
											<i class="bi bi-info-circle text-primary"></i> 비밀번호를 잊었거나 계정이
											잠기면 인사팀에 초기화·잠금 해제를 요청하세요.
										</p>
									</c:when>
									<c:otherwise>
										<p class="hr-empty mb-0">발급된 계정이 없습니다.</p>
									</c:otherwise>
								</c:choose>
							</div>
						</div>

						<!-- 보유 Role · 권한 -->
						<div class="col-lg-7">
							<div class="hr-card hr-section h-100">
								<div class="hr-section-head">
									<div>
										<h3 class="hr-section-title">
											보유 권한<span class="hr-badge is-blue ms-1">${fn:length(account.roles)}</span>
										</h3>
									</div>
								</div>

								<c:forEach var="role" items="${account.roles}">
									<div class="hr-role-item">
										<div class="hr-role-head">
											<span class="hr-badge is-blue"
												title="<c:out value='${role.roleCode}' />"><c:out
													value="${role.roleName}" /></span> <span
												class="text-secondary small ms-auto">권한
												${fn:length(role.permissions)}개</span>
										</div>
										<div class="hr-roles">
											<c:forEach var="p" items="${role.permissions}">
												<span class="hr-perm"
													title="<c:out value='${p.groupName}' /> · <c:out value='${p.shortCode}' />"><c:out
														value="${p.permissionName}" /></span>
											</c:forEach>
										</div>
									</div>
								</c:forEach>
								<c:if test="${empty account.roles}">
									<p class="hr-empty mb-0">부여된 권한이 없습니다.</p>
								</c:if>
							</div>
						</div>
					</div>

				</div>
			</div>
		</main>

	</div>

	<%@ include file="/WEB-INF/views/common/scripts.jsp"%>
</body>
</html>
