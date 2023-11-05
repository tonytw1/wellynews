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
                if (optionLabel === tagName) {
                    $(option).prop("selected", true);
                    $(option)[0].scrollIntoView();
					console.log($(option));
					tagList.append('<l data-tag="' + $(option)[0] + '">' + tagName + "</li>");
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
