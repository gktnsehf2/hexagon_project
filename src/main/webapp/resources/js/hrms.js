/*
 모든 화면 공통 스크립트
 - hrToast(메시지, 색) : 오른쪽 위 알림 (색: "dark" 기본, "success", "danger")
 - data-soon 이 붙은 링크 : 아직 만들지 않은 화면이라 이동하지 않고 안내만
*/
function hrToast(message, color) {
	const box = document.getElementById("hr-toast");
	if (!box || !window.bootstrap) {
		alert(message);
		return;
	}
	box.className = "toast align-items-center border-0 text-bg-" + (color || "dark");
	document.getElementById("hr-toast-body").textContent = message;
	bootstrap.Toast.getOrCreateInstance(box, { delay: 2500 }).show();
}

document.addEventListener("click", (event) => {
	const link = event.target.closest("[data-soon]");
	if (link) {
		event.preventDefault();
		hrToast("준비 중인 기능입니다.");
	}
});
