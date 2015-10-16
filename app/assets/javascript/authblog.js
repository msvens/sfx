//functions for posting a blog post (using markdown)

$(function () {
	// editor buttons (basic editing provided by default)
	var cmdImgLink = {
		name: 'cmdImage',
		title: 'Image',
		icon: 'glyphicon glyphicon-picture',
		callback: function (e) {
			// Give ![] surround the selection and prepend the image link
			var chunk, cursor, selected = e.getSelection(), content = e.getContent(), link;

			if (selected.length === 0) {
				// Give extra word
				chunk = 'enter image description here';
			} else {
				chunk = selected.text;
			}

			link = prompt('Insert Image Hyperlink', 'http://');

			if (link !== null) {
				// transform selection and set the cursor into chunked text
				e.replaceSelection('![' + chunk + '](' + link + ' "enter image title here")');
				cursor = selected.start + 2;

				// Set the next tab
				e.setNextTab('enter image title here');

				// Set the cursor
				e.setSelection(cursor, cursor + chunk.length);
			}
		}
	};

	var cmdLink = {
		name: 'cmdUrl',
		title: 'URL/Link',
		icon: 'glyphicon glyphicon-globe',
		callback: function (e) {
			// Give [] surround the selection and prepend the link
			var chunk, cursor, selected = e.getSelection(), content = e.getContent(), link;

			if (selected.length === 0) {
				// Give extra word
				chunk = 'enter link description here';
			} else {
				chunk = selected.text;
			}

			link = prompt('Insert Hyperlink', 'http://');

			if (link !== null && link !== '' && link != 'http://') {
				// transform selection and set the cursor into chunked text
				e.replaceSelection('[' + chunk + '](' + link + ')');
				cursor = selected.start + 1;

				// Set the cursor
				e.setSelection(cursor, cursor + chunk.length);
			}
		}

	};

	var cmdList = {
		name: 'cmdList',
		title: 'List',
		icon: 'glyphicon glyphicon-list',
		callback: function (e) {
			// Prepend/Give - surround the selection
			var chunk, cursor, selected = e.getSelection(), content = e.getContent();

			// transform selection and set the cursor into chunked text
			if (selected.length === 0) {
				// Give extra word
				chunk = 'list text here';

				e.replaceSelection('- ' + chunk);

				// Set the cursor
				cursor = selected.start + 2;
			} else {
				if (selected.text.indexOf('\n') < 0) {
					chunk = selected.text;

					e.replaceSelection('- ' + chunk);

					// Set the cursor
					cursor = selected.start + 2;
				} else {
					var list = [];

					list = selected.text.split('\n');
					chunk = list[0];

					$.each(list, function (k, v) {
						list[k] = '- ' + v;
					});

					e.replaceSelection('\n\n' + list.join('\n'));

					// Set the cursor
					cursor = selected.start + 4;
				}
			}

			// Set the cursor
			e.setSelection(cursor, cursor + chunk.length);
		}
	};

	var cmdPreview = {
		name: 'cmdPreview',
		toggle: true,
		title: 'Preview',
		btnText: 'Preview',
		btnClass: 'btn btn-primary btn-sm',
		icon: 'glyphicon glyphicon-search',
		callback: function (e) {
			// Check the preview mode and toggle based on this flag
			var isPreview = e.$isPreview, content;

			if (isPreview === false) {
				// Give flag that tell the editor enter preview mode
				e.showPreview();
			} else {
				e.hidePreview();
			}
		}

	};

	var util = {name: 'groupUtil', data: [cmdPreview]};

	var links = {name: 'groupLinks', data: [cmdLink, cmdImgLink]};

	var lists = {name: 'groupLists', data: [cmdList]};

	var addButtons = [[lists, links, util]];

	$("#edit-post").markdown({
		autofocus: true,
		savable: false,
		hideable: false,
		additionalButtons: addButtons,
		onSave: function (e) {
			var c = e.getContent();
			alert(markdown.toHTML(c));
			e.preventDefault();
		}
	});

	var options = {
		//dataType: 'json',
		success: function (data) {
			alert(JSON.stringify(data));
			if (data.status == "ok") {
				//here we should just load the new data that was returned
				alert(data.stauts + " " + data.message);
			}
			window.location = window.location;
		}
	};

	/*Event Handlers*/
	//upload post:
	$('#blogForm').submit(function (event) {
		event.preventDefault();
		$(this).ajaxSubmit(options);

		return false;
	});

});