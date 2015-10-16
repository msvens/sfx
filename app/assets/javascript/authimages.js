$(function () {

    var options = {
        dataType: 'json',
        success: function (data) {
            alert(JSON.stringify(data));
            if (data.status == "ok") {
                alert(data.stauts + " " + data.message);
            }
            window.location = window.location;
        }
    };

    /*Event Handlers*/
    //upload image:
    $('#imgForm').submit(function (event) {
        event.preventDefault();
        // submit the form
        alert("in ajax submit");
        $(this).ajaxSubmit(options);
        // return false to prevent normal browser submit and page navigation
        return false;
    });

    //delete image:
    $(".deleteImg").click(function (event) {
        event.preventDefault();
        alert("in delete image");
        $.ajax({
            type: 'DELETE',
            url: $(this).attr('href'),
            dataType: 'json',
            success: function (data) {
                window.location = window.location;
            }
        });
    });


});