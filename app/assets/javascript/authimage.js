

$(function() {
	
	var n = $("#name");
	var d = $("#description");
	var cpy = $("#copy");
	var updateUrl = $("#updateImgUrl");
	var updateModal = $("#updateImgModal");
	var updateThumbUrl = $("#updateThumbButton").data("url");
	var jcrop_api = null;
	
	function updateImage() {
		var doCopy;
		doCopy = cpy.val == "true";
		alert(JSON.stringify({name: n.val(), description: d.val(), copy: doCopy}));
		$.ajax({
			type: "POST",
			url: updateUrl.val(),
			data: JSON.stringify({name: n.val(), description: d.val(), copy: doCopy}),
			dataType: "json",
			contentType: "application/json; charset=utf-8",
			success: function(data){
					alert(JSON.stringify(data));
					window.location = window.location;
				}
		});
	}
	
	function updateThumb() {
		var c = jcrop_api.tellSelect();
		$.ajax({
			type: "POST",
			url: updateThumbUrl,
			data: JSON.stringify({x: c.x, y: c.y, w: c.w, h: c.h}),
			dataType: "json",
			contentType: "application/json; charset=utf-8",
			success: function(data){  alert(JSON.stringify(data));}
		});
	}
	
	$(".projectImage").Jcrop({
		allowResize : true,
		aspectRatio: 1,
		setSelect : [10, 10, 160, 160]
	},function() {
		jcrop_api = this;
	});

	/* EVENT HANDLERS */
	$("#updateImageButton").click(function(event){
		updateImage();
		event.preventDefault();
	});
	
	$("#updateThumbButton").click(function(event){
		updateThumb();
		event.preventDefault();
	});
	
});