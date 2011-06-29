package nz.co.searchwellington.repositories.solr.indexing;

import java.util.Set;

import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;

import org.apache.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;

public class SolrGeotagHandler {
	
	private static Logger log = Logger.getLogger(SolrGeotagHandler.class);
	
	public SolrInputDocument processGeotags(Resource resource, Set<Tag> indexTagsForResource, SolrInputDocument inputDocument) {
		Geocode geocode = resource.getGeocode();
		if (geocode != null && geocode.isValid()) {
			applyGeotagToIndexDocument(inputDocument, geocode);
		} else {
			applyGeotagToIndexDocument(inputDocument, getGeotagFromFirstResourceTagWithLocation(indexTagsForResource));			
		}
		return inputDocument;
	}
	
	private Geocode getGeotagFromFirstResourceTagWithLocation(Set<Tag> indexTagsForResource) {
		for (Tag tag : indexTagsForResource) {
			if (tag.getGeocode() != null && tag.getGeocode().isValid()) {
				log.info("Found subsitute geotag for resource on resource index tag: " + tag.getName());
				return tag.getGeocode();
			}
		}
		return null;
	}
	
	private void applyGeotagToIndexDocument(SolrInputDocument inputDocument, final Geocode geocode) {
		if (geocode != null && geocode.isValid()) {
			log.info("Appling geotag '" + geocode.getAddress() + "' to resource");
			inputDocument.addField("geotagged", true);
			inputDocument.addField("address", geocode.getAddress());
			inputDocument.addField("position", geocode.getLatitude() + "," + geocode.getLongitude());
		} else {
			inputDocument.addField("geotagged", false);
		}		
	}
	
}
