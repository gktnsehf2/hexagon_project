<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="menu" value="accountRoles" />

<!DOCTYPE html>
<html lang="ko">
<head>
    <%@ include file="/WEB-INF/views/common/head.jsp" %>

    <title>계정 Role 부여 | 인사관리 시스템</title>

    <meta name="_csrf" content="${_csrf.token}">
    <meta name="_csrf_header" content="${_csrf.headerName}">

    <style>
        /* S-25 전용 스타일: 기존 화면에 영향을 주지 않도록 범위 제한 */
        .ar-page {
            --ar-blue: #3768e5;
            --ar-line: #dee6f2;
            --ar-text: #26364d;
            --ar-muted: #8595aa;
            color: var(--ar-text);
            padding-bottom: 32px;
        }

        .ar-page *,
        .ar-dialog * {
            box-sizing: border-box;
        }

        .ar-page [hidden],
        .ar-dialog [hidden] {
            display: none !important;
        }

        .ar-page button,
        .ar-dialog button {
            cursor: pointer;
        }

        .ar-page button:disabled,
        .ar-dialog button:disabled {
            opacity: .55;
            cursor: not-allowed;
        }

        /* 페이지 제목 */
        .ar-page .ar-breadcrumb {
            margin: 0 0 8px;
            color: #8797ad;
            font-size: 13px;
        }

        .ar-page .ar-heading {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 15px;
            margin-bottom: 24px;
        }

        .ar-page .ar-heading h1 {
            font-size: 29px;
            letter-spacing: -.7px;
            font-weight: 800;
            margin: 0 0 7px;
        }

        .ar-page .ar-heading p {
            font-size: 13px;
            color: var(--ar-muted);
            margin: 0;
        }

        .ar-page .ar-auth-badge {
            background: #e9f0ff;
            color: #3466d8;
            font-size: 12px;
            font-weight: 800;
            border-radius: 8px;
            padding: 11px 13px;
            white-space: nowrap;
        }

        /* 좌측 계정 목록 / 우측 Role 관리 */
        .ar-page .ar-grid {
            display: grid;
            grid-template-columns: minmax(290px, 360px) minmax(0, 1fr);
            gap: 20px;
            align-items: start;
        }

        .ar-page .ar-card {
            background: #fff;
            border: 1px solid var(--ar-line);
            border-radius: 13px;
            padding: 22px;
            min-width: 0;
        }

        .ar-page .ar-card-title {
            display: flex;
            align-items: center;
            gap: 9px;
            margin: 0 0 14px;
            font-size: 18px;
            font-weight: 800;
        }

        .ar-page .ar-counter {
            font-size: 12px;
            font-weight: 800;
            border-radius: 16px;
            padding: 4px 10px;
            background: #eaf1ff;
            color: var(--ar-blue);
        }

        /* 계정 검색 */
        .ar-page .ar-search {
            width: 100%;
            border: 1px solid #d8e3f1;
            border-radius: 8px;
            min-height: 43px;
            padding: 10px 12px;
            color: var(--ar-text);
            background: #f8faff;
            font-size: 13px;
            outline: none;
            margin-bottom: 13px;
        }

        .ar-page .ar-search:focus,
        .ar-page .ar-reason:focus {
            border-color: #94b4fa;
            box-shadow: 0 0 0 3px #eaf0ff;
        }

        /* 계정 목록 */
        .ar-page .ar-account-list {
            display: flex;
            flex-direction: column;
            gap: 7px;
            max-height: 535px;
            overflow-y: auto;
            padding-right: 2px;
        }

        .ar-page .ar-account {
            display: flex;
            align-items: center;
            gap: 10px;
            width: 100%;
            text-align: left;
            background: #fff;
            border: 1px solid var(--ar-line);
            border-radius: 9px;
            padding: 11px 12px;
        }

        .ar-page .ar-account:hover {
            background: #f8fbff;
            border-color: #b8cdf8;
        }

        .ar-page .ar-account.is-selected {
            background: #eaf2ff;
            border-color: #bcd0fa;
        }

        .ar-page .ar-avatar {
            flex: 0 0 32px;
            width: 32px;
            height: 32px;
            border-radius: 50%;
            background: #edf1f6;
            color: #637a9b;
            display: grid;
            place-items: center;
            font-size: 13px;
            font-weight: 800;
        }

        .ar-page .ar-account.is-selected .ar-avatar {
            background: #d6e5ff;
            color: #3869e5;
        }

        .ar-page .ar-account-text {
            min-width: 0;
            flex: 1;
        }

        .ar-page .ar-account-name {
            display: block;
            font-size: 13px;
            font-weight: 800;
            line-height: 1.4;
            color: #26374e;
        }

        .ar-page .ar-account-id {
            color: #95a3b8;
            font-size: 11px;
            font-weight: 500;
            margin-left: 5px;
        }

        .ar-page .ar-account-sub {
            color: #94a3b8;
            font-size: 11px;
            display: block;
            margin-top: 2px;
        }

        .ar-page .ar-account-count {
            font-size: 11px;
            color: #65758b;
            font-weight: 800;
            background: #eef2f7;
            border-radius: 6px;
            padding: 6px 8px;
            white-space: nowrap;
        }

        .ar-page .ar-account.is-selected .ar-account-count {
            color: #3768e5;
            background: #e4eeff;
        }

        .ar-page .ar-note {
            display: flex;
            gap: 6px;
            margin-top: 15px;
            color: #8999b0;
            font-size: 12px;
            line-height: 1.55;
        }

        /* 선택된 계정 상세 */
        .ar-page .ar-person-head {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 12px;
            margin-bottom: 20px;
        }

        .ar-page .ar-person-name {
            display: flex;
            align-items: center;
            gap: 10px;
            flex-wrap: wrap;
        }

        .ar-page .ar-person-name h2 {
            font-size: 19px;
            font-weight: 800;
            margin: 0;
        }

        .ar-page .ar-login-badge {
            color: #60738e;
            background: #eef2f7;
            font-size: 12px;
            font-weight: 800;
            border-radius: 6px;
            padding: 5px 8px;
        }

        .ar-page .ar-person-sub {
            color: #8b9bb0;
            font-size: 12px;
            margin-top: 6px;
        }

        .ar-page .ar-active-badge {
            flex: 0 0 auto;
            color: #168467;
            background: #e7f8f0;
            border-radius: 7px;
            font-size: 12px;
            font-weight: 750;
            padding: 7px 10px;
        }

        /* Role 선택 */
        .ar-page .ar-section-title {
            display: flex;
            align-items: center;
            flex-wrap: wrap;
            gap: 10px;
            margin: 0 0 13px;
        }

        .ar-page .ar-section-title h3 {
            font-size: 15px;
            font-weight: 800;
            margin: 0;
        }

        .ar-page .ar-help {
            color: #95a4b7;
            font-size: 12px;
        }

        .ar-page .ar-role-list {
            display: flex;
            flex-direction: column;
            gap: 7px;
        }

        .ar-page .ar-role {
            display: grid;
            grid-template-columns:
                18px
                minmax(120px, 1fr)
                minmax(140px, 1fr)
                minmax(150px, 1.7fr)
                auto;
            align-items: center;
            gap: 10px;
            padding: 12px;
            border: 1px solid var(--ar-line);
            border-radius: 9px;
            cursor: pointer;
            margin: 0;
            font-size: 12px;
            min-width: 0;
        }

        .ar-page .ar-role:hover {
            border-color: #b9cffb;
            background: #fafcff;
        }

        .ar-page .ar-role.is-checked {
            border-color: #bfd1ff;
            background: #f1f5ff;
        }

        .ar-page .ar-role input {
            accent-color: var(--ar-blue);
            margin: 0;
            cursor: pointer;
        }

        .ar-page .ar-role-name {
            font-size: 13px;
            font-weight: 800;
            color: #293a52;
        }

        .ar-page .ar-role.is-checked .ar-role-name {
            color: #3264d7;
        }

        .ar-page .ar-role-code {
            font-size: 11px;
            color: #8798ac;
            overflow-wrap: anywhere;
        }

        .ar-page .ar-role-desc {
            font-size: 11px;
            color: #71839d;
            line-height: 1.5;
        }

        .ar-page .ar-perm-number {
            color: #7e8fa5;
            font-size: 11px;
            white-space: nowrap;
            text-align: right;
        }

        .ar-page .ar-inactive {
            background: #fafafa;
        }

        .ar-page .ar-inactive .ar-role-name::after {
            content: ' (비활성)';
            color: #c87c65;
            font-size: 10px;
        }

        /* 적용 Permission 미리보기 */
        .ar-page .ar-preview-title {
            font-weight: 800;
            color: #536b8d;
            font-size: 12px;
            margin: 19px 0 9px;
        }

        .ar-page .ar-permission-tags {
            display: flex;
            flex-wrap: wrap;
            gap: 6px;
            min-height: 26px;
        }

        .ar-page .ar-tag {
            font-size: 11px;
            color: #73859f;
            background: #edf1f5;
            border-radius: 5px;
            padding: 5px 8px;
            overflow-wrap: anywhere;
        }

        /* 저장 버튼 */
        .ar-page .ar-actions {
            display: flex;
            align-items: center;
            flex-wrap: wrap;
            gap: 10px;
            margin-top: 20px;
        }

        .ar-page .ar-reason {
            flex: 1 1 260px;
            min-width: 0;
            border: 1px solid #dae4f0;
            border-radius: 8px;
            min-height: 40px;
            padding: 9px 11px;
            background: #f8faff;
            font-size: 13px;
            outline: none;
        }

        .ar-page .ar-btn {
            border-radius: 8px;
            font-size: 13px;
            font-weight: 800;
            padding: 10px 16px;
            border: 1px solid #dbe3ee;
            background: #fff;
            color: #60728b;
        }

        .ar-page .ar-btn-primary {
            background: var(--ar-blue);
            border-color: var(--ar-blue);
            color: #fff;
        }

        .ar-page .ar-btn-primary:hover {
            background: #305bca;
        }

        .ar-page .ar-actions .ar-btn-history {
            margin-left: auto;
        }

        .ar-page .ar-message {
            font-size: 12px;
            color: #63758e;
            min-height: 18px;
            margin: 12px 0 0;
        }

        .ar-page .ar-message.is-error {
            color: #c84d4d;
        }

        .ar-page .ar-empty {
            text-align: center;
            color: #92a1b4;
            font-size: 13px;
            padding: 28px 10px;
        }

        /* 변경 이력 팝업 */
        .ar-dialog {
            border: 1px solid #dfe7f2;
            border-radius: 13px;
            padding: 0;
            width: min(680px, calc(100vw - 28px));
            max-height: 80vh;
            color: #293950;
            box-shadow: 0 15px 55px #14253a33;
        }

        .ar-dialog::backdrop {
            background: #15223883;
        }

        .ar-dialog .ar-dialog-inner {
            padding: 24px;
        }

        .ar-dialog .ar-dialog-head {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 10px;
            margin-bottom: 15px;
        }

        .ar-dialog h2 {
            font-size: 20px;
            font-weight: 800;
            margin: 0;
        }

        .ar-dialog .ar-history-list {
            display: flex;
            flex-direction: column;
            gap: 10px;
            max-height: 50vh;
            overflow: auto;
        }

        .ar-dialog .ar-history-row {
            border: 1px solid #e0e8f2;
            border-radius: 8px;
            padding: 12px;
        }

        .ar-dialog .ar-history-main {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 9px;
            flex-wrap: wrap;
            font-size: 13px;
            font-weight: 800;
        }

        .ar-dialog .ar-history-sub {
            font-size: 12px;
            color: #8393a9;
            margin-top: 7px;
            overflow-wrap: anywhere;
        }

        .ar-dialog .ar-history-grant {
            color: #2b7f60;
        }

        .ar-dialog .ar-history-revoke {
            color: #c15252;
        }

        /* 반응형 */
        @media (max-width: 1150px) {
            .ar-page .ar-role {
                grid-template-columns:
                    18px
                    minmax(115px, 1fr)
                    minmax(125px, 1fr)
                    auto;
            }

            .ar-page .ar-role-desc {
                display: none;
            }
        }

        @media (max-width: 850px) {
            .ar-page .ar-grid {
                grid-template-columns: 1fr;
            }

            .ar-page .ar-heading {
                align-items: flex-start;
                flex-direction: column;
            }

            .ar-page .ar-auth-badge {
                white-space: normal;
            }

            .ar-page .ar-account-list {
                max-height: 280px;
            }
        }

        @media (max-width: 520px) {
            .ar-page .ar-card {
                padding: 17px 13px;
            }

            .ar-page .ar-role {
                grid-template-columns: 18px 1fr auto;
            }

            .ar-page .ar-role-code,
            .ar-page .ar-role-desc {
                display: none;
            }

            .ar-page .ar-actions .ar-btn-history {
                margin-left: 0;
            }
        }
    </style>
</head>

<body class="layout-fixed sidebar-expand-lg bg-body-tertiary">
<div class="app-wrapper">

    <%@ include file="/WEB-INF/views/common/header.jsp" %>
    <%@ include file="/WEB-INF/views/common/sidebar.jsp" %>

    <main class="app-main">
        <div class="app-content">
            <div class="container-fluid py-4 ar-page">

                <!-- 페이지 제목 -->
                <p class="ar-breadcrumb">
                    권한 관리 / 계정 Role 부여
                </p>

                <div class="ar-heading">
                    <div>
                        <h1>계정 Role 부여</h1>
                        <p>
                            계정을 선택해 Role을 부여하거나 회수합니다.
                            권한은 Role을 통해 계정에 적용됩니다.
                        </p>
                    </div>

                    <span class="ar-auth-badge">
                        필요 권한 · ROLE_ASSIGN
                    </span>
                </div>

                <div class="ar-grid">

                    <!-- 좌측: 계정 선택 -->
                    <section class="ar-card" aria-label="계정 선택">

                        <h2 class="ar-card-title">
                            계정 선택
                            <span class="ar-counter" id="ar-account-total">
                                —
                            </span>
                        </h2>

                        <input
                            class="ar-search"
                            type="search"
                            id="ar-search"
                            placeholder="아이디 · 이름 검색"
                            aria-label="계정 검색"
                            autocomplete="off">

                        <div class="ar-account-list" id="ar-account-list">
                            <p class="ar-empty">
                                계정 목록을 불러오는 중입니다.
                            </p>
                        </div>

                        <p class="ar-note">
                            <span aria-hidden="true">ⓘ</span>
                            <span>
                                활성 계정만 목록에 표시됩니다.
                                Role 변경 내역은 이력에 기록되며
                                변경된 권한은 재로그인 후 반영됩니다.
                            </span>
                        </p>
                    </section>


                    <!-- 우측: 계정 Role 설정 -->
                    <section class="ar-card" aria-label="계정 Role 설정">

                        <!-- 선택한 계정 정보 -->
                        <div class="ar-person-head">
                            <div>
                                <div class="ar-person-name">
                                    <span
                                        class="ar-avatar"
                                        id="ar-detail-avatar"
                                        aria-hidden="true">?</span>

                                    <h2 id="ar-detail-name">
                                        계정을 선택해주세요
                                    </h2>

                                    <span
                                        class="ar-login-badge"
                                        id="ar-detail-login"
                                        hidden></span>
                                </div>

                                <div
                                    class="ar-person-sub"
                                    id="ar-detail-sub">
                                    왼쪽에서 계정을 선택하면
                                    Role을 관리할 수 있습니다.
                                </div>
                            </div>

                            <span
                                class="ar-active-badge"
                                id="ar-active-badge"
                                hidden>
                                계정 정상
                            </span>
                        </div>

                        <!-- Role 편집 영역 -->
                        <div id="ar-editor" hidden>

                            <div class="ar-section-title">
                                <h3>부여할 Role</h3>

                                <span
                                    class="ar-counter"
                                    id="ar-selected-total">
                                    0개 선택
                                </span>

                                <span class="ar-help">
                                    선택한 Role의 Permission이 계정에 적용됩니다.
                                </span>
                            </div>

                            <!-- Role 체크박스 생성 위치 -->
                            <div
                                class="ar-role-list"
                                id="ar-role-list">
                            </div>

                            <!-- 선택한 Role의 Permission 합계 -->
                            <p
                                class="ar-preview-title"
                                id="ar-preview-title">
                                적용되는 권한 0개
                            </p>

                            <div
                                class="ar-permission-tags"
                                id="ar-permission-tags">
                            </div>

                            <!-- 변경 사유 및 저장 -->
                            <div class="ar-actions">

                                <input
                                    class="ar-reason"
                                    id="ar-reason"
                                    type="text"
                                    maxlength="200"
                                    placeholder="변경 사유 (예: 팀장 발령)"
                                    aria-label="Role 변경 사유">

                                <button
                                    class="ar-btn ar-btn-history"
                                    type="button"
                                    id="ar-history-btn">
                                    변경 이력
                                </button>

                                <button
                                    class="ar-btn ar-btn-primary"
                                    type="button"
                                    id="ar-save-btn"
                                    disabled>
                                    저장
                                </button>
                            </div>

                            <p
                                id="ar-status"
                                class="ar-message"
                                aria-live="polite">
                            </p>
                        </div>
                    </section>
                </div>
            </div>
        </div>
    </main>
