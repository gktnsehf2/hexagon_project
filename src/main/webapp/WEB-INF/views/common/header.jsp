<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!-- 상단 헤더: 사이드바 열기/닫기 | 로그인한 사용자 (이름 + 직급) + 로그아웃 -->
<nav class="app-header navbar navbar-expand hr-header" data-bs-theme="dark">
	<div class="container-fluid">

		<ul class="navbar-nav">
			<li class="nav-item">
				<a class="nav-link" href="#" data-lte-toggle="sidebar" role="button" aria-label="사이드바 열기/닫기">
					<i class="bi bi-list fs-5"></i>
				</a>
			</li>
		</ul>

		<!-- principal = 로그인할 때 만든 AccountDetails -->
		<div class="ms-auto d-flex align-items-center gap-3">
			<%-- 이름을 누르면 내 정보 (권한이 없으면 글자만) --%>
			<sec:authorize access="hasAuthority('PERMISSION_MY_INFO_READ')" var="canMyInfo" />
			<c:set var="userLabel">
				<i class="bi bi-person-circle"></i>
				<span>
					<sec:authentication property="principal.account.emp.ename" />
					<small>(<sec:authentication property="principal.account.emp.job.jobName" />)</small>
				</span>
			</c:set>
			<c:choose>
				<c:when test="${canMyInfo}"><a class="hr-user hr-user-link" href="${ctx}/me" title="내 정보">${userLabel}</a></c:when>
				<c:otherwise><span class="hr-user">${userLabel}</span></c:otherwise>
			</c:choose>
			<form action="${ctx}/logout" method="post" class="m-0">
				<button type="submit" class="hr-logout">로그아웃</button>
			</form>
		</div>

	</div>
</nav>
