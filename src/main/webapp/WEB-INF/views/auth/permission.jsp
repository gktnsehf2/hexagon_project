<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<c:set var="menuGroup" value="auth" />
<c:set var="menu" value="permissions" />
<!DOCTYPE html>
<html lang="ko">
<head>
<%@ include file="/WEB-INF/views/common/head.jsp"%>
<title>권한 관리 | HexagonGMI 인사관리</title>
<meta name="_csrf" content="${_csrf.token}">
<meta name="_csrf_header" content="${_csrf.headerName}">
<style>
.perm-page, .perm-overlay {
	--perm-blue: #3e65e8;
	--perm-line: #e1e8f2;
	--perm-text: #26354b;
	--perm-muted: #8490a3;
}

.perm-page {
	color: var(--perm-text);
	padding-bottom: 28px;
}

.perm-top {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	gap: 18px;
	margin-bottom: 20px;
}

.perm-title {
	font-size: 29px;
	font-weight: 800;
	letter-spacing: -1px;
	margin: 0 0 5px;
}

.perm-subtitle {
	font-size: 14px;
	color: var(--perm-muted);
	margin: 0;
}

.perm-primary {
	background: var(--perm-blue);
	border: 1px solid var(--perm-blue);
	color: white;
	border-radius: 9px;
	padding: 11px 20px;
	font-weight: 700;
	cursor: pointer;
}

.perm-primary:hover {
	background: #3156d5;
	color: white;
}

.perm-primary:disabled, .perm-outline:disabled, .perm-danger:disabled {
	opacity: .6;
	cursor: not-allowed;
}

.perm-stats {
	display: grid;
	grid-template-columns: repeat(3, minmax(0, 225px));
	gap: 18px;
	margin-bottom: 24px;
}

.perm-stat {
	border: 1px solid var(--perm-line);
	background: #fff;
	border-radius: 13px;
	padding: 17px 16px;
	display: flex;
	align-items: center;
	gap: 14px;
	min-height: 90px;
}

.perm-stat-icon {
	width: 45px;
	height: 45px;
	background: #edf2ff;
	color: var(--perm-blue);
	border-radius: 12px;
	display: grid;
	place-items: center;
	font-size: 25px;
	font-weight: 700;
	flex: 0 0 auto;
}

.perm-stat-icon.green {
	background: #eaf9f4;
	color: #18ae89;
}

.perm-stat-icon.purple {
	background: #f3efff;
	color: #8766de;
}

.perm-stat-label {
	font-size: 13px;
	color: var(--perm-muted);
	margin: 0 0 1px;
}

.perm-stat-value {
	font-size: 24px;
	font-weight: 800;
	line-height: 1.2;
	margin: 0;
}

.perm-panel {
	border: 1px solid var(--perm-line);
	background: white;
	border-radius: 14px;
	padding: 21px 22px 13px;
}

.perm-panel-head {
	display: flex;
	justify-content: space-between;
	align-items: center;
	gap: 16px;
	margin-bottom: 17px;
}

.perm-panel-head h2 {
	font-size: 19px;
	font-weight: 800;
	margin: 0 0 4px;
}

.perm-panel-head p {
	font-size: 13px;
	color: var(--perm-muted);
	margin: 0;
}

.perm-search {
	width: 245px;
	max-width: 100%;
	background: #f8faff;
	border: 1px solid var(--perm-line);
	border-radius: 8px;
	padding: 10px 12px;
	outline: none;
	font-size: 13px;
}

.perm-search:focus {
	border-color: var(--perm-blue);
	box-shadow: 0 0 0 3px rgba(62, 101, 232, .12);
}

.perm-table-wrap {
	overflow-x: auto;
}

.perm-table {
	width: 100%;
	border-collapse: collapse;
	font-size: 13px;
	min-width: 770px;
}

.perm-table thead {
	background: #f1f4fa;
	border-top: 3px solid #228cf0;
}

.perm-table th {
	font-weight: 700;
	color: #607089;
	text-align: left;
	padding: 13px 12px;
	white-space: nowrap;
}

.perm-table td {
	padding: 10px 12px;
	border-bottom: 1px solid #edf0f5;
	vertical-align: middle;
}

