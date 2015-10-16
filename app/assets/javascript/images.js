$(function() {
	
	function loadImage(link, event){
		event.preventDefault();
		var m = $("#imageModal");
		var imgurl = link.attr('href');
		var fi = $("#fullImage");
		fi.attr('src', imgurl);
		m.modal('show');
	}
	
	/*event handlers*/
	$(".thumb").click(function(event){loadImage($(this), event);});
	

});

