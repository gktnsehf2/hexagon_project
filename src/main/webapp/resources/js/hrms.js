/*
 모든 화면 공통 스크립트 (jQuery)
 - hrToast(메시지, 색) : 오른쪽 위 알림 (색: "dark" 기본, "success", "danger")
 - data-soon 이 붙은 링크 : 아직 만들지 않은 화면이라 이동하지 않고 안내만
*/
function hrToast(message, color) {
	const $box = $("#hr-toast");
	if (!$box.length || !window.bootstrap) {
		alert(message);
		return;
	}
	$box.attr("class", "toast align-items-center border-0 text-bg-" + (color || "dark"));
	$("#hr-toast-body").text(message);
	bootstrap.Toast.getOrCreateInstance($box[0], { delay: 2500 }).show();
}

// 이벤트 위임: 나중에 그려진 링크에도 적용됨
$(document).on("click", "[data-soon]", function (event) {
	event.preventDefault();
	hrToast("준비 중인 기능입니다.");
});
