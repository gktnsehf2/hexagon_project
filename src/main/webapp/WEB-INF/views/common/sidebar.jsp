<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%-- 왼쪽 사이드바
     sec:authorize 로 권한이 있는 메뉴만 보여준다 (Role 이 아니라 권한 코드로 검사)
     그룹은 안쪽 메뉴 중 하나라도 권한이 있으면 보인다
     각 화면에서 지정하는 값
       menuGroup : 펼쳐 둘 그룹 (hr / dept / att / leave / auth)
       menu      : 선택 표시할 메뉴 (예: employees)
     아직 만들지 않은 화면은 is-soon + data-soon (누르면 "준비 중" 안내) --%>
<aside class="app-sidebar hr-sidebar" data-bs-theme="dark">

	<div class="sidebar-brand">
		<%-- 로고(회사명)를 누르면 대시보드로 (대시보드 메뉴는 따로 두지 않음) --%>
		<a href="${ctx}/dashboard" class="brand-link" title="대시보드">
			<span class="hr-brand-mark"><i class="bi bi-buildings"></i></span>
			<span class="brand-text">HexagonGMI</span>
		</a>
	</div>

	<div class="sidebar-wrapper">
		<nav>
			<ul class="nav sidebar-menu flex-column" role="menu" data-lte-toggle="treeview" data-accordion="false">

				<li class="nav-header">MAIN NAVIGATION</li>

				<%-- 인사 관리 --%>
				<sec:authorize access="hasAnyAuthority('PERMISSION_MY_INFO_READ','PERMISSION_EMP_READ_TEAM','PERMISSION_EMP_READ_DEPT','PERMISSION_EMP_READ_ALL','PERMISSION_EMP_CREATE')">
				<li class="nav-item ${menuGroup == 'hr' ? 'menu-open' : ''}">
					<a href="#" class="nav-link ${menuGroup == 'hr' ? 'active' : ''}">
						<i class="nav-icon bi bi-people"></i><p>인사 관리<i class="nav-arrow bi bi-chevron-right"></i></p>
					</a>
					<ul class="nav nav-treeview">
						<sec:authorize access="hasAuthority('PERMISSION_MY_INFO_READ')">
						<li class="nav-item"><a href="${ctx}/me" class="nav-link ${menu == 'me' ? 'active' : ''}"><i class="nav-icon bi bi-dot"></i><p>내 정보</p></a></li>
						</sec:authorize>
						<sec:authorize access="hasAnyAuthority('PERMISSION_EMP_READ_TEAM','PERMISSION_EMP_READ_DEPT','PERMISSION_EMP_READ_ALL')">
						<li class="nav-item"><a href="${ctx}/employees" class="nav-link ${menu == 'employees' ? 'active' : ''}"><i class="nav-icon bi bi-dot"></i><p>직원 목록</p></a></li>
						</sec:authorize>
						<sec:authorize access="hasAuthority('PERMISSION_EMP_CREATE')">
						<li class="nav-item"><a href="${ctx}/employees/new" class="nav-link ${menu == 'empNew' ? 'active' : ''}"><i class="nav-icon bi bi-dot"></i><p>사원 등록</p></a></li>
						</sec:authorize>
					</ul>
				</li>
				</sec:authorize>

				<%-- 부서 관리 --%>
				<sec:authorize access="hasAnyAuthority('PERMISSION_EMP_READ_TEAM','PERMISSION_EMP_READ_DEPT','PERMISSION_EMP_READ_ALL','PERMISSION_DEPT_CREATE','PERMISSION_DEPT_UPDATE','PERMISSION_DEPT_DELETE')">
				<li class="nav-item ${menuGroup == 'dept' ? 'menu-open' : ''}">
					<a href="#" class="nav-link ${menuGroup == 'dept' ? 'active' : ''}">
						<i class="nav-icon bi bi-diagram-3"></i><p>부서 관리<i class="nav-arrow bi bi-chevron-right"></i></p>
					</a>
					<ul class="nav nav-treeview">
						<sec:authorize access="hasAnyAuthority('PERMISSION_EMP_READ_TEAM','PERMISSION_EMP_READ_DEPT','PERMISSION_EMP_READ_ALL')">
						<li class="nav-item"><a href="#" class="nav-link is-soon" data-soon><i class="nav-icon bi bi-dot"></i><p>조직도</p></a></li>
						</sec:authorize>
						<sec:authorize access="hasAnyAuthority('PERMISSION_DEPT_CREATE','PERMISSION_DEPT_UPDATE','PERMISSION_DEPT_DELETE')">
						<li class="nav-item"><a href="#" class="nav-link is-soon" data-soon><i class="nav-icon bi bi-dot"></i><p>부서 등록·수정</p></a></li>
						</sec:authorize>
					</ul>
				</li>
				</sec:authorize>

				<%-- 근태 관리 --%>
				<sec:authorize access="hasAnyAuthority('PERMISSION_MY_ATT_READ','PERMISSION_ATT_READ_TEAM','PERMISSION_ATT_READ_DEPT','PERMISSION_ATT_READ_ALL','PERMISSION_ATT_CORRECT_REQUEST','PERMISSION_ATT_CORRECT_APPROVE')">
				<li class="nav-item ${menuGroup == 'att' ? 'menu-open' : ''}">
					<a href="#" class="nav-link ${menuGroup == 'att' ? 'active' : ''}">
						<i class="nav-icon bi bi-clock-history"></i><p>근태 관리<i class="nav-arrow bi bi-chevron-right"></i></p>
					</a>
					<ul class="nav nav-treeview">
						<sec:authorize access="hasAuthority('PERMISSION_MY_ATT_READ')">
						<li class="nav-item"><a href="${ctx}/attendance/my" class="nav-link ${menu == 'myAtt' ? 'active' : ''}"><i class="nav-icon bi bi-dot"></i><p>내 근태</p></a></li>
						</sec:authorize>
						<sec:authorize access="hasAnyAuthority('PERMISSION_ATT_READ_TEAM','PERMISSION_ATT_READ_DEPT','PERMISSION_ATT_READ_ALL')">
						<li class="nav-item"><a href="#" class="nav-link is-soon" data-soon><i class="nav-icon bi bi-dot"></i><p>근태 현황</p></a></li>
						</sec:authorize>
						<sec:authorize access="hasAuthority('PERMISSION_ATT_CORRECT_REQUEST')">
						<li class="nav-item"><a href="#" class="nav-link is-soon" data-soon><i class="nav-icon bi bi-dot"></i><p>근태 정정 신청</p></a></li>
						</sec:authorize>
						<sec:authorize access="hasAuthority('PERMISSION_ATT_CORRECT_APPROVE')">
						<li class="nav-item"><a href="#" class="nav-link is-soon" data-soon><i class="nav-icon bi bi-dot"></i><p>근태 결재</p></a></li>
						</sec:authorize>
					</ul>
				</li>
				</sec:authorize>

				<%-- 휴가 관리 --%>
				<sec:authorize access="hasAnyAuthority('PERMISSION_LEAVE_REQUEST','PERMISSION_MY_LEAVE_READ','PERMISSION_LEAVE_APPROVE','PERMISSION_LEAVE_READ_ALL')">
				<li class="nav-item ${menuGroup == 'leave' ? 'menu-open' : ''}">
					<a href="#" class="nav-link ${menuGroup == 'leave' ? 'active' : ''}">
						<i class="nav-icon bi bi-calendar-check"></i><p>휴가 관리<i class="nav-arrow bi bi-chevron-right"></i></p>
					</a>
					<ul class="nav nav-treeview">
						<sec:authorize access="hasAuthority('PERMISSION_LEAVE_REQUEST')">
						<li class="nav-item"><a href="#" class="nav-link is-soon" data-soon><i class="nav-icon bi bi-dot"></i><p>휴가 신청</p></a></li>
						</sec:authorize>
						<sec:authorize access="hasAuthority('PERMISSION_MY_LEAVE_READ')">
						<li class="nav-item"><a href="${ctx}/leaves/my" class="nav-link ${menu == 'myLeave' ? 'active' : ''}"><i class="nav-icon bi bi-dot"></i><p>내 휴가</p></a></li>
						</sec:authorize>
						<sec:authorize access="hasAuthority('PERMISSION_LEAVE_APPROVE')">
						<li class="nav-item"><a href="#" class="nav-link is-soon" data-soon><i class="nav-icon bi bi-dot"></i><p>휴가 결재</p></a></li>
						</sec:authorize>
						<sec:authorize access="hasAuthority('PERMISSION_LEAVE_READ_ALL')">
						<li class="nav-item"><a href="#" class="nav-link is-soon" data-soon><i class="nav-icon bi bi-dot"></i><p>전체 휴가 현황</p></a></li>
						</sec:authorize>
					</ul>
				</li>
				</sec:authorize>

				<%-- 권한 관리 (권한 → Role 묶음 → 계정에 부여) --%>
				<sec:authorize access="hasAnyAuthority('PERMISSION_PERM_CREATE','PERMISSION_PERM_UPDATE','PERMISSION_PERM_DELETE','PERMISSION_ROLE_CREATE','PERMISSION_ROLE_UPDATE','PERMISSION_ROLE_DELETE','PERMISSION_ROLE_ASSIGN','PERMISSION_ACCOUNT_UNLOCK','PERMISSION_ACCOUNT_PASSWORD_RESET','PERMISSION_ACCOUNT_ENABLE')">
				<li class="nav-item ${menuGroup == 'auth' ? 'menu-open' : ''}">
					<a href="#" class="nav-link ${menuGroup == 'auth' ? 'active' : ''}">
						<i class="nav-icon bi bi-shield-lock"></i><p>권한 관리<i class="nav-arrow bi bi-chevron-right"></i></p>
					</a>
					<ul class="nav nav-treeview">
						<sec:authorize access="hasAnyAuthority('PERMISSION_PERM_CREATE','PERMISSION_PERM_UPDATE','PERMISSION_PERM_DELETE')">
						<li class="nav-item"><a href="#" class="nav-link is-soon" data-soon><i class="nav-icon bi bi-dot"></i><p>권한 관리</p></a></li>
						</sec:authorize>
						<sec:authorize access="hasAnyAuthority('PERMISSION_ROLE_CREATE','PERMISSION_ROLE_UPDATE','PERMISSION_ROLE_DELETE')">
						<li class="nav-item"><a href="#" class="nav-link is-soon" data-soon><i class="nav-icon bi bi-dot"></i><p>Role 관리</p></a></li>
						</sec:authorize>
						<sec:authorize access="hasAuthority('PERMISSION_ROLE_ASSIGN')">
						<li class="nav-item"><a href="#" class="nav-link is-soon" data-soon><i class="nav-icon bi bi-dot"></i><p>계정 Role 부여</p></a></li>
						</sec:authorize>
						<sec:authorize access="hasAnyAuthority('PERMISSION_ACCOUNT_UNLOCK','PERMISSION_ACCOUNT_PASSWORD_RESET','PERMISSION_ACCOUNT_ENABLE')">
						<li class="nav-item"><a href="#" class="nav-link is-soon" data-soon><i class="nav-icon bi bi-dot"></i><p>계정 관리</p></a></li>
						</sec:authorize>
					</ul>
				</li>
				</sec:authorize>

			</ul>
		</nav>
	</div>

</aside>
