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

	$(".editForm").on('click', '.removeTag',function(event) {
		console.log(this);
		var selectedTagId = this.attr("data-tag");
		console.log("Remove " + selectedTagId);
		$("#tags option").each(function( index ) {
			var option = $("#tags option")[index];
			var tagId = $(option).val();
			if (selectedTagId === tagId) {
				$(option).prop("selected", false);
			}
		});
		this.remove();
	});

});
