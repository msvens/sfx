$(function () {

    var name = $('#prjName');
    var description = $('#prjDesc');
    var addUrl = $('#addUrl');


    function createProject() {
        alert("trying to create project " + JSON.stringify({name: name.val(), description: description.val()}));
        alert(addUrl.val());
        $.ajax({
            type: "POST",
            url: addUrl.val(),
            data: JSON.stringify({name: name.val(), description: description.val()}),
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            success: function (data) {
                window.location = window.location;
            }
        });
        $('#projectModal').modal('hide');
        //$(this).dialog("close");
    }

    $('#addProjectButton').click(createProject);

    /*
     var name = $("#name"), description = $("#description");
     $("#createProject-button")
     .button()
     .click(function() {
     $("#create-project-form").dialog("open");
     });
     $("#create-project-form").dialog({
     autoOpen: false,
     height: 500,
     width: 350,
     modal: true,
     buttons: {
     "Create": createProject,
     "Cancel": cancelCreate
     },
     close: function() {
     allFields.val( "" ).removeClass( "ui-state-error" );
     }
     });
     */
});