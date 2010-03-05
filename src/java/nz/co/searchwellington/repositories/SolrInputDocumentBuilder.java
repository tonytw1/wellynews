package nz.co.searchwellington.repositories;

import java.util.List;

import nz.co.searchwellington.dates.DateFormatter;
import nz.co.searchwellington.htmlparsing.SnapshotBodyExtractor;
import nz.co.searchwellington.model.Comment;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.PublishedResource;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService;

import org.apache.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;

public class SolrInputDocumentBuilder {
	
	Logger log = Logger.getLogger(SolrInputDocumentBuilder.class);
	
	private SnapshotBodyExtractor snapshotBodyExtractor;
	private TaggingReturnsOfficerService taggingReturnsService;

	
	
	public SolrInputDocumentBuilder(SnapshotBodyExtractor snapshotBodyExtractor, TaggingReturnsOfficerService taggingReturnsService) {
		this.snapshotBodyExtractor = snapshotBodyExtractor;
		this.taggingReturnsService = taggingReturnsService;
	}


	public SolrInputDocument buildResouceInputDocument(Resource resource) {
		SolrInputDocument inputDocument = new SolrInputDocument();
		inputDocument.addField("id", resource.getId());
		inputDocument.addField("title", resource.getName());
		inputDocument.addField("url", resource.getUrl());
		inputDocument.addField("type", resource.getType());
		inputDocument.addField("httpStatus", resource.getHttpStatus());
		inputDocument.addField("description", resource.getDescription());
		
		inputDocument.addField("date", resource.getDate());
		inputDocument.addField("month", new DateFormatter().formatDate(resource.getDate(), DateFormatter.MONTH_FACET));
		inputDocument.addField("lastLive", resource.getLiveTime());
		inputDocument.addField("embargoedUntil", resource.getEmbargoedUntil());
		inputDocument.addField("held", resource.isHeld());
		
		if (resource.getLastChanged() != null) {
			inputDocument.addField("lastChanged", resource.getLastChanged());
		}
		
		if (resource.getType().equals("N")) {
			Newsitem newsitem = (Newsitem) resource;
			List<Comment> comments = newsitem.getComments();
			if(comments.size() > 0) {
				inputDocument.addField("commented", 1);
				for (Comment comment : comments) {
					inputDocument.addField("comment", comment.getTitle());
				}
				
				
			} else {
				inputDocument.addField("commented", 0);
			}
			
			inputDocument.addField("twitterCount", newsitem.getReTwits().size());			
		}
		
		
		if (resource.getType().equals("F")) {
			inputDocument.addField("feedLatestItemDate", ((Feed) resource).getLatestItemDate());			
		}
		
		
		
		if (resource.getGeocode() != null && resource.getGeocode().isValid()) {
			inputDocument.addField("geotagged", true);
		} else {
			inputDocument.addField("geotagged", false);
		}
		
		for(Tag tag: taggingReturnsService.getIndexTagsForResource(resource)) {
			inputDocument.addField("tags", tag.getId());
		}
				
		Website publisher = getIndexPublisherForResource(resource);
		if (publisher != null) {
			inputDocument.addField("publisher", publisher.getId());
			inputDocument.addField("publisherName", publisher.getName());
		}
		
		// TODO not sure this is in the right place; should be under content update service.
		final String bodyText = snapshotBodyExtractor.extractSnapshotBodyTextFor(resource);
		if (bodyText != null) {
			inputDocument.addField("bodytext", bodyText);
		}
		
		return inputDocument;
	}


	private Website getIndexPublisherForResource(Resource resource) {
		Website publisher = null;
		if (resource.getType().equals("N") || resource.getType().equals("F")){
			publisher = ((PublishedResource) resource).getPublisher();
		}
		return publisher;
	}

	
	

}