</div>


<!-- 변경 이력 팝업 -->
<dialog
    class="ar-dialog"
    id="ar-history-dialog"
    aria-labelledby="ar-history-title">

    <div class="ar-dialog-inner">

        <div class="ar-dialog-head">
            <h2 id="ar-history-title">
                Role 변경 이력
            </h2>

            <button
                type="button"
                class="ar-btn"
                id="ar-history-close">
                닫기
            </button>
        </div>

        <div
            id="ar-history-list"
            class="ar-history-list">
        </div>
    </div>
</dialog>


<%@ include file="/WEB-INF/views/common/scripts.jsp" %>

<script>
(function () {
    'use strict';

    // 1. API 주소 및 화면 요소
    const CONTEXT = '${pageContext.request.contextPath}';

    const ACCOUNT_API = CONTEXT + '/api/accounts';
    const ROLE_API = CONTEXT + '/api/roles';

    const byId = (id) => document.getElementById(id);

    let accounts = [];
    let roles = [];

    let selectedAccount = null;

    // 기존 Role과 화면에서 선택한 Role 비교용
    let originalRoleIds = new Set();
    let draftRoleIds = new Set();

    // 조회한 계정의 Role 개수 보관
    let roleCountCache = new Map();

    let loadingSerial = 0;
    let busy = false;


    // 2. 텍스트 요소 생성
    function makeText(tag, value, className) {

        const element = document.createElement(tag);

        if (className) {
            element.className = className;
        }

        element.textContent =
            value == null ? '' : String(value);

        return element;
    }


    // 3. 처리 결과 메시지 표시
    function setStatus(message, error) {

        const status = byId('ar-status');

        status.textContent = message || '';

        status.classList.toggle(
            'is-error',
            Boolean(error)
        );
    }


    // 4. CSRF 헤더 생성
    function csrfHeaders() {

        const token =
            document.querySelector('meta[name="_csrf"]')?.content;

        const header =
            document.querySelector('meta[name="_csrf_header"]')?.content;

        return token && header
            ? {[header]: token}
            : {};
    }


    // 5. REST API 공통 호출
    async function apiFetch(url, options) {

        const response = await fetch(url, {
            credentials: 'same-origin',
            ...(options || {})
        });

        if (!response.ok) {

            let message =
                '요청에 실패했습니다. (HTTP '
                + response.status + ')';

            if (response.status === 403) {
                message = '접근 권한이 없습니다. (403)';
            }

            try {
                const data = await response.json();

                if (data && (data.message || data.error)) {
                    message = data.message || data.error;
                }

            } catch (_) {
                // 서버가 HTML 오류 페이지를 반환한 경우
            }

            throw new Error(message);
        }

        if (response.status === 204) {
            return null;
        }

        return response.json();
    }


    // 6. 기존 Role과 선택한 Role 비교
    function sameIds(a, b) {

        return a.size === b.size
            && Array.from(a).every(
                (id) => b.has(id)
            );
    }


    // 7. 변경 사항 존재 여부 확인
    function hasChanges() {

        return !sameIds(
            originalRoleIds,
            draftRoleIds
        );
    }


    // 8. 미저장 변경 사항 확인
    function confirmDiscard() {

        return !hasChanges()
            || window.confirm(
                '저장되지 않은 Role 변경이 있습니다. 이동하시겠습니까?'
            );
    }


    // 9. 저장 버튼 활성화 상태 변경
    function refreshSaveButton() {

        byId('ar-save-btn').disabled =
            busy || !selectedAccount || !hasChanges();
    }


    // 10. 왼쪽 계정 목록 표시
    function renderAccounts() {

        const filter =
            byId('ar-search').value.trim().toLowerCase();

        const matches = accounts.filter((account) => {

            return String(account.accountId).includes(filter)
                || String(account.loginId || '')
                    .toLowerCase().includes(filter)
                || String(account.empName || '')
                    .toLowerCase().includes(filter);
        });

        const root = byId('ar-account-list');

        root.replaceChildren();

        byId('ar-account-total').textContent =
            String(accounts.length);

        if (!matches.length) {

            root.appendChild(
                makeText(
                    'p',
                    '검색 결과가 없습니다.',
                    'ar-empty'
                )
            );

            return;
        }

        matches.forEach((account) => {

            const button = document.createElement('button');

            button.type = 'button';

            button.className =
                'ar-account'
                + (
                    selectedAccount
                    && selectedAccount.accountId === account.accountId
                        ? ' is-selected'
                        : ''
                );

            const name =
                account.empName || account.loginId || '이름 없음';

            const avatar = makeText(
                'span',
                name.substring(0, 1),
                'ar-avatar'
            );

            const text = makeText(
                'span',
                '',
                'ar-account-text'
            );

            const title = makeText(
                'span',
                name,
                'ar-account-name'
            );

            title.appendChild(
                makeText(
                    'span',
                    account.loginId || '',
                    'ar-account-id'
                )
            );

            text.append(
                title,
                makeText(
                    'span',
                    '계정 ID ' + account.accountId,
                    'ar-account-sub'
                )
            );

            const count =
                roleCountCache.get(account.accountId);

            const badge = makeText(
                'span',
                count == null
                    ? 'Role 조회'
                    : 'Role ' + count,
                'ar-account-count'
            );

            button.append(
                avatar,
                text,
                badge
            );

            button.addEventListener('click', () => {
                openAccount(account);
            });

            root.appendChild(button);
        });
    }


    // 11. 선택한 Role의 Permission 합산 표시
    function updatePreview() {

        const codes = new Map();

        roles
            .filter((role) =>
                draftRoleIds.has(role.hrRoleId)
            )
            .forEach((role) => {

                const permissions =
                    Array.isArray(role.permissions)
                        ? role.permissions
                        : [];

                permissions.forEach((permission) => {

                    const code =
                        permission.shortCode
                        || String(
                            permission.permissionCode || ''
                        ).replace(/^PERMISSION_/, '');

                    codes.set(
                        permission.permissionId,
                        code
                    );
                });
            });

        byId('ar-selected-total').textContent =
            draftRoleIds.size + '개 선택';

        byId('ar-preview-title').textContent =
            '적용되는 권한 ' + codes.size
            + '개 (선택한 Role의 권한을 합산)';

        const root = byId('ar-permission-tags');

        root.replaceChildren();

        if (!codes.size) {

            root.appendChild(
                makeText(
                    'span',
                    '선택한 Role에 Permission이 없습니다.',
                    'ar-help'
                )
            );
        }

        codes.forEach((code) => {

            root.appendChild(
                makeText(
                    'span',
                    code,
                    'ar-tag'
                )
            );
        });

        refreshSaveButton();
    }


    // 12. 오른쪽 Role 체크박스 표시
    function renderRoles() {

        const root = byId('ar-role-list');

        root.replaceChildren();

        // 활성 Role과 이미 부여된 비활성 Role 표시
        const visible = roles.filter((role) => {

            return Number(role.isActive) === 1
                || originalRoleIds.has(role.hrRoleId);
        });

        if (!visible.length) {

            root.appendChild(
                makeText(
                    'p',
                    '선택 가능한 Role이 없습니다.',
                    'ar-empty'
                )
            );
        }

        visible.forEach((role) => {

            const active =
                Number(role.isActive) === 1;

            const checked =
                draftRoleIds.has(role.hrRoleId);

            const row = document.createElement('label');

            row.className =
                'ar-role'
                + (checked ? ' is-checked' : '')
                + (!active ? ' ar-inactive' : '');

            const checkbox =
                document.createElement('input');

            checkbox.type = 'checkbox';
            checkbox.checked = checked;
            checkbox.value = String(role.hrRoleId);

            checkbox.disabled =
                busy || (!active && !checked);

            checkbox.addEventListener('change', () => {

                if (checkbox.checked) {
                    draftRoleIds.add(role.hrRoleId);
                } else {
                    draftRoleIds.delete(role.hrRoleId);
                }

                row.classList.toggle(
                    'is-checked',
                    checkbox.checked
                );

                updatePreview();
            });

            const perms =
                Array.isArray(role.permissions)
                    ? role.permissions
                    : [];

            const description = perms
                .slice(0, 3)
                .map((p) =>
                    p.permissionName || p.shortCode
                )
                .filter(Boolean)
                .join(' · ');

            row.append(
                checkbox,

                makeText(
                    'span',
                    role.roleName || '이름 없음',
                    'ar-role-name'
                ),

                makeText(
                    'span',
                    role.roleCode || '',
                    'ar-role-code'
                ),

                makeText(
                    'span',
                    description || '연결된 권한 없음',
                    'ar-role-desc'
                ),

                makeText(
                    'span',
                    '권한 ' + perms.length + '개',
                    'ar-perm-number'
                )
            );

            root.appendChild(row);
        });

        updatePreview();
    }


    // 13. 특정 계정의 현재 Role 조회
    async function openAccount(account, force) {

        if (busy) {
            return false;
        }

        if (!force && selectedAccount && !confirmDiscard()) {
            return false;
        }

        const serial = ++loadingSerial;

        selectedAccount = account;

        originalRoleIds = new Set();
        draftRoleIds = new Set();

        byId('ar-editor').hidden = true;

        byId('ar-detail-name').textContent =
            account.empName || account.loginId || '이름 없음';

        byId('ar-detail-avatar').textContent =
            (
                account.empName
                || account.loginId
                || '?'
            ).substring(0, 1);

        byId('ar-detail-login').textContent =
            account.loginId || '';

        byId('ar-detail-login').hidden = false;

        byId('ar-detail-sub').textContent =
            '계정 ID ' + account.accountId
            + ' · 사원 ID ' + account.empId;

        byId('ar-active-badge').hidden = false;

        renderAccounts();

        try {

            const currentRoles = await apiFetch(
                ACCOUNT_API
                + '/'
                + encodeURIComponent(account.accountId)
                + '/roles'
            );

            if (serial !== loadingSerial) {
                return false;
            }

            if (!Array.isArray(currentRoles)) {
                throw new Error(
                    '계정 Role 응답 형식이 올바르지 않습니다.'
                );
            }

            originalRoleIds = new Set(
                currentRoles.map(
                    (role) => role.hrRoleId
                )
            );

            draftRoleIds = new Set(originalRoleIds);

            // 현재 연결되어 있으나 Role 목록에 없는 경우
            currentRoles.forEach((role) => {

                if (!roles.some(
                    (item) =>
                        item.hrRoleId === role.hrRoleId
                )) {
                    roles.push(role);
                }
            });

            roleCountCache.set(
                account.accountId,
                originalRoleIds.size
            );

            renderAccounts();
            renderRoles();

            byId('ar-reason').value = '';

            byId('ar-editor').hidden = false;

            setStatus(
                'Role을 변경한 뒤 변경 사유를 입력하고 저장하세요.'
            );

            return true;

        } catch (error) {

            if (serial === loadingSerial) {

                byId('ar-editor').hidden = false;

                byId('ar-role-list').replaceChildren(
                    makeText(
                        'p',
                        error.message,
                        'ar-empty'
                    )
                );

                byId('ar-selected-total').textContent =
                    '조회 실패';

                setStatus(error.message, true);
                refreshSaveButton();
            }

            return false;
        }
    }


    // 14. Role 부여 및 회수 저장
    async function saveChanges() {

        if (
            busy
            || !selectedAccount
            || !hasChanges()
        ) {
            return;
        }

        const reason =
            byId('ar-reason').value.trim();

        if (!reason) {

            setStatus(
                'Role 변경 사유를 입력해주세요.',
                true
            );

            byId('ar-reason').focus();

            return;
        }

        // 기존 목록과 새로운 목록의 차이 계산
        const toRevoke =
            Array.from(originalRoleIds)
                .filter(
                    (id) => !draftRoleIds.has(id)
                );

        const toGrant =
            Array.from(draftRoleIds)
                .filter(
                    (id) => !originalRoleIds.has(id)
                );

        const baseRole = roles.find(
            (role) =>
                role.roleCode === 'ROLE_EMPLOYEE'
        );

        let question =
            selectedAccount.loginId
            + ' 계정에 Role '
            + toGrant.length
            + '개 부여, '
            + toRevoke.length
            + '개 회수하시겠습니까?';

        if (
            baseRole
            && toRevoke.includes(baseRole.hrRoleId)
        ) {
            question +=
                '\n주의: 기본 직원 Role도 회수됩니다.';
        }

        if (!window.confirm(question)) {
            return;
        }

        busy = true;

        refreshSaveButton();

        byId('ar-history-btn').disabled = true;

        setStatus('Role 변경을 처리하는 중입니다.');

        const accountId = selectedAccount.accountId;

        try {

            // Role 회수
            for (const hrRoleId of toRevoke) {

                await apiFetch(
                    ACCOUNT_API
                    + '/'
                    + accountId
                    + '/roles/'
                    + hrRoleId,
                    {
                        method: 'DELETE',

                        headers: {
                            'Content-Type':
                                'application/json; charset=UTF-8',
                            ...csrfHeaders()
                        },

                        body: JSON.stringify({
                            reason: reason
                        })
                    }
                );
            }

            // Role 부여
            for (const hrRoleId of toGrant) {

                await apiFetch(
                    ACCOUNT_API
                    + '/'
                    + accountId
                    + '/roles',
                    {
                        method: 'POST',

                        headers: {
                            'Content-Type':
                                'application/json; charset=UTF-8',
                            ...csrfHeaders()
                        },

                        body: JSON.stringify({
                            hrRoleId: hrRoleId,
                            reason: reason
                        })
                    }
                );
            }

            setStatus(
                'Role 변경이 완료되었습니다. '
                + '적용된 권한은 재로그인 후 반영됩니다.'
            );

        } catch (error) {

            setStatus(
                '변경 중 오류가 발생했습니다: '
                + error.message
                + ' 일부 변경이 저장되었을 수 있습니다.',
                true
            );

        } finally {

            busy = false;

            byId('ar-history-btn').disabled = false;

            const message =
                byId('ar-status').textContent;

            const error =
                byId('ar-status')
                    .classList.contains('is-error');

            // 실제 DB에 저장된 상태를 다시 조회
            const refreshed =
                await openAccount(selectedAccount, true);

            if (refreshed) {
                setStatus(message, error);
            }
        }
    }


    // 15. 계정별 Role 변경 이력 조회
    async function showHistory() {

        if (!selectedAccount || busy) {
            return;
        }

        const dialog =
            byId('ar-history-dialog');

        byId('ar-history-title').textContent =
            (
                selectedAccount.empName
                || selectedAccount.loginId
            ) + ' · Role 변경 이력';

        const root =
            byId('ar-history-list');

        root.replaceChildren(
            makeText(
                'p',
                '변경 이력을 불러오는 중입니다.',
                'ar-empty'
            )
        );

        dialog.showModal();

        try {

            const items = await apiFetch(
                ACCOUNT_API
                + '/'
                + selectedAccount.accountId
                + '/roles/history'
            );

            if (!Array.isArray(items)) {

                throw new Error(
                    '변경 이력 응답 형식이 올바르지 않습니다.'
                );
            }

            root.replaceChildren();

            if (!items.length) {

                root.appendChild(
                    makeText(
                        'p',
                        '등록된 변경 이력이 없습니다.',
                        'ar-empty'
                    )
                );
            }

            items.forEach((item) => {

                const wrap = makeText(
                    'div',
                    '',
                    'ar-history-row'
                );

                const head = makeText(
                    'div',
                    '',
                    'ar-history-main'
                );

                const action =
                    item.actionType === 'GRANT'
                        ? '부여'
                        : (
                            item.actionType === 'REVOKE'
                                ? '회수'
                                : item.actionType
                        );

                head.append(
                    makeText(
                        'span',
                        item.roleName
                            || 'Role #' + item.hrRoleId,
                        ''
                    ),

                    makeText(
                        'span',
                        action,
                        item.actionType === 'GRANT'
                            ? 'ar-history-grant'
                            : 'ar-history-revoke'
                    )
                );

                wrap.append(head);

                wrap.appendChild(
                    makeText(
                        'div',
                        '사유: ' + (item.reason || '-'),
                        'ar-history-sub'
                    )
                );

                wrap.appendChild(
                    makeText(
                        'div',
                        '처리자: '
                        + (item.registerName || '-')
                        + ' · 처리일: '
                        + String(
                            item.createdAt || '-'
                        ).replace('T', ' '),
                        'ar-history-sub'
                    )
                );

                root.appendChild(wrap);
            });

        } catch (error) {

            root.replaceChildren(
                makeText(
                    'p',
                    error.message,
                    'ar-empty'
                )
            );
        }
    }


    // 16. 이벤트 연결
    byId('ar-search').addEventListener(
        'input',
        renderAccounts
    );

    byId('ar-reason').addEventListener(
        'input',
        refreshSaveButton
    );

    byId('ar-save-btn').addEventListener(
        'click',
        saveChanges
    );

    byId('ar-history-btn').addEventListener(
        'click',
        showHistory
    );

    byId('ar-history-close').addEventListener(
        'click',
        () => byId('ar-history-dialog').close()
    );


    // 17. 최초 화면 진입 시 데이터 조회
    (async function initialize() {

        try {

            const results = await Promise.all([

                apiFetch(
                    ACCOUNT_API + '/assignable'
                ),

                apiFetch(ROLE_API)

            ]);

            if (
                !Array.isArray(results[0])
                || !Array.isArray(results[1])
            ) {
                throw new Error(
                    'API 응답 형식이 올바르지 않습니다.'
                );
            }

            accounts = results[0];
            roles = results[1];

            renderAccounts();

            // 첫 번째 계정 자동 선택
            if (accounts.length) {

                await openAccount(
                    accounts[0],
                    true
                );

            } else {

                byId('ar-account-list').replaceChildren(
                    makeText(
                        'p',
                        'Role을 부여할 수 있는 활성 계정이 없습니다.',
                        'ar-empty'
                    )
                );
            }

        } catch (error) {

            byId('ar-account-list').replaceChildren(
                makeText(
                    'p',
                    error.message,
                    'ar-empty'
                )
            );

            byId('ar-detail-sub').textContent =
                'API 접근 권한과 서버 상태를 확인해주세요.';
        }
    })();

})();
</script>
</body>
</html>
