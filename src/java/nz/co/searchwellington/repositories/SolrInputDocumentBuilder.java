package nz.co.searchwellington.repositories;

import java.util.HashSet;
import java.util.Set;

import nz.co.searchwellington.dates.DateFormatter;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.PublishedResource;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.Website;

import org.apache.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;

public class SolrInputDocumentBuilder {
	
	Logger log = Logger.getLogger(SolrInputDocumentBuilder.class);
	
	public SolrInputDocument buildResouceInputDocument(Resource resource) {
		SolrInputDocument inputDocument = new SolrInputDocument();
		inputDocument.addField("id", resource.getId());
		inputDocument.addField("name", resource.getName());
		inputDocument.addField("type", resource.getType());
		inputDocument.addField("httpStatus", resource.getHttpStatus());
		inputDocument.addField("description", resource.getDescription());
		
		inputDocument.addField("date", resource.getDate());
		inputDocument.addField("month", new DateFormatter().formatDate(resource.getDate(), DateFormatter.MONTH_FACET));
		inputDocument.addField("lastLive", resource.getLiveTime());
		
		if (resource.getLastChanged() != null) {
			inputDocument.addField("lastChanged", resource.getLastChanged());
		}
		
		if (resource.getType().equals("N") && ((Newsitem) resource).getComments().size() > 0) {
			inputDocument.addField("commented", 1);
		} else {
			inputDocument.addField("commented", 0);
		}
		
		if (resource.getGeocode() != null && resource.getGeocode().isValid()) {
			inputDocument.addField("geotagged", true);
		} else {
			inputDocument.addField("geotagged", false);
		}
		
		for(Tag tag: getIndexTagsForResource(resource)) {
			inputDocument.addField("tags", tag.getId());
		}
		
		Website publisher = getIndexPublisherForResource(resource);
		if (publisher != null) {
			inputDocument.addField("publisher", publisher.getId());
		}
		return inputDocument;
	}


	private Website getIndexPublisherForResource(Resource resource) {
		Website publisher = null;
		if (resource.getType().equals("N")){
			publisher = ((Newsitem) resource).getPublisher();
		}
		return publisher;
	}

	
	private Set<Tag> getIndexTagsForResource(Resource resource) {	
		Set <Tag> indexTags = new HashSet<Tag>();
		indexTags.addAll(resource.getTags());
		
		final boolean shouldAppearOnPublisherAndParentTagPages = 
		    resource.getType().equals("L") || resource.getType().equals("N")
		    || resource.getType().equals("C") || resource.getType().equals("F");
				
		if (shouldAppearOnPublisherAndParentTagPages) {            
		    Set <Tag> existingTags = new HashSet<Tag>(indexTags);
		    for (Tag tag : existingTags) {
		        indexTags.addAll(tag.getAncestors());
		    }
		    
		    if (((PublishedResource) resource).getPublisher() != null) {              
		        for (Tag publisherTag : ((PublishedResource) resource).getPublisher().getTags()) {                
		            log.debug("Adding publisher tag " + publisherTag.getName() + " to record.");
		            indexTags.add(publisherTag);
		            indexTags.addAll(publisherTag.getAncestors());
		        }
		    }
		}
		
		return indexTags;
	}

}
