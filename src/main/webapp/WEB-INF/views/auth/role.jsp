<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<c:set var="menuGroup" value="auth" />
<c:set var="menu" value="roles" />
<!DOCTYPE html>
<html lang="ko">
<head>
<%@ include file="/WEB-INF/views/common/head.jsp"%>
<title>권한 그룹 관리 | HexagonGMI 인사관리</title>
<meta name="_csrf" content="${_csrf.token}">
<meta name="_csrf_header" content="${_csrf.headerName}">
</head>
<body class="layout-fixed sidebar-expand-lg hr-app">
	<div class="app-wrapper">
		<%@ include file="/WEB-INF/views/common/header.jsp"%>
		<%@ include file="/WEB-INF/views/common/sidebar.jsp"%>

		<main class="app-main">
			<div class="app-content">
				<div class="container-fluid py-4 role-page">
					<p class="role-breadcrumb">권한 관리 / 권한 그룹 관리</p>
					<div class="role-heading">
						<div>
							<h1>권한 그룹 관리</h1>
							<p>권한을 업무 단위로 묶어 권한 그룹을 만듭니다. 만든 그룹은 계정 권한 부여에서 계정에 줍니다.</p>
						</div>
						<span class="role-permission-badge">필요 권한 · ROLE_CREATE /
							UPDATE / DELETE</span>
					</div>
					<div class="role-layout">
						<section class="role-card" aria-label="권한 그룹 목록">
							<div class="role-list-head">
								<h2>
									권한 그룹 목록 <span class="role-count" id="role-total">—</span>
								</h2>
								<button class="role-main-btn" type="button" id="role-add">＋
									권한 그룹 등록</button>
							</div>
							<div class="role-items" id="role-items">
								<p class="role-empty">권한 그룹을 불러오는 중입니다.</p>
							</div>
							<div class="role-note">
								<span aria-hidden="true">ⓘ</span><span>권한 그룹에 연결된 권한은
									Spring Security에서 사용됩니다. 변경 사항은 계정의 인증 정보 갱신 또는 재로그인 후 반영될 수
									있습니다.</span>
							</div>
						</section>

						<section class="role-card" aria-label="권한 그룹 상세 및 권한 관리">
							<div class="role-detail-head">
								<div>
									<h2 id="role-detail-title">권한 그룹을 선택해주세요</h2>
									<div id="role-detail-sub" class="role-detail-sub">왼쪽 목록에서
										권한 그룹을 선택하면 권한을 편집할 수 있습니다.</div>
								</div>
								<div class="role-detail-actions" id="role-actions" hidden>
									<button type="button" id="role-cancel-new"
										class="role-outline-btn" hidden>취소</button>
									<button type="button" id="role-toggle" class="role-danger-btn"
										hidden>비활성화</button>
									<button type="button" id="role-save" class="role-main-btn">저장</button>
								</div>
							</div>
							<div id="role-editor" hidden>
								<div class="role-editor-fields">
									<div class="role-field">
										<label for="role-code">그룹 코드</label> <input id="role-code"
											type="text" maxlength="30" autocomplete="off"
											placeholder="ROLE_EXAMPLE">
										<p id="role-code-hint" class="role-field-help">권한 그룹 코드는 최초
											등록 후 변경할 수 없습니다.</p>
									</div>
									<div class="role-field">
										<label for="role-name">권한 그룹 이름</label> <input id="role-name"
											type="text" maxlength="50" autocomplete="off"
											placeholder="예: 팀 업무 승인자">
									</div>
								</div>
								<div class="role-section-title">
									<h3>포함된 권한</h3>
									<span class="role-selected-counter" id="role-selection-total">0
										/ 0</span> <span class="role-hint">선택한 권한을 해당 권한 그룹에
										연결합니다.</span>
								</div>
								<div id="role-permission-groups"></div>
								<div class="role-utility">
									<button class="role-outline-btn" type="button"
										id="role-select-all">전체 선택</button>
									<button class="role-outline-btn" type="button"
										id="role-clear-all">전체 해제</button>
								</div>
							</div>
							<p id="role-status" class="role-status" aria-live="polite"></p>
						</section>
					</div>
				</div>
			</div>
		</main>
	</div>
	<%@ include file="/WEB-INF/views/common/scripts.jsp"%>
	<script>
(function () {
    'use strict';

    const CONTEXT = '${pageContext.request.contextPath}';
    // 버튼은 권한이 있을 때만 (서버에서도 SecurityConfig 로 막음)
    const CAN_CREATE = <sec:authorize access="hasAuthority('PERMISSION_ROLE_CREATE')" var="canCreate"/>${canCreate};
    const CAN_UPDATE = <sec:authorize access="hasAuthority('PERMISSION_ROLE_UPDATE')" var="canUpdate"/>${canUpdate};
    const CAN_DELETE = <sec:authorize access="hasAuthority('PERMISSION_ROLE_DELETE')" var="canDelete"/>${canDelete};
    const ROLE_API = CONTEXT + '/api/roles';
    const PERMISSION_API = CONTEXT + '/api/permissions';
    const GROUPS = [
        {key:'HR', label:'인사'},
        {key:'ATTENDANCE', label:'근태'},
        {key:'LEAVE', label:'휴가'},
        {key:'AUTH', label:'권한'},
        {key:'SECURITY', label:'보안'}
    ];
    const byId = (id) => document.getElementById(id);

    let roles = [];
    let permissions = [];
    let selectedRoleId = null;
    let currentRole = null;
    let isCreating = false;
    let busy = false;
    let requestSerial = 0;
    let initialSnapshot = '';

    function csrfHeaders() {
        const token = document.querySelector('meta[name="_csrf"]')?.content;
        const headerName = document.querySelector('meta[name="_csrf_header"]')?.content;
        return token && headerName ? {[headerName]:token} : {};
    }

    async function apiFetch(url, options = {}) {
        const response = await fetch(url, {credentials:'same-origin', ...options});
        if (!response.ok) {
            let message = '요청에 실패했습니다. (HTTP ' + response.status + ')';
            try {
                const result = await response.json();
                if (result && result.message) message = result.message;
            } catch (_) { /* JSON이 아닌 오류 응답 */ }
            throw new Error(message);
        }
        if (response.status === 204) return null;
        return response.json();
    }

    function setStatus(text, isError = false) {
        const status = byId('role-status');
        status.textContent = text || '';
        status.classList.toggle('error', Boolean(isError));
    }

    function selectedPermissions() {
        return Array.from(byId('role-permission-groups').querySelectorAll('input[type="checkbox"]:checked'))
            .map((checkbox) => Number(checkbox.value))
            .sort((a, b) => a - b);
    }

    function editorSnapshot() {
        return JSON.stringify({
            code: byId('role-code').value.trim(),
            name: byId('role-name').value.trim(),
            ids: selectedPermissions()
        });
    }

    function confirmLeaving() {
        return !initialSnapshot || initialSnapshot === editorSnapshot()
            || window.confirm('저장되지 않은 변경 사항이 있습니다. 이동하시겠습니까?');
    }

    function makeText(tag, text, className) {
        const element = document.createElement(tag);
        if (className) element.className = className;
        element.textContent = text;
        return element;
    }

    function renderRoleList() {
        byId('role-total').textContent = String(roles.length);
        const root = byId('role-items');
        root.replaceChildren();
        if (!roles.length) {
            root.appendChild(makeText('p', '등록된 권한 그룹이 없습니다.', 'role-empty'));
            return;
        }
        roles.forEach((role) => {
            const active = Number(role.isActive) === 1;
            const item = document.createElement('button');
            item.type = 'button';
            item.className = 'role-item' + (role.hrRoleId === selectedRoleId ? ' active' : '') + (!active ? ' inactive' : '');
            const text = document.createElement('span');
            text.appendChild(makeText('span', role.roleName || '(이름 없음)', 'role-item-title'));
            text.appendChild(makeText('span', role.roleCode || '', 'role-item-code'));
            const meta = document.createElement('span');
            meta.className = 'role-item-meta';
            meta.appendChild(makeText('b', '권한 ' + (Array.isArray(role.permissions) ? role.permissions.length : 0)));
            const badge = makeText('span', active ? '활성' : '비활성', 'role-state' + (active ? '' : ' off'));
            meta.appendChild(badge);
            item.append(text, meta);
            item.addEventListener('click', async () => {
                if (busy || (selectedRoleId === role.hrRoleId && !isCreating)) return;
                if (!confirmLeaving()) return;
                await openRole(role.hrRoleId);
            });
            root.appendChild(item);
        });
    }

    function updateCounter() {
        const chosen = new Set(selectedPermissions());
        byId('role-selection-total').textContent = chosen.size + ' / ' + permissions.length;
        GROUPS.forEach(({key}) => {
            const node = byId('role-group-count-' + key);
            if (!node) return;
            const groupPerms = permissions.filter((permission) => permission.permissionGroup === key);
            node.textContent = groupPerms.filter((permission) => chosen.has(permission.permissionId)).length + ' / ' + groupPerms.length;
        });
    }

    function renderPermissionGroups(checkedIds) {
        const chosen = new Set(checkedIds || []);
        const root = byId('role-permission-groups');
        root.replaceChildren();
        const extras = Array.from(new Set(permissions.map((p) => p.permissionGroup).filter((group) => !GROUPS.some((g) => g.key === group))))
            .map((group) => ({key:group, label:group || '기타'}));
        [...GROUPS, ...extras].forEach(({key, label}) => {
            const data = permissions.filter((p) => p.permissionGroup === key);
            if (!data.length) return;
            const section = makeText('div', '', 'role-perm-group');
            const title = document.createElement('div');
            title.appendChild(makeText('div', label, 'role-group-title'));
            const count = makeText('div', '', 'role-group-sub');
            count.id = 'role-group-count-' + key;
            title.appendChild(count);
            const checks = document.createElement('div');
            checks.className = 'role-checks';
            data.forEach((permission) => {
                const input = document.createElement('input');
                input.type = 'checkbox';
                input.value = String(permission.permissionId);
                input.checked = chosen.has(permission.permissionId);
                input.addEventListener('change', updateCounter);
                const caption = permission.shortCode || String(permission.permissionCode || '').replace(/^PERMISSION_/, '');
                const labelNode = makeText('label', '', 'role-check');
                labelNode.title = (permission.permissionCode || '') + ' — ' + (permission.permissionName || '');
                labelNode.append(input, makeText('span', caption));
                checks.appendChild(labelNode);
            });
            section.append(title, checks);
            root.appendChild(section);
        });
        if (!permissions.length) root.appendChild(makeText('p', '등록된 권한이 없습니다.', 'role-empty'));
        updateCounter();
    }

    function showEditor(role) {
        const creating = isCreating;
        byId('role-editor').hidden = false;
        byId('role-actions').hidden = false;
        byId('role-cancel-new').hidden = !creating;
        byId('role-toggle').hidden = creating || !CAN_DELETE;
        byId('role-save').hidden = creating ? !CAN_CREATE : !CAN_UPDATE;
        byId('role-select-all').disabled = byId('role-save').hidden;
        byId('role-clear-all').disabled = byId('role-save').hidden;
        byId('role-save').textContent = creating ? '등록' : '저장';
        byId('role-code').readOnly = !creating;
        byId('role-code').value = role ? (role.roleCode || '') : '';
        byId('role-name').value = role ? (role.roleName || '') : '';
        byId('role-code-hint').textContent = creating
            ? 'ROLE_로 시작하는 새 권한 그룹 코드를 입력하세요. (최대 30자)'
            : '권한 그룹 코드는 최초 등록 후 변경할 수 없습니다.';
        if (creating) {
            byId('role-detail-title').textContent = '신규 권한 그룹 등록';
            byId('role-detail-sub').textContent = '권한 그룹 이름과 권한을 선택한 뒤 등록해주세요.';
        } else {
            byId('role-detail-title').textContent = role.roleName || '권한 그룹 상세';
            const subtitle = byId('role-detail-sub');
            subtitle.replaceChildren();
            subtitle.append(makeText('span', role.roleCode || '', 'role-code-badge'));
            subtitle.append(document.createTextNode('   ·   ' + (Number(role.isActive) === 1 ? '활성 권한 그룹' : '비활성 권한 그룹')));
            byId('role-toggle').textContent = Number(role.isActive) === 1 ? '비활성화' : '재활성화';
            byId('role-toggle').className = Number(role.isActive) === 1 ? 'role-danger-btn' : 'role-warning-btn';
        }
        renderPermissionGroups(role && role.permissions ? role.permissions.map((p) => p.permissionId) : []);
        initialSnapshot = editorSnapshot();
        setStatus('');
        renderRoleList();
        if (creating) byId('role-code').focus();
    }

    async function openRole(id) {
        const sequence = ++requestSerial;
        setStatus('권한 그룹 정보를 불러오는 중입니다.');
        try {
            const role = await apiFetch(ROLE_API + '/' + encodeURIComponent(id));
            if (sequence !== requestSerial) return;
            selectedRoleId = role.hrRoleId;
            currentRole = role;
            isCreating = false;
            showEditor(role);
        } catch (error) {
            setStatus(error.message, true);
        }
    }

    function startCreate() {
        if (busy || !confirmLeaving()) return;
        ++requestSerial;
        selectedRoleId = null;
        currentRole = null;
        isCreating = true;
        showEditor(null);
    }

    async function reloadRoles(preferredId) {
        roles = await apiFetch(ROLE_API);
        if (!Array.isArray(roles)) throw new Error('권한 그룹 목록 응답 형식이 올바르지 않습니다.');
        renderRoleList();
        const id = preferredId != null ? preferredId : (roles[0] ? roles[0].hrRoleId : null);
        if (id != null) await openRole(id);
        else {
            selectedRoleId = null;
            currentRole = null;
            isCreating = false;
            byId('role-editor').hidden = true;
            byId('role-actions').hidden = true;
            initialSnapshot = '';
        }
    }

    async function saveRole() {
        if (busy) return;
        const code = byId('role-code').value.trim().toUpperCase();
        const name = byId('role-name').value.trim();
        const permissionIds = selectedPermissions();
        if (!name || (isCreating && !code)) {
            setStatus('권한 그룹 코드와 이름은 필수 입력 항목입니다.', true);
            return;
        }
        if (isCreating && !/^ROLE_[A-Z0-9_]+$/.test(code)) {
            setStatus('권한 그룹 코드는 ROLE_로 시작하며 영문 대문자, 숫자, _만 사용할 수 있습니다.', true);
            return;
        }
        if (code.length > 30 || name.length > 50) {
            setStatus('권한 그룹 코드는 최대 30자, 이름은 최대 50자입니다.', true);
            return;
        }
        const creating = isCreating;
        const id = selectedRoleId;
        const data = {roleCode:code, roleName:name, permissionIds};
        const url = creating ? ROLE_API : ROLE_API + '/' + encodeURIComponent(id);
        busy = true;
        byId('role-save').disabled = true;
        byId('role-toggle').disabled = true;
        setStatus(creating ? '등록 중...' : '저장 중...');
        try {
            const result = await apiFetch(url, {
                method:creating ? 'POST' : 'PUT',
                headers:{'Content-Type':'application/json; charset=UTF-8', ...csrfHeaders()},
                body:JSON.stringify(data)
            });
            const nextId = creating ? (result && result.hrRoleId) : id;
            initialSnapshot = '';  // 저장 완료 뒤 새 항목 선택 시 확인창 방지
            await reloadRoles(nextId);
            setStatus(creating ? '권한 그룹이 등록되었습니다.' : '권한 그룹과 권한이 저장되었습니다.');
        } catch (error) {
            setStatus(error.message, true);
        } finally {
            busy = false;
            byId('role-save').disabled = false;
            byId('role-toggle').disabled = false;
        }
    }

    async function toggleRole() {
        if (busy || !currentRole || isCreating) return;
        const active = Number(currentRole.isActive) === 1;
        if (!confirmLeaving()) return;
        const message = active
            ? '[' + currentRole.roleName + '] 권한 그룹을 비활성화하시겠습니까?\n계정에 부여된 권한 그룹은 비활성화할 수 없습니다.'
            : '[' + currentRole.roleName + '] 권한 그룹을 재활성화하시겠습니까?';
        if (!window.confirm(message)) return;
        busy = true;
        byId('role-toggle').disabled = true;
        byId('role-save').disabled = true;
        setStatus(active ? '비활성화 처리 중...' : '재활성화 처리 중...');
        try {
            await apiFetch(ROLE_API + '/' + encodeURIComponent(currentRole.hrRoleId) + '/' + (active ? 'deactivate' : 'activate'), {
                method:'PATCH', headers:csrfHeaders()
            });
            initialSnapshot = '';
            await reloadRoles(currentRole.hrRoleId);
            setStatus(active ? '권한 그룹이 비활성화되었습니다.' : '권한 그룹이 재활성화되었습니다.');
        } catch (error) {
            setStatus(error.message, true);
        } finally {
            busy = false;
            byId('role-toggle').disabled = false;
            byId('role-save').disabled = false;
        }
    }

    byId('role-add').hidden = !CAN_CREATE;
    byId('role-add').addEventListener('click', startCreate);
    byId('role-save').addEventListener('click', saveRole);
    byId('role-toggle').addEventListener('click', toggleRole);
    byId('role-cancel-new').addEventListener('click', () => {
        if (busy || !confirmLeaving()) return;
        initialSnapshot = '';
        reloadRoles(roles[0] ? roles[0].hrRoleId : null).catch((error) => setStatus(error.message, true));
    });
    byId('role-select-all').addEventListener('click', () => {
        byId('role-permission-groups').querySelectorAll('input[type="checkbox"]').forEach((input) => {input.checked = true;});
        updateCounter();
    });
    byId('role-clear-all').addEventListener('click', () => {
        byId('role-permission-groups').querySelectorAll('input[type="checkbox"]').forEach((input) => {input.checked = false;});
        updateCounter();
    });
    byId('role-code').addEventListener('input', () => {
        if (isCreating) byId('role-code').value = byId('role-code').value.toUpperCase();
    });

    (async function initialize() {
        setStatus('권한 그룹 및 권한 목록을 불러오는 중입니다.');
        try {
            const results = await Promise.all([apiFetch(ROLE_API), apiFetch(PERMISSION_API)]);
            roles = results[0];
            permissions = results[1];
            if (!Array.isArray(roles) || !Array.isArray(permissions)) {
                throw new Error('API 응답 형식이 올바르지 않습니다.');
            }
            renderRoleList();
            if (roles.length) await openRole(roles[0].hrRoleId);
            else {
                setStatus('권한 그룹이 없습니다. 권한 그룹 등록 버튼으로 새 권한 그룹을 만들어주세요.');
            }
        } catch (error) {
            setStatus(error.message, true);
            byId('role-items').replaceChildren(makeText('p', '권한 그룹 목록을 불러오지 못했습니다.', 'role-empty'));
        }
    })();
})();
</script>
</body>
</html>