.perm-table tbody tr:nth-child(even) {
	background: #fafbfe;
}

.perm-table .perm-code {
	color: #234c9b;
	font-weight: 800;
	overflow-wrap: anywhere;
}

.perm-table .perm-name {
	font-weight: 700;
}

.perm-table .perm-desc {
	color: #718096;
}

.perm-module {
	display: inline-block;
	padding: 5px 12px;
	min-width: 62px;
	text-align: center;
	border-radius: 99px;
	background: #f1efff;
	color: #6f61a8;
	font-weight: 700;
	white-space: nowrap;
}

.perm-module.HR {
	background: #e9f0ff;
	color: #4d679f;
}

.perm-module.ATTENDANCE {
	background: #e7f7f2;
	color: #3b917d;
}

.perm-module.LEAVE {
	background: #f1ebff;
	color: #7b60b9;
}

.perm-module.SECURITY {
	background: #fff1e4;
	color: #b2793c;
}

.perm-module.AUTH {
	background: #f0eef7;
	color: #71658d;
}

.perm-actions {
	display: flex;
	align-items: center;
	gap: 7px;
	white-space: nowrap;
}

.perm-outline, .perm-danger {
	border-radius: 7px;
	font-weight: 700;
	font-size: 12px;
	background: white;
	padding: 6px 13px;
	cursor: pointer;
}

.perm-outline {
	border: 1px solid #cbd9ef;
	color: #385173;
}

.perm-outline:hover {
	background: #eff4ff;
}

.perm-danger {
	border: 1px solid #ffc6c6;
	color: #e04a4a;
	background: #fffafa;
}

.perm-danger:hover {
	background: #fff0f0;
}

.perm-empty {
	padding: 38px 15px !important;
	text-align: center;
	color: var(--perm-muted);
}

.perm-note {
	font-size: 12px;
	color: #93a0b1;
	margin: 21px 0 0 15px;
}

.perm-status {
	font-size: 13px;
	min-height: 18px;
	color: var(--perm-muted);
	margin: 10px 0 0;
}

.perm-status.error {
	color: #c83434;
}

.perm-overlay {
	position: fixed;
	inset: 0;
	background: rgba(15, 30, 50, .56);
	z-index: 1090;
	display: none;
	align-items: center;
	justify-content: center;
	padding: 18px;
}

.perm-overlay.open {
	display: flex;
}

.perm-dialog {
	width: min(100%, 470px);
	background: white;
	border-radius: 14px;
	box-shadow: 0 24px 70px rgba(0, 0, 0, .2);
	overflow: hidden;
}

.perm-dialog-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 18px 22px;
	border-bottom: 1px solid var(--perm-line);
}

.perm-dialog-header h2 {
	font-size: 20px;
	font-weight: 800;
	margin: 0;
}

.perm-close {
	border: 0;
	background: none;
	font-size: 28px;
	color: #778398;
	line-height: 1;
	cursor: pointer;
}

.perm-dialog-body {
	padding: 20px 22px 8px;
}

.perm-field {
	margin-bottom: 18px;
}

.perm-field label {
	display: block;
	font-size: 13px;
	font-weight: 700;
	margin-bottom: 7px;
}

.perm-field input, .perm-field select {
	box-sizing: border-box;
	width: 100%;
	border: 1px solid #d9e0ed;
	border-radius: 8px;
	padding: 11px 12px;
	font-size: 14px;
	background: white;
}

.perm-field input:focus, .perm-field select:focus {
	outline: none;
	border-color: var(--perm-blue);
}

.perm-field input[readonly] {
	background: #f2f4f8;
	color: #647084;
	cursor: not-allowed;
}

.perm-help {
	font-size: 12px;
	color: var(--perm-muted);
	margin: 6px 0 0;
}

.perm-form-error {
	min-height: 19px;
	font-size: 13px;
	color: #d53c3c;
	margin: 0 0 10px;
}

.perm-dialog-footer {
	display: flex;
	justify-content: flex-end;
	gap: 9px;
	padding: 14px 22px 20px;
}

.perm-cancel {
	border: 1px solid #d5ddeb;
	background: white;
	color: #4a5870;
	border-radius: 9px;
	padding: 10px 18px;
	cursor: pointer;
}

.perm-toast {
	position: fixed;
	right: 24px;
	bottom: 24px;
	z-index: 1100;
	background: #26354b;
	color: white;
	border-radius: 10px;
	padding: 12px 18px;
	box-shadow: 0 8px 25px rgba(0, 0, 0, .16);
	display: none;
	max-width: 350px;
	font-size: 13px;
}

.perm-toast.show {
	display: block;
}

.perm-toast.error {
	background: #b83c3c;
}

@media ( max-width :900px) {
	.perm-stats {
		grid-template-columns: repeat(3, minmax(0, 1fr));
	}
	.perm-stat {
		padding: 13px;
	}
}

@media ( max-width :700px) {
	.perm-top, .perm-panel-head {
		flex-direction: column;
		align-items: stretch;
	}
	.perm-top .perm-primary {
		align-self: flex-start;
	}
	.perm-search {
		width: 100%;
	}
	.perm-stats {
		grid-template-columns: 1fr;
		gap: 10px;
	}
	.perm-panel {
		padding: 15px 12px;
	}
	.perm-title {
		font-size: 25px;
	}
}
</style>
</head>
<body class="layout-fixed sidebar-expand-lg hr-app">
	<div class="app-wrapper">
		<%@ include file="/WEB-INF/views/common/header.jsp"%>
		<%@ include file="/WEB-INF/views/common/sidebar.jsp"%>

		<main class="app-main">
			<div class="app-content">
				<div class="container-fluid py-4 perm-page">
					<div class="perm-top">
						<div>
							<h1 class="perm-title">권한 목록</h1>
							<p class="perm-subtitle">시스템 접근 권한을 확인하고 관리합니다.</p>
						</div>
						<sec:authorize access="hasAuthority('PERMISSION_PERM_CREATE')">
						<button id="perm-add-btn" type="button" class="perm-primary">＋
							권한 등록</button>
						</sec:authorize>
					</div>

					<section class="perm-stats" aria-label="권한 통계">
						<div class="perm-stat">
							<div class="perm-stat-icon" aria-hidden="true">◇</div>
							<div>
								<p class="perm-stat-label">등록된 권한</p>
								<p class="perm-stat-value">
									<span id="perm-count">—</span>개
								</p>
							</div>
						</div>
						<div class="perm-stat">
							<div class="perm-stat-icon green" aria-hidden="true">◇</div>
							<div>
								<p class="perm-stat-label">관리 모듈</p>
								<p class="perm-stat-value">
									<span id="perm-group-count">—</span>개
								</p>
							</div>
						</div>
						<div class="perm-stat">
							<div class="perm-stat-icon purple" aria-hidden="true">◇</div>
							<div>
								<p class="perm-stat-label">등록된 Role</p>
								<p class="perm-stat-value" id="perm-role-count">—</p>
							</div>
						</div>
					</section>

					<section class="perm-panel">
						<div class="perm-panel-head">
							<div>
								<h2>권한 상세 목록</h2>
								<p>모듈별 접근 권한과 관리 기능</p>
							</div>
							<input id="perm-search" class="perm-search" type="search"
								autocomplete="off" placeholder="⌕ 권한 코드·권한명 검색"
								aria-label="권한 검색">
						</div>
						<div class="perm-table-wrap">
							<table class="perm-table">
								<thead>
									<tr>
										<th style="width: 27%">권한 코드</th>
										<th style="width: 21%">권한명</th>
										<th style="width: 15%">모듈</th>
										<th>설명</th>
										<th style="width: 145px">관리</th>
									</tr>
								</thead>
								<tbody id="perm-tbody">
									<tr>
										<td colspan="5" class="perm-empty">권한 목록을 불러오는 중입니다.</td>
									</tr>
								</tbody>
							</table>
						</div>
						<p id="perm-status" class="perm-status" role="status"
							aria-live="polite"></p>
					</section>
					<p class="perm-note">※ 권한 코드는 최초 등록 후 수정할 수 없습니다. 사용 중인 권한의 삭제는
						접근 제어에 영향을 줄 수 있습니다.</p>
				</div>
			</div>
		</main>
	</div>

	<!-- Bootstrap 버전과 관계없이 동작하는 S-16 전용 등록/수정 대화상자 -->
	<div id="perm-overlay" class="perm-overlay" aria-hidden="true">
		<div class="perm-dialog" role="dialog" aria-modal="true"
			aria-labelledby="perm-dialog-title">
			<div class="perm-dialog-header">
				<h2 id="perm-dialog-title">권한 등록</h2>
				<button id="perm-dialog-close" type="button" class="perm-close"
					aria-label="닫기">&times;</button>
			</div>
			<form id="perm-form">
				<div class="perm-dialog-body">
					<div class="perm-field">
						<label for="perm-code">Permission Code <span
							aria-hidden="true">*</span></label> <input id="perm-code"
							name="permissionCode" type="text" required autocomplete="off"
							placeholder="PERMISSION_EXAMPLE_READ">
						<p id="perm-code-help" class="perm-help">최초 등록 후 코드를 변경할 수
							없습니다.</p>
					</div>
					<div class="perm-field">
						<label for="perm-name">권한명 <span aria-hidden="true">*</span></label>
						<input id="perm-name" name="permissionName" type="text" required
							placeholder="예: 전체 사원 조회">
					</div>
					<div class="perm-field">
						<label for="perm-group">모듈 <span aria-hidden="true">*</span></label>
						<select id="perm-group" name="permissionGroup" required>
							<option value="">선택해주세요</option>
							<option value="HR">인사 (HR)</option>
							<option value="ATTENDANCE">근태 (ATTENDANCE)</option>
							<option value="LEAVE">휴가 (LEAVE)</option>
							<option value="AUTH">권한 (AUTH)</option>
							<option value="SECURITY">보안 (SECURITY)</option>
						</select>
					</div>
					<p id="perm-form-error" class="perm-form-error" role="alert"></p>
				</div>
				<div class="perm-dialog-footer">
					<button id="perm-cancel-btn" type="button" class="perm-cancel">취소</button>
					<button id="perm-save-btn" type="submit" class="perm-primary">등록</button>
				</div>
			</form>
		</div>
	</div>
	<div id="perm-toast" class="perm-toast" role="status"
		aria-live="polite"></div>

	<%@ include file="/WEB-INF/views/common/scripts.jsp"%>
	<script>
