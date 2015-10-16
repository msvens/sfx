$(function() {
	$( "#login-button" )
	.button()
	.click(function() {
		$("#login-form").submit();
	});
	$( "#cancel-button" )
	.button()
	.click(function() {
		$("#password").val('');
		$("#login").val('');
	});
	
});