$(function() {  	

	if ($('#tagName').length) {
		$("#tagName").autocomplete({source: "/ajax/tags"});
	}
	
	if ($('#addTag').length) {
		$("#addTag").click(function() {
			var tagList = $('#tagList');

			var tagName = $('#tagName').val();
		    $("#tags option").each(function( index ) {
                var option = $("#tags option")[index];
                var optionLabel = $(option).text();
				var selectedTagId = $(option).value();
				console.log("Selected tag id: " + selectedTagId);
                if (optionLabel === tagName) {
                    $(option).prop("selected", true);
                    $(option)[0].scrollIntoView();
					console.log($(option));
					tagList.append('<li data-tag="' + selectedTagId + '">' + tagName + "</li>");
				}
            });
            $("#tagName").val("");
		});
	}

	if ($('#removeTags').length) {	
		$("#removeTags").click(function() {
			$('#tags option:selected').remove();			
		});
	}

});
