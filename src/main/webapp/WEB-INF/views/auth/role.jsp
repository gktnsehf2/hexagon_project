<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="menu" value="roles" />
<!DOCTYPE html>
<html lang="ko">
<head>
<%@ include file="/WEB-INF/views/common/head.jsp"%>
<title>Role 관리 | 인사관리 시스템</title>
<meta name="_csrf" content="${_csrf.token}">
<meta name="_csrf_header" content="${_csrf.headerName}">
<style>
/* S-24 전용 스타일: 기존 AdminLTE/공통 JSP에는 영향을 주지 않도록 범위 제한 */
.role-page {
	--role-primary: #3c68e7;
	--role-text: #26364e;
	--role-muted: #8796ad;
	--role-border: #e0e7f2;
	color: var(--role-text);
	padding-bottom: 36px;
}

.role-page * {
	box-sizing: border-box;
}

.role-page [hidden] {
	display: none !important;
}

.role-page button {
	cursor: pointer;
}

.role-page button:disabled {
	opacity: .55;
	cursor: not-allowed;
}

.role-page .role-breadcrumb {
	font-size: 13px;
	color: #8a98ac;
	margin-bottom: 8px;
}

.role-page .role-heading {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 15px;
	margin-bottom: 23px;
}

.role-page .role-heading h1 {
	margin: 0 0 7px;
	font-size: 29px;
	line-height: 1.25;
	font-weight: 800;
	letter-spacing: -.7px;
}

.role-page .role-heading p {
	margin: 0;
	color: var(--role-muted);
	font-size: 13px;
}

.role-page .role-permission-badge {
	background: #eaf1ff;
	padding: 10px 14px;
	border-radius: 9px;
	color: #3466d5;
	font-size: 12px;
	font-weight: 750;
	white-space: nowrap;
}

.role-page .role-layout {
	display: grid;
	grid-template-columns: minmax(270px, 360px) minmax(0, 1fr);
	gap: 20px;
	align-items: start;
}

.role-page .role-card {
	background: #fff;
	border: 1px solid var(--role-border);
	border-radius: 14px;
	padding: 24px 22px;
	min-width: 0;
}

.role-page .role-list-head {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 7px;
	margin-bottom: 18px;
}

.role-page .role-list-head h2 {
	font-size: 19px;
	font-weight: 800;
	margin: 0;
}

.role-page .role-count {
	display: inline-block;
	vertical-align: middle;
	margin-left: 8px;
	background: #ebf1ff;
	color: #3e68e0;
	border-radius: 25px;
	padding: 4px 9px;
	font-size: 12px;
}

.role-page .role-main-btn {
	border: 1px solid #3c68e7;
	background: #3c68e7;
	color: #fff;
	font-weight: 750;
	border-radius: 8px;
	padding: 9px 14px;
	font-size: 13px;
}

.role-page .role-main-btn:hover {
	background: #3158c7;
}

.role-page .role-outline-btn {
	background: #fff;
	color: #60718c;
	border: 1px solid #d7e1ee;
	border-radius: 8px;
	padding: 9px 13px;
	font-weight: 700;
	font-size: 13px;
}

.role-page .role-danger-btn {
	background: #fff;
	color: #d95454;
	border: 1px solid #ffc6c6;
	border-radius: 8px;
	padding: 9px 13px;
	font-weight: 700;
	font-size: 13px;
}

.role-page .role-warning-btn {
	background: #fff;
	color: #24775d;
	border: 1px solid #b8e5d4;
	border-radius: 8px;
	padding: 9px 13px;
	font-weight: 700;
	font-size: 13px;
}

.role-page .role-items {
	display: flex;
	flex-direction: column;
	gap: 8px;
}

.role-page .role-item {
	width: 100%;
	text-align: left;
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 12px;
	border: 1px solid var(--role-border);
	background: #fff;
	border-radius: 9px;
	padding: 13px 12px;
}

.role-page .role-item:hover {
	border-color: #b7cdfa;
	background: #f9fbff;
}

.role-page .role-item.active {
	background: #eaf2ff;
	border-color: #bed2ff;
}

.role-page .role-item.inactive {
	opacity: .75;
}

.role-page .role-item-title {
	display: block;
	color: #25354c;
	font-size: 14px;
	font-weight: 800;
}

.role-page .role-item-code {
	display: block;
	color: var(--role-muted);
	font-size: 11px;
	margin-top: 4px;
	word-break: break-all;
}

.role-page .role-item-meta {
	flex-shrink: 0;
	display: flex;
	flex-direction: column;
	align-items: flex-end;
	gap: 4px;
	color: #667991;
	font-size: 11px;
}

.role-page .role-item-meta b {
	color: #405879;
	font-size: 12px;
}

.role-page .role-note {
	display: flex;
	gap: 7px;
	margin-top: 18px;
	color: #8797ac;
	font-size: 12px;
	line-height: 1.5;
}

.role-page .role-detail-head {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	flex-wrap: wrap;
	gap: 12px;
	margin-bottom: 20px;
}

.role-page .role-detail-head h2 {
	font-size: 21px;
	font-weight: 800;
	margin: 0 0 7px;
	line-height: 1.2;
}

.role-page .role-code-badge {
	display: inline-block;
	background: #eef2f7;
	padding: 5px 8px;
	color: #647890;
	font-size: 12px;
	border-radius: 6px;
	font-weight: 700;
	vertical-align: middle;
	word-break: break-all;
}

.role-page .role-detail-sub {
	color: #8796aa;
	font-size: 12px;
	margin: 0;
}

.role-page .role-detail-actions {
	display: flex;
	gap: 8px;
	flex-wrap: wrap;
}

.role-page .role-editor-fields {
	display: grid;
	grid-template-columns: 1fr 1fr;
	gap: 13px;
	margin: 0 0 23px;
}

.role-page .role-field label {
	display: block;
	font-size: 12px;
	color: #596c83;
	font-weight: 700;
	margin-bottom: 6px;
}

.role-page .role-field input {
	width: 100%;
	background: #fff;
	border: 1px solid #d9e2ef;
	border-radius: 8px;
	min-height: 39px;
	padding: 9px 11px;
	color: #26364e;
	font-size: 13px;
}

.role-page .role-field input[readonly] {
	background: #f5f7fa;
	color: #8190a5;
}

.role-page .role-field input:focus {
	outline: 2px solid #c4d7ff;
	outline-offset: 0;
}

.role-page .role-field-help {
	color: #96a2b3;
	font-size: 11px;
	margin: 5px 0 0;
}

.role-page .role-section-title {
	display: flex;
	align-items: center;
	flex-wrap: wrap;
	gap: 10px;
	margin-bottom: 11px;
}

.role-page .role-section-title h3 {
	font-size: 15px;
	font-weight: 800;
	margin: 0;
}

.role-page .role-selected-counter {
	padding: 4px 11px;
	border-radius: 20px;
	background: #e9efff;
	color: #3c68e7;
	font-size: 12px;
	font-weight: 800;
}

.role-page .role-hint {
	color: #93a0b3;
	font-size: 12px;
}

.role-page .role-perm-group {
	display: grid;
	grid-template-columns: 62px minmax(0, 1fr);
	gap: 12px;
	border-bottom: 1px solid #eaf0f6;
	padding: 13px 0;
}

.role-page .role-perm-group:first-child {
	padding-top: 4px;
}

.role-page .role-group-title {
	font-size: 13px;
	font-weight: 800;
	color: #354967;
}

.role-page .role-group-sub {
	font-size: 11px;
	color: #93a3b8;
	padding-top: 3px;
}

.role-page .role-checks {
	display: flex;
	flex-wrap: wrap;
	align-items: center;
	gap: 7px;
}

.role-page .role-check {
	display: inline-flex;
	align-items: center;
	gap: 5px;
	cursor: pointer;
	background: #fff;
	border: 1px solid #dce5f1;
	border-radius: 6px;
	padding: 5px 8px;
	font-size: 12px;
	color: #71839d;
	line-height: 1.25;
	max-width: 100%;
	margin: 0;
	font-weight: 500;
}

.role-page .role-check:has(input:checked) {
	border-color: #b7cdff;
	background: #ecf2ff;
	color: #315fd6;
	font-weight: 800;
}

.role-page .role-check input {
	margin: 0;
	accent-color: #3b69e7;
	cursor: pointer;
	flex-shrink: 0;
}

.role-page .role-check span {
	overflow-wrap: anywhere;
}

.role-page .role-state {
	display: inline-block;
	border-radius: 20px;
	padding: 4px 8px;
	background: #e7f7f0;
	color: #178366;
	font-size: 11px;
	font-weight: 750;
}

.role-page .role-state.off {
	background: #fff0ee;
	color: #cc5b53;
}

.role-page .role-status {
	margin: 14px 0 0;
	min-height: 19px;
	font-size: 12px;
	color: #7787a0;
}

.role-page .role-status.error {
	color: #cd4949;
}

.role-page .role-empty {
	color: #8a97a9;
	text-align: center;
	padding: 35px 10px;
	font-size: 13px;
}

.role-page .role-utility {
	margin-top: 12px;
	display: flex;
	justify-content: flex-end;
	gap: 7px;
}

@media ( max-width :1050px) {
	.role-page .role-layout {
		grid-template-columns: 300px minmax(0, 1fr);
	}
	.role-page .role-editor-fields {
		grid-template-columns: 1fr;
	}
}

@media ( max-width :830px) {
	.role-page .role-layout {
		grid-template-columns: 1fr;
	}
	.role-page .role-heading {
		align-items: flex-start;
		flex-direction: column;
	}
	.role-page .role-permission-badge {
		white-space: normal;
	}
}

@media ( max-width :530px) {
	.role-page .role-card {
		padding: 18px 13px;
	}
	.role-page .role-perm-group {
		grid-template-columns: 1fr;
		gap: 7px;
	}
	.role-page .role-list-head {
		flex-wrap: wrap;
	}
}
</style>
</head>
<body class="layout-fixed sidebar-expand-lg bg-body-tertiary">
	<div class="app-wrapper">
		<%@ include file="/WEB-INF/views/common/header.jsp"%>
		<%@ include file="/WEB-INF/views/common/sidebar.jsp"%>

		<main class="app-main">
			<div class="app-content">
				<div class="container-fluid py-4 role-page">
					<p class="role-breadcrumb">권한 관리 / Role 관리</p>
					<div class="role-heading">
						<div>
							<h1>Role 관리</h1>
							<p>권한(Permission)을 업무 단위 Role로 묶습니다. 계정에는 Role을 부여합니다 (S-25).</p>
						</div>
						<span class="role-permission-badge">필요 권한 · ROLE_CREATE /
							UPDATE / DELETE</span>
					</div>
					<div class="role-layout">
						<section class="role-card" aria-label="Role 목록">
							<div class="role-list-head">
								<h2>
									Role 목록 <span class="role-count" id="role-total">—</span>
								</h2>
								<button class="role-main-btn" type="button" id="role-add">＋
									Role 등록</button>
							</div>
							<div class="role-items" id="role-items">
								<p class="role-empty">Role을 불러오는 중입니다.</p>
							</div>
							<div class="role-note">
								<span aria-hidden="true">ⓘ</span><span>Role에 연결된 권한은
									Spring Security에서 사용됩니다. 변경 사항은 계정의 인증 정보 갱신 또는 재로그인 후 반영될 수
									있습니다.</span>
							</div>
						</section>

						<section class="role-card" aria-label="Role 상세 및 Permission 관리">
							<div class="role-detail-head">
								<div>
									<h2 id="role-detail-title">Role을 선택해주세요</h2>
									<div id="role-detail-sub" class="role-detail-sub">왼쪽 목록에서
										Role을 선택하면 권한을 편집할 수 있습니다.</div>
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
										<label for="role-code">Role Code</label> <input id="role-code"
											type="text" maxlength="30" autocomplete="off"
											placeholder="ROLE_EXAMPLE">
										<p id="role-code-hint" class="role-field-help">Role 코드는 최초
											등록 후 변경할 수 없습니다.</p>
									</div>
									<div class="role-field">
										<label for="role-name">Role 이름</label> <input id="role-name"
											type="text" maxlength="50" autocomplete="off"
											placeholder="예: 팀 업무 승인자">
									</div>
								</div>
								<div class="role-section-title">
									<h3>포함된 권한</h3>
									<span class="role-selected-counter" id="role-selection-total">0
										/ 0</span> <span class="role-hint">선택한 Permission을 해당 Role에
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
            root.appendChild(makeText('p', '등록된 Role이 없습니다.', 'role-empty'));
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
        if (!permissions.length) root.appendChild(makeText('p', '등록된 Permission이 없습니다.', 'role-empty'));
        updateCounter();
    }

    function showEditor(role) {
        const creating = isCreating;
        byId('role-editor').hidden = false;
        byId('role-actions').hidden = false;
        byId('role-cancel-new').hidden = !creating;
        byId('role-toggle').hidden = creating;
        byId('role-save').textContent = creating ? '등록' : '저장';
        byId('role-code').readOnly = !creating;
        byId('role-code').value = role ? (role.roleCode || '') : '';
        byId('role-name').value = role ? (role.roleName || '') : '';
        byId('role-code-hint').textContent = creating
            ? 'ROLE_로 시작하는 새 Role 코드를 입력하세요. (최대 30자)'
            : 'Role 코드는 최초 등록 후 변경할 수 없습니다.';
        if (creating) {
            byId('role-detail-title').textContent = '신규 Role 등록';
            byId('role-detail-sub').textContent = 'Role 이름과 Permission을 선택한 뒤 등록해주세요.';
        } else {
            byId('role-detail-title').textContent = role.roleName || 'Role 상세';
            const subtitle = byId('role-detail-sub');
            subtitle.replaceChildren();
            subtitle.append(makeText('span', role.roleCode || '', 'role-code-badge'));
            subtitle.append(document.createTextNode('   ·   ' + (Number(role.isActive) === 1 ? '활성 Role' : '비활성 Role')));
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
        setStatus('Role 정보를 불러오는 중입니다.');
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
        if (!Array.isArray(roles)) throw new Error('Role 목록 응답 형식이 올바르지 않습니다.');
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
            setStatus('Role 코드와 이름은 필수 입력 항목입니다.', true);
            return;
        }
        if (isCreating && !/^ROLE_[A-Z0-9_]+$/.test(code)) {
            setStatus('Role 코드는 ROLE_로 시작하며 영문 대문자, 숫자, _만 사용할 수 있습니다.', true);
            return;
        }
        if (code.length > 30 || name.length > 50) {
            setStatus('Role 코드는 최대 30자, 이름은 최대 50자입니다.', true);
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
            setStatus(creating ? 'Role이 등록되었습니다.' : 'Role과 Permission이 저장되었습니다.');
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
            ? '[' + currentRole.roleName + '] Role을 비활성화하시겠습니까?\n계정에 부여된 Role은 비활성화할 수 없습니다.'
            : '[' + currentRole.roleName + '] Role을 재활성화하시겠습니까?';
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
            setStatus(active ? 'Role이 비활성화되었습니다.' : 'Role이 재활성화되었습니다.');
        } catch (error) {
            setStatus(error.message, true);
        } finally {
            busy = false;
            byId('role-toggle').disabled = false;
            byId('role-save').disabled = false;
        }
    }

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
        setStatus('Role 및 Permission 목록을 불러오는 중입니다.');
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
                setStatus('Role이 없습니다. Role 등록 버튼으로 새 Role을 만들어주세요.');
            }
        } catch (error) {
            setStatus(error.message, true);
            byId('role-items').replaceChildren(makeText('p', 'Role 목록을 불러오지 못했습니다.', 'role-empty'));
        }
    })();
})();
</script>
</body>
</html>
