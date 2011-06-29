package nz.co.searchwellington.repositories;

import java.util.List;
import java.util.Set;

import nz.co.searchwellington.dates.DateFormatter;
import nz.co.searchwellington.htmlparsing.SnapshotBodyExtractor;
import nz.co.searchwellington.model.Comment;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.HandTagging;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.PublishedResource;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.UrlWordsGenerator;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.solr.indexing.SolrGeotagHandler;
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService;

import org.apache.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;

public class SolrInputDocumentBuilder {
	
	private static Logger log = Logger.getLogger(SolrInputDocumentBuilder.class);
	
	private SnapshotBodyExtractor snapshotBodyExtractor;
	private TaggingReturnsOfficerService taggingReturnsService;
	private HandTaggingDAO handTaggingDAO;
	
	public SolrInputDocumentBuilder(SnapshotBodyExtractor snapshotBodyExtractor, TaggingReturnsOfficerService taggingReturnsService, HandTaggingDAO handTaggingDAO) {
		this.snapshotBodyExtractor = snapshotBodyExtractor;
		this.taggingReturnsService = taggingReturnsService;
		this.handTaggingDAO = handTaggingDAO;
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
			inputDocument.addField("pageUrl", UrlWordsGenerator.markUrlForNewsitem(newsitem));
			log.info(UrlWordsGenerator.markUrlForNewsitem(newsitem));
			
			List<Comment> comments = newsitem.getComments();
			if(comments.size() > 0) {
				inputDocument.addField("commented", 1);
				for (Comment comment : comments) {
					if (comment != null) {
						inputDocument.addField("comment", comment.getTitle());
					}
				}
				
			} else {
				inputDocument.addField("commented", 0);
			}
			
			inputDocument.addField("twitterCount", newsitem.getReTwits().size());			
		}
		
		
		if (resource.getType().equals("F")) {
			inputDocument.addField("feedLatestItemDate", ((Feed) resource).getLatestItemDate());			
		}
		
		
		for(HandTagging handTagging : handTaggingDAO.getHandTaggingsForResource(resource)) {			
			final int userId = handTagging.getUser().getId();
			inputDocument.addField("handTaggingUsers", userId);	// TODO minimise?
			
			final String userTag = userId + ":" + handTagging.getTag().getId();
			inputDocument.addField("handTaggingUserTags", userTag);
		}
		
		for(Tag tag: taggingReturnsService.getHandTagsForResource(resource)) {
			inputDocument.addField("handTags", tag.getId());
		}
		
		final Set<Tag> indexTagsForResource = taggingReturnsService.getIndexTagsForResource(resource);
		for(Tag tag: indexTagsForResource) {
			inputDocument.addField("tags", tag.getId());
		}
	
		inputDocument = new SolrGeotagHandler().processGeotags(resource, indexTagsForResource, inputDocument);
		
		Website publisher = getIndexPublisherForResource(resource);
		if (publisher != null) {
			inputDocument.addField("publisher", publisher.getId());
			inputDocument.addField("publisherName", publisher.getName());
		}
		
		final String bodyText = snapshotBodyExtractor.extractSnapshotBodyTextFor(resource);
		if (bodyText != null) {
			inputDocument.addField("bodytext", bodyText);
		}
		
		return inputDocument;
	}
	
	private Website getIndexPublisherForResource(Resource resource) {
		Website publisher = null;
		if (resource.getType().equals("N") || resource.getType().equals("F") || resource.getType().equals("L")) {	// TODO instance of required
			publisher = ((PublishedResource) resource).getPublisher();
		}
		return publisher;
	}
	
}