(function () {
    'use strict';

    const API = '${pageContext.request.contextPath}/api/permissions';
    // 버튼은 권한이 있을 때만 (서버도 SecurityConfig 에서 한 번 더 검사)
    const CAN_UPDATE = <sec:authorize access="hasAuthority('PERMISSION_PERM_UPDATE')" var="canUpdate"/>${canUpdate};
    const CAN_DELETE = <sec:authorize access="hasAuthority('PERMISSION_PERM_DELETE')" var="canDelete"/>${canDelete};
    const $ = (id) => document.getElementById(id);
    const groupLabels = { HR:'인사', ATTENDANCE:'근태', LEAVE:'휴가', AUTH:'권한', SECURITY:'보안' };
    let permissions = [];
    let currentMode = 'create';
    let currentId = null;
    let toastTimer;
    let searchTimer;
    let searchSequence = 0;
    let previousFocus = null;

    function toast(message, isError) {
        const node = $('perm-toast');
        node.textContent = message;
        node.classList.toggle('error', Boolean(isError));
        node.classList.add('show');
        clearTimeout(toastTimer);
        toastTimer = setTimeout(() => node.classList.remove('show'), 3000);
    }

    function csrfHeaders() {
        const token = document.querySelector('meta[name="_csrf"]')?.content;
        const header = document.querySelector('meta[name="_csrf_header"]')?.content;
        return token && header ? { [header]:token } : {};
    }

    async function apiFetch(url, options) {
        const response = await fetch(url, options);
        if (!response.ok) {
            let message = '요청에 실패했습니다. (HTTP ' + response.status + ')';
            try {
                const data = await response.json();
                if (data && data.message) message = data.message;
            } catch (_) { /* HTML 오류 페이지 등 */ }
            throw new Error(message);
        }
        if (response.status === 204 || response.status === 201) return null;
        return response.json();
    }

    function textCell(row, text, className) {
        const cell = document.createElement('td');
        if (className) cell.className = className;
        cell.textContent = text == null ? '' : String(text);
        row.appendChild(cell);
        return cell;
    }

    function renderTable() {
        const tbody = $('perm-tbody');
        tbody.replaceChildren();
        if (!permissions.length) {
            const row = document.createElement('tr');
            const cell = textCell(row, '조회된 권한이 없습니다.', 'perm-empty');
            cell.colSpan = 5;
            tbody.appendChild(row);
            return;
        }
        permissions.forEach((p) => {
            const row = document.createElement('tr');
            const shortCode = p.shortCode || (p.permissionCode || '').replace(/^PERMISSION_/, '');
            textCell(row, shortCode, 'perm-code');
            textCell(row, p.permissionName, 'perm-name');
            const moduleCell = document.createElement('td');
            const badge = document.createElement('span');
            badge.className = 'perm-module ' + (groupLabels[p.permissionGroup] ? p.permissionGroup : '');
            badge.textContent = p.groupName || groupLabels[p.permissionGroup] || p.permissionGroup || '—';
            moduleCell.appendChild(badge);
            row.appendChild(moduleCell);
            textCell(row, p.description || '—', 'perm-desc');
            const actions = document.createElement('td');
            const wrap = document.createElement('div');
            wrap.className = 'perm-actions';
            const edit = document.createElement('button');
            edit.type = 'button';
            edit.className = 'perm-outline';
            edit.textContent = '수정';
            edit.addEventListener('click', () => openDialog('edit', p));
            const remove = document.createElement('button');
            remove.type = 'button';
            remove.className = 'perm-danger';
            remove.textContent = '삭제';
            remove.addEventListener('click', () => deletePermission(p));
            if (CAN_UPDATE) wrap.append(edit);
            if (CAN_DELETE) wrap.append(remove);
            actions.appendChild(wrap);
            row.appendChild(actions);
            tbody.appendChild(row);
        });
    }

    async function loadPermissions() {
        const sequence = ++searchSequence;
        const keyword = $('perm-search').value.trim();
        const params = new URLSearchParams();
        if (keyword) params.set('keyword', keyword);
        const url = API + (params.toString() ? '?' + params.toString() : '');
        $('perm-status').classList.remove('error');
        $('perm-status').textContent = '조회 중...';
        try {
            const data = await apiFetch(url, { credentials:'same-origin' });
            if (sequence !== searchSequence) return;
            permissions = Array.isArray(data) ? data : [];
            renderTable();
            $('perm-status').textContent = keyword ? '검색 결과 ' + permissions.length + '건' : '총 ' + permissions.length + '건';
        } catch (e) {
            if (sequence !== searchSequence) return;
            permissions = [];
            renderTable();
            $('perm-status').classList.add('error');
            $('perm-status').textContent = e.message;
        }
    }

    async function refreshCounts() {
        try {
            const all = await apiFetch(API, { credentials:'same-origin' });
            $('perm-count').textContent = all.length;
            $('perm-group-count').textContent = new Set(all.map(p => p.permissionGroup).filter(Boolean)).size;
        } catch (e) {
            console.error('통계 조회 실패:', e);
        }
        // S-16 통계 카드: 등록된 전체 Role 수 (활성·비활성 포함)
        try {
            const roleData = await apiFetch(
                '${pageContext.request.contextPath}/api/hr-roles/count',
                { credentials:'same-origin' }
            );
            const count = Number(roleData && roleData.count);
            if (!Number.isFinite(count) || count < 0) {
                throw new Error('Role 개수 응답 형식이 올바르지 않습니다.');
            }
            $('perm-role-count').textContent = count + '개';
        } catch (e) {
            $('perm-role-count').textContent = '—';
            console.error('Role 개수 조회 실패:', e);
        }
    }

    function openDialog(mode, permission) {
        currentMode = mode;
        currentId = permission ? permission.permissionId : null;
        previousFocus = document.activeElement;
        $('perm-form').reset();
        $('perm-form-error').textContent = '';
        $('perm-dialog-title').textContent = mode === 'create' ? '권한 등록' : '권한 수정';
        $('perm-save-btn').textContent = mode === 'create' ? '등록' : '수정';
        $('perm-code').readOnly = mode === 'edit';
        $('perm-code-help').textContent = mode === 'edit'
            ? '권한 코드는 시스템 식별값이므로 수정할 수 없습니다.'
            : '권한 코드는 최초 등록 후 변경할 수 없습니다.';
        if (permission) {
            $('perm-code').value = permission.permissionCode || '';
            $('perm-name').value = permission.permissionName || '';
            $('perm-group').value = permission.permissionGroup || '';
        }
        $('perm-overlay').classList.add('open');
        $('perm-overlay').setAttribute('aria-hidden', 'false');
        (mode === 'edit' ? $('perm-name') : $('perm-code')).focus();
    }

    function closeDialog() {
        $('perm-overlay').classList.remove('open');
        $('perm-overlay').setAttribute('aria-hidden', 'true');
        if (previousFocus && typeof previousFocus.focus === 'function') previousFocus.focus();
    }

    async function savePermission(event) {
        event.preventDefault();
        const code = $('perm-code').value.trim().toUpperCase();
        const name = $('perm-name').value.trim();
        const group = $('perm-group').value;
        if (!name || !group || (currentMode === 'create' && !code)) {
            $('perm-form-error').textContent = '필수 항목을 모두 입력해주세요.';
            return;
        }
        if (currentMode === 'create' && !/^PERMISSION_[A-Z0-9_]+$/.test(code)) {
            $('perm-form-error').textContent = '권한 코드는 PERMISSION_으로 시작하며 영문 대문자, 숫자, _만 사용할 수 있습니다.';
            return;
        }
        const isCreate = currentMode === 'create';
        const url = isCreate ? API : API + '/' + encodeURIComponent(currentId);
        const body = { permissionName:name, permissionGroup:group };
        // 수정 시에는 permissionCode 필드를 보내지 않습니다.
        if (isCreate) body.permissionCode = code;
        const saveBtn = $('perm-save-btn');
        saveBtn.disabled = true;
        $('perm-form-error').textContent = '';
        try {
            await apiFetch(url, {
                method: isCreate ? 'POST' : 'PUT',
                credentials:'same-origin',
                headers: { 'Content-Type':'application/json; charset=UTF-8', ...csrfHeaders() },
                body: JSON.stringify(body)
            });
            closeDialog();
            toast(isCreate ? '권한이 등록되었습니다.' : '권한이 수정되었습니다.');
            await Promise.all([loadPermissions(), refreshCounts()]);
        } catch (e) {
            $('perm-form-error').textContent = e.message;
        } finally {
            saveBtn.disabled = false;
        }
    }

    async function deletePermission(permission) {
        const message = '[' + permission.permissionCode + '] 권한을 삭제하시겠습니까?\n'
            + '삭제 시 시스템의 접근 제어에 영향을 줄 수 있습니다.';
        if (!window.confirm(message)) return;
        try {
            await apiFetch(API + '/' + encodeURIComponent(permission.permissionId), {
                method:'DELETE', credentials:'same-origin', headers:csrfHeaders()
            });
            toast('권한이 삭제되었습니다.');
            await Promise.all([loadPermissions(), refreshCounts()]);
        } catch (e) {
            toast(e.message, true);
        }
    }

    if ($('perm-add-btn')) $('perm-add-btn').addEventListener('click', () => openDialog('create'));
    $('perm-form').addEventListener('submit', savePermission);
    $('perm-cancel-btn').addEventListener('click', closeDialog);
    $('perm-dialog-close').addEventListener('click', closeDialog);
    $('perm-overlay').addEventListener('click', (e) => {
        if (e.target === $('perm-overlay')) closeDialog();
    });
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && $('perm-overlay').classList.contains('open')) closeDialog();
    });
    $('perm-search').addEventListener('input', () => {
        clearTimeout(searchTimer);
        searchTimer = setTimeout(loadPermissions, 250);
    });
    $('perm-code').addEventListener('input', () => {
        if (currentMode === 'create') $('perm-code').value = $('perm-code').value.toUpperCase();
    });

    loadPermissions();
    refreshCounts();
})();
</script>
</body>
</html>
