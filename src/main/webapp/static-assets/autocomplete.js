$(function() {  	

	if ($('#tagName').length) {
		$("#tagName").autocomplete({
			source: "http://wellington.gen.nz/ajax/tags"});
	}
	
	if ($('#publisherName').length) {
	    	$("#publisherName").autocomplete({
        	    source: "http://wellington.gen.nz/ajax/publishers"});
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
	}

	if ($('#geocode').length) {

	       itemTypes={
        	        'N': 'NODE',
                	'W': 'WAY',
                	'R': 'RELATIONSHIP'};

		$("#geocode").autocomplete({

			source: function( request, response ) {
				$.ajax({
					url: "http://nominatim-ac.eelpieconsulting.co.uk/search",
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
