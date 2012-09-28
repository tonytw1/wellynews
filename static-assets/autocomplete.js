$(function() {  	

	$("#tagName").autocomplete({
		source: "http://wellington.gen.nz/ajax/tags"});
	
    $("#publisherName").autocomplete({
            source: "http://wellington.gen.nz/ajax/publishers"});
    
	$("#addTag").click(function() {
		var tagname = $('#tagName').val();
		$('#tags').append('<option value="' + tagname + '">' + tagname + '</options>');
		$('#tagName').val("");
	});
			
	$("#removeTags").click(function() {
		$('#tags option:selected').remove();			
	});
			
	$("#editForm").submit(function() {
		$('#tags option').attr('selected', 'selected');
	});				 
	
	$("#geocode").autocomplete({
		source: function( request, response ) {
			$.ajax({
				url: "http://nominatim-ac.eelpieconsulting.co.uk/suggest",
				dataType: "jsonp",
				data: {
					term: request.term
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
			$("#selectedGeocode").val( ui.item ? ui.item.osmId + "/" + ui.item.osmType : "");
		}
	});

});