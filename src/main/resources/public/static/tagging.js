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
				var selectedTagId = $(option).val();
				console.log("Selected tag id: " + selectedTagId);
                if (optionLabel === tagName) {
                    $(option).prop("selected", true);
                    $(option)[0].scrollIntoView();
					tagList.append('<li data-tag="' + selectedTagId + '">' + tagName + "<span class=\"removeTag\" data-tag=\"" + selectedTagId + "\">X</span></li>");
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
