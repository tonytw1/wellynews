package nz.co.searchwellington.repositories.elasticsearch;

import java.util.List;

import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.UrlWordsGenerator;
import nz.co.searchwellington.model.frontend.FrontendFeed;
import nz.co.searchwellington.model.frontend.FrontendImage;
import nz.co.searchwellington.model.frontend.FrontendNewsitem;
import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.model.frontend.FrontendTag;
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService;
import nz.co.searchwellington.views.GeocodeToPlaceMapper;

import org.apache.log4j.Logger;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ElasticSearchIndexUpdateService {

	public static final String INDEX = "searchwellington";	// TODO config
	public static final String TYPE = "resources";	// TODO config
	
	private static Logger log = Logger.getLogger(ElasticSearchIndexUpdateService.class);
	
	private final ElasticSearchClientFactory elasticSearchClientFactory;
	private final TaggingReturnsOfficerService taggingReturnsOfficerService;
	private final ObjectMapper mapper;
	private final UrlWordsGenerator urlWordsGenerator;
	private final GeocodeToPlaceMapper geocodeToPlaceMapper;
	
	@Autowired
	public ElasticSearchIndexUpdateService(ElasticSearchClientFactory elasticSearchClientFactory, 
			TaggingReturnsOfficerService taggingReturnsOfficerService,
			UrlWordsGenerator urlWordsGenerator, GeocodeToPlaceMapper geocodeToPlaceMapper) {
		this.elasticSearchClientFactory = elasticSearchClientFactory;
		this.taggingReturnsOfficerService = taggingReturnsOfficerService;
		this.urlWordsGenerator = urlWordsGenerator;
		this.geocodeToPlaceMapper = geocodeToPlaceMapper;
		
		this.mapper = new ObjectMapper();
	    this.mapper.configure(MapperFeature.USE_ANNOTATIONS, true);	    
	}
	
	public void updateSingleContentItem(Resource contentItem) {
		log.debug("Updating content item: " + contentItem.getId());		
		try {
			final Client client = elasticSearchClientFactory.getClient();
			prepateUpdateFor(contentItem, client).execute().actionGet();			
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void deleteContentItem(Integer id) {
		log.info("Deleting content item: " + id);
		elasticSearchClientFactory.getClient().prepareDelete(INDEX, TYPE, Integer.toString(id)).setOperationThreaded(false).execute().actionGet();
	}
	
	public void updateMultipleContentItems(List<Resource> contentItems) {
		log.debug("Updating content items");
		if (contentItems.isEmpty()) {
			log.warn("Ignoring empty index update request");
			return;
		}
		
		final Client client = elasticSearchClientFactory.getClient();
		final BulkRequestBuilder bulkRequest = client.prepareBulk();
		for (Resource contentItem : contentItems) {
			try {
				bulkRequest.add(prepateUpdateFor(contentItem, client));			
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		}
		
		log.info("Executing bulk request with " + bulkRequest.numberOfActions() + " actions");
		bulkRequest.execute().actionGet();		
	}
	
	private IndexRequestBuilder prepateUpdateFor(Resource contentItem, Client client) throws JsonProcessingException {
		FrontendResource frontendContentItem = new FrontendResource();
		if (contentItem.getType().equals("N")) {
			final Newsitem contentItemNewsitem = (Newsitem) contentItem;
			FrontendNewsitem frontendNewsitem = new FrontendNewsitem();
			frontendNewsitem.setPublisherName(contentItemNewsitem.getPublisherName());
			frontendNewsitem.setAcceptedFromFeedName(contentItemNewsitem.getFeed() != null ? contentItemNewsitem.getFeed().getName() : null);
			frontendNewsitem.setAcceptedByProfilename(contentItemNewsitem.getAcceptedBy() != null ? contentItemNewsitem.getAcceptedBy().getProfilename() : null);
			frontendNewsitem.setAccepted(contentItemNewsitem.getAccepted());
			if (contentItemNewsitem.getImage() != null) {
				frontendNewsitem.setFrontendImage(new FrontendImage(contentItemNewsitem.getImage().getUrl()));
			}
			frontendContentItem = frontendNewsitem;
		}
		
		if (contentItem.getType().equals("F")) {
			FrontendFeed frontendFeed = new FrontendFeed();
			Feed contentItemFeed = (Feed) contentItem;
			frontendFeed.setPublisherName(contentItemFeed.getPublisherName());	
			frontendFeed.setLatestItemDate(contentItemFeed.getLatestItemDate());
			frontendContentItem = frontendFeed;
		}
		
		frontendContentItem.setId(contentItem.getId());
		frontendContentItem.setType(contentItem.getType());
		frontendContentItem.setName(contentItem.getName());
		frontendContentItem.setUrl(contentItem.getUrl());
		frontendContentItem.setDate(contentItem.getDate());
		frontendContentItem.setDescription(contentItem.getDescription());
		frontendContentItem.setHttpStatus(contentItem.getHttpStatus());
		
		frontendContentItem.setUrlWords(urlWordsGenerator.makeUrlWordsFromName(contentItem.getName()));
		if (frontendContentItem.getType().equals("N")) {
			frontendContentItem.setUrlWords(urlWordsGenerator.makeUrlForNewsitem((FrontendNewsitem) frontendContentItem));
		} else if (frontendContentItem.getType().equals("F")) {
			frontendContentItem.setUrlWords("/feed/" + urlWordsGenerator.makeUrlWordsFromName(contentItem.getName()));
		}
		
		final List<FrontendTag> tags = Lists.newArrayList();
		for (Tag tag : Lists.newArrayList(taggingReturnsOfficerService.getIndexTagsForResource(contentItem))) {
			tags.add(mapTagToFrontendTag(tag));
		}
		frontendContentItem.setTags(tags);
		
		final List<FrontendTag> handTags = Lists.newArrayList();
		for (Tag tag : taggingReturnsOfficerService.getHandTagsForResource(contentItem)) {
			handTags.add(mapTagToFrontendTag(tag));
		}
		frontendContentItem.setHandTags(handTags);
		
		final Geocode contentItemGeocode = taggingReturnsOfficerService.getIndexGeocodeForResource(contentItem);
		if (contentItemGeocode != null) {
			frontendContentItem.setPlace(geocodeToPlaceMapper.mapGeocodeToPlace(contentItemGeocode));
		}
				
		final String json = mapper.writeValueAsString(frontendContentItem);
		log.debug("Updating elastic search with json: " + json);
		return client.prepareIndex(INDEX, TYPE, Integer.toString(contentItem.getId())).setSource(json);
	}

	private FrontendTag mapTagToFrontendTag(Tag tag) {
		final FrontendTag frontendTag = new FrontendTag();
		frontendTag.setId(tag.getName());
		frontendTag.setName(tag.getDisplayName());
		return frontendTag;
	}
	
}
