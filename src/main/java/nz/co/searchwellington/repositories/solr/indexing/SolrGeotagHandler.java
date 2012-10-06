package nz.co.searchwellington.repositories.solr.indexing;

import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService;

import org.apache.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;

public class SolrGeotagHandler {
	
	private static Logger log = Logger.getLogger(SolrGeotagHandler.class);
	
	TaggingReturnsOfficerService taggingReturnsOfficerService;
	
	public SolrGeotagHandler(TaggingReturnsOfficerService taggingReturnsOfficerService) {
		this.taggingReturnsOfficerService = taggingReturnsOfficerService;
	}

	public SolrInputDocument processGeotags(Resource resource, SolrInputDocument inputDocument) {
		final Geocode geocode = taggingReturnsOfficerService.getIndexGeocodeForResource(resource);
		if (geocode != null && geocode.isValid()) {
			applyGeotagToIndexDocument(inputDocument, geocode);
		} else {
			inputDocument.addField("geotagged", false);
		}
		return inputDocument;
	}
	
	private void applyGeotagToIndexDocument(SolrInputDocument inputDocument, final Geocode geocode) {
		if (geocode != null && geocode.isValid()) {
			log.info("Applying geotag '" + geocode.getAddress() + "' to resource");
			inputDocument.addField("geotagged", true);
			inputDocument.addField("address", geocode.getAddress());
			inputDocument.addField("position", geocode.getLatitude() + "," + geocode.getLongitude());
			inputDocument.addField("osm_id", geocode.getOsmPlaceId());
		}
	}
	
}
