$(function() {  	

	if ($('#tagName').length) {
		$("#tagName").autocomplete({source: "/ajax/tags"});
	}
	
	if ($('#addTag').length) {
		$("#addTag").click(function() {
			var tagName = $('#tagName').val();
		    $("#tags option").each(function( index ) {
                var option = $("#tags option")[index];
                var optionLabel = $(option).text();
                if (optionLabel == tagName) {
                    $(option).prop("selected", true);
                    $(option)[0].scrollIntoView();
                }
            });
            $("#tagName").val("");

			var tagList = $('#tagList');
			tagList.append("<li>" + tagName + "</li>");
		});
	}

	if ($('#removeTags').length) {	
		$("#removeTags").click(function() {
			$('#tags option:selected').remove();			
		});
	}

});
