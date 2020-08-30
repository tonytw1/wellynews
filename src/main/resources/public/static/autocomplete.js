$(function() {  	

	if ($('#tagName').length) {
		$("#tagName").autocomplete({
			source: "http://wellington.gen.nz/ajax/tags"});
	}
	
	if ($('#publisher').length) {
	    	$("#publisher").autocomplete({
        	    source: "/ajax/publishers"});
    	}
	
	if ($('#addTag').length) {
		$("#addTag").click(function() {
			var tagname = $('#tagName').val();
			$('#tags').append('<option value="' + tagname + '">' + tagname + '</options>');
			$('#tagName').val("");
		});
	}

	if ($('#removeTags').length) {	
		$("#removeTags").click(function() {
			$('#tags option:selected').remove();			
		});
	}
	
	if ($('#editForm').length) {
		$("#editForm").submit(function() {
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
                'R': 'RELATIONSHIP'};

		$("#geocode").autocomplete({

			source: function( request, response ) {
				$.ajax({
					url: "https://nominatim-ac.eelpieconsulting.co.uk/search",
					cache: true,
					dataType: "jsonp",
					jsonpCallback: 'placeAutocomplete',
					data: {
  						q: request.term,
                        country: 'nz',
                        lat: -41.2889,
                        lon: 174.7772
					},
					success: function( data ) {
						response( $.map( data, function( item ) {
						    itemType = itemTypes[item.type];
							return {
								label: (item.address + " (" + item.classification + "/" + itemType + ")"),
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
				$("#selectedGeocode").val( ui.item ? ui.item.osmId + "/" + itemType : "");
			}
		});
	}
	
});
