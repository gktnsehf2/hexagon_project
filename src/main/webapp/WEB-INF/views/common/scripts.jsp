<%@ page pageEncoding="UTF-8" %>
<%-- 모든 화면의 </body> 직전 공통: 알림 토스트 + 스크립트 --%>
<div class="toast-container position-fixed top-0 end-0 p-3" style="z-index: 1090">
	<div id="hr-toast" class="toast align-items-center border-0" role="status" aria-live="polite" aria-atomic="true">
		<div class="d-flex">
			<div class="toast-body" id="hr-toast-body"></div>
			<button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="닫기"></button>
		</div>
	</div>
</div>
<script src="${ctx}/resources/js/jquery-3.7.1.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="${ctx}/resources/adminlte/js/adminlte.min.js"></script>
<script src="${ctx}/resources/js/hrms.js"></script>
