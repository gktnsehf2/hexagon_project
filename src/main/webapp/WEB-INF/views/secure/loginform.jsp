<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<%@ include file="/WEB-INF/views/common/head.jsp"%>
<title>로그인 | 인사관리 시스템</title>
</head>

<body class="login-page bg-body-secondary">
	<div class="login-box">
		<div class="card card-outline card-primary">
			<div class="card-header text-center">
				<span class="h1 mb-0">HRMS</span>
			</div>
			<div class="card-body login-card-body">
				<p class="login-box-msg">인사관리 시스템에 로그인하세요</p>

				<%-- 로그인 결과 안내 (F-02) --%>
				<div id="login-alert" class="alert d-none" role="alert"></div>
				<c:if test="${param.logout != null}">
					<div class="alert alert-info">로그아웃되었습니다.</div>
				</c:if>

				<form id="login-form">
					<div class="input-group mb-3">
						<input type="text" class="form-control" name="accountId" placeholder="아이디" autocomplete="username" required autofocus>
						<div class="input-group-text"><span class="bi bi-person"></span></div>
					</div>
					<div class="input-group mb-3">
						<input type="password" class="form-control" name="pwd" placeholder="비밀번호" autocomplete="current-password" required>
						<div class="input-group-text"><span class="bi bi-lock-fill"></span></div>
					</div>
					<div class="d-grid">
						<button type="submit" class="btn btn-primary" id="bt-login">로그인</button>
					</div>
				</form>
			</div>
		</div>
	</div>

	<%@ include file="/WEB-INF/views/common/scripts.jsp"%>
	<script>
		//수업 코드처럼 비동기로 로그인하기 !! (fetch + async/await)
		const ctx = "${ctx}";
		const form = document.getElementById("login-form");
		const button = document.getElementById("bt-login");
		const alertBox = document.getElementById("login-alert");

		function showAlert(message) {
			alertBox.textContent = message;
			alertBox.className = "alert alert-danger";
		}

		form.addEventListener("submit", async (event) => {
			event.preventDefault();
			button.disabled = true;
			try {
				const response = await fetch(ctx + "/login", {
					method: "POST",
					body: new URLSearchParams(new FormData(form))
				});
				const result = await response.json();
				if (response.ok) {
					location.href = result.url;
					return;
				}
				showAlert(result.message);
			} catch (error) {
				console.log("로그인 요청 실패", error);
				showAlert("서버와 통신할 수 없습니다. 잠시 후 다시 시도해 주세요.");
			} finally {
				button.disabled = false;
			}
		});
	</script>
</body>
</html>
