<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="menuGroup" value="leave" />
<c:set var="menu" value="calendar" />
<!DOCTYPE html>
<html lang="ko">
<head>
<%@ include file="/WEB-INF/views/common/head.jsp"%>
<title>휴가 캘린더 | HexagonGMI 인사관리</title>
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
							<h2 class="hr-page-title">휴가 캘린더</h2>
						</div>
						<div class="col-md-6">
							<ol class="breadcrumb justify-content-md-end mb-2">
								<li class="breadcrumb-item"><a href="${ctx}/dashboard">홈</a></li>
								<li class="breadcrumb-item">휴가 관리</li>
								<li class="breadcrumb-item active" aria-current="page">휴가 캘린더</li>
							</ol>
						</div>
					</div>
				</div>
			</div>

			<div class="app-content">
				<div class="container-fluid">
					<div class="hr-card hr-section mt-1">
						<!-- 범례 -->
						<div class="hr-cal-legend">
							<span><i class="is-annual"></i>연차</span>
							<span><i class="is-half"></i>반차</span>
							<span><i class="is-sick"></i>병가</span>
							<span><i class="is-etc"></i>경조사 · 공가</span>
							<span><i class="is-holiday"></i>공휴일</span>
						</div>
						<div id="calendar" class="hr-calendar"></div>
						<p class="hr-help">
						</p>
					</div>
				</div>
			</div>
		</main>

	</div>

	<!-- 휴가 상세 팝업 -->
	<div class="modal fade hr-modal" id="leave-modal" tabindex="-1" aria-labelledby="leave-title" aria-hidden="true">
		<div class="modal-dialog modal-dialog-centered" style="max-width: 380px">
			<div class="modal-content">
				<div class="modal-body p-4">
					<button type="button" class="btn-close float-end" data-bs-dismiss="modal" aria-label="닫기"></button>
					<h3 class="h5 fw-bold mb-3" id="leave-title">휴가 정보</h3>
					<dl class="hr-dl mb-0">
						<dt>이름</dt><dd id="lv-ename"></dd>
						<dt>부서</dt><dd id="lv-dept"></dd>
						<dt>휴가 유형</dt><dd id="lv-type"></dd>
						<dt>기간</dt><dd id="lv-period"></dd>
						<dt>일수</dt><dd id="lv-days"></dd>
					</dl>
				</div>
			</div>
		</div>
	</div>

	<%@ include file="/WEB-INF/views/common/scripts.jsp"%>
	<script src="${ctx}/resources/fullcalendar/index.global.min.js"></script>
	<script src="${ctx}/resources/fullcalendar/ko.global.min.js"></script>
	<script>
		const ctx = "${ctx}";
		const calendarEl = document.getElementById("calendar");
		const leaveModal = new bootstrap.Modal(document.getElementById("leave-modal"));

		const calendar = new FullCalendar.Calendar(calendarEl, {
			locale: "ko",
			initialView: "dayGridMonth",
			headerToolbar: {
				left: "timeGridDay,timeGridWeek,dayGridMonth,listMonth",
				center: "prev title next",
				right: "today"
			},
			buttonText: { day: "일간", week: "주간", month: "월간", list: "목록", today: "오늘" },
			height: "auto",
			fixedWeekCount: false,
			dayMaxEvents: 3,                            // 하루 3건 넘으면 +n 으로 접음
			moreLinkContent: arg => "+" + arg.num,
			allDayText: "종일",
			slotMinTime: "07:00:00",
			slotMaxTime: "21:00:00",
			nowIndicator: true,
			eventDisplay: "block",                      // 반차(시간 일정)도 월간에서 막대로
			displayEventTime: false,
			noEventsText: "이 기간에는 휴가가 없습니다.",
			dayCellContent: arg => arg.dayNumberText.replace("일", ""),

			// 보기·기간이 바뀔 때마다 FullCalendar 가 start, end 를 붙여서 GET
			events: {
				url: ctx + "/api/calendar/events",
				failure: () => hrToast("일정을 불러오지 못했습니다. 다시 로그인하거나 잠시 후 시도해 주세요.", "danger")
			},

			// 공휴일 날짜는 숫자를 빨간색으로
			eventsSet: events => {
				calendarEl.querySelectorAll(".is-holiday-date").forEach(el => el.classList.remove("is-holiday-date"));
				events.filter(e => e.extendedProps.kind === "HOLIDAY").forEach(e => {
					calendarEl.querySelectorAll('[data-date="' + e.startStr + '"]').forEach(el => el.classList.add("is-holiday-date"));
				});
			},

			eventClick: info => {
				const p = info.event.extendedProps;
				if (p.kind !== "LEAVE") {
					return;
				}
				document.getElementById("lv-ename").textContent = p.ename;
				document.getElementById("lv-dept").textContent = p.departmentName;
				document.getElementById("lv-type").textContent = p.leaveTypeName;
				document.getElementById("lv-period").textContent = p.startDate === p.endDate ? p.startDate : p.startDate + " ~ " + p.endDate;
				document.getElementById("lv-days").textContent = p.days + "일";
				leaveModal.show();
			}
		});
		calendar.render();
	</script>
</body>
</html>
