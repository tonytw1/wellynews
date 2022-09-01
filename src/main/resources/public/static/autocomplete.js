$(function() {  	

	if ($('#tagName').length) {
		$("#tagName").autocomplete({source: "/ajax/tags"});
	}
	
	if ($('#publisher').length) {
	    	$("#publisher").autocomplete({
        	    source: "/ajax/publishers"});
    	}
	
	if ($('#addTag').length) {
		$("#addTag").click(function() {
			var tagName = $('#tagName').val();
            var selector = 'option:contains("' + tagName + '")';
            var tagOption = $('#tags ' + selector);
            tagOption[0].scrollIntoView();
            tagOption.prop("selected", true);
		});
	}

	if ($('#removeTags').length) {	
		$("#removeTags").click(function() {
			$('#tags option:selected').remove();			
		});
	}

	// Edit form behaviours
	if ($('.editForm').length) {
		$(".editForm").submit(function() {
			$('#tags option').attr('selected', 'selected');
		});

        // When a URL is entered we can potentially autofill the publisher and page title.
        $("[name='url']").change(function() {
            var url = $(this).val();
            // If the publisher is blank we should try to auto fill it based on the publisher url
            var publisherField = $("[name='publisher']");
            if (publisherField && publisherField.val() == "") {
                $.ajax({
                    url: "/ajax/publisher-guess",
                    data: { url: url},
                    success: function (data, status, xhr) {
                        if (data.length > 0) {
                            var p = data[0];
                            publisherField.val(p);
                        }
                    },
                });
            }

            // If the title is blank we should try to auto fill it based on the pages HTML title.
            var titleField = $("[name='title']");
            if (titleField && titleField.val() == "") {
                $.ajax({
                    url: "/ajax/title-autofill",
                    data: { url: url},
                    success: function (data, status, xhr) {
                        if (data.length > 0) {
                            titleField.val(data);
                        }
                    },
                });
            }
        });
    }

	if ($('#geocode').length) {
       itemTypes={
                'N': 'NODE',
                'W': 'WAY',
                'R': 'RELATION'};

		$("#geocode").autocomplete({
		    search: function( event, ui ) {
    		    $("#osm").val("");
                $("#geocodeStatus").text("");
		    },
		    change: function( event, ui ) {
		        var osm = $("#osm").val();
		        var selected = osm != "";
		        if (!selected) {
		            $("#geocode").val("");
		            $("#geocodeStatus").text("");
		        }
		    },
			source: function( request, response ) {
				$.ajax({
					url: "https://nominatim-ac.eelpieconsulting.co.uk/search",
					cache: true,
					data: {
  						q: request.term,
                        country: 'nz',
                        lat: -41.2889,
                        lon: 174.7772
					},
					success: function( data ) {
						response( $.map( data, function( item ) {
							return {
								label: (item.address + " (" + item.classification + "/" + item.type + ")"),
								value: item.address,
								osmId: item.osmId,
								osmType: item.osmType
							}
						}));
					}
				});
			},			
			select: function( event, ui ) {
				itemType = itemTypes[ui.item.osmType];
				$("#osm").val( ui.item ? ui.item.osmId + "/" + itemType : "");
                $("#geocodeStatus").text("✅");
			}
		});
	}
	
});
