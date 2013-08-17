package nz.co.searchwellington.repositories.elasticsearch;

import java.util.List;

import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.PublishedResource;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.UrlWordsGenerator;
import nz.co.searchwellington.model.frontend.FrontendFeedImpl;
import nz.co.searchwellington.model.frontend.FrontendNewsitem;
import nz.co.searchwellington.model.frontend.FrontendNewsitemImpl;
import nz.co.searchwellington.model.frontend.FrontendResourceImpl;
import nz.co.searchwellington.model.frontend.FrontendTag;
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService;

import org.apache.log4j.Logger;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.geo.model.LatLong;
import uk.co.eelpieconsulting.common.geo.model.OsmId;
import uk.co.eelpieconsulting.common.geo.model.Place;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ElasticSearchIndexUpdateService {

	public static final String INDEX = "searchwellington";
	public static final String TYPE = "resources";
	
	private static Logger log = Logger.getLogger(ElasticSearchIndexUpdateService.class);
	
	private final ElasticSearchClientFactory elasticSearchClientFactory;
	private final TaggingReturnsOfficerService taggingReturnsOfficerService;
	private final ObjectMapper mapper;
	private final UrlWordsGenerator urlWordsGenerator;
	
	@Autowired
	public ElasticSearchIndexUpdateService(ElasticSearchClientFactory elasticSearchClientFactory, 
			TaggingReturnsOfficerService taggingReturnsOfficerService,
			UrlWordsGenerator urlWordsGenerator) {
		this.elasticSearchClientFactory = elasticSearchClientFactory;
		this.taggingReturnsOfficerService = taggingReturnsOfficerService;
		this.urlWordsGenerator = urlWordsGenerator;
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
	
	public void deleteContentItem(String contentId) {
		log.debug("Deleting content item: " + contentId);
		elasticSearchClientFactory.getClient().prepareDelete(INDEX, TYPE, contentId).setOperationThreaded(false).execute().actionGet();
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

	public void deleteResourceFromIndex(int id) {
		// TODO Auto-generated method stub		
	}
	
	private IndexRequestBuilder prepateUpdateFor(Resource contentItem, Client client) throws JsonProcessingException {
		FrontendResourceImpl frontendContentItem = new FrontendResourceImpl();
		if (contentItem.getType().equals("N")) {
			FrontendNewsitemImpl frontendNewsitem = new FrontendNewsitemImpl();
			frontendNewsitem.setPublisherName(((PublishedResource) contentItem).getPublisherName());			
			frontendContentItem = frontendNewsitem;
		}
		
		if (contentItem.getType().equals("F")) {
			FrontendFeedImpl frontendFeed = new FrontendFeedImpl();
			frontendFeed.setPublisherName(((PublishedResource) contentItem).getPublisherName());			
			frontendContentItem = frontendFeed;
		}
		
		frontendContentItem.setId(contentItem.getId());
		frontendContentItem.setType(contentItem.getType());
		frontendContentItem.setName(contentItem.getName());
		frontendContentItem.setUrl(contentItem.getUrl());
		frontendContentItem.setDate(contentItem.getDate());
		frontendContentItem.setDescription(contentItem.getDescription());
		frontendContentItem.setTags(contentItem.getTags());
		frontendContentItem.setHttpStatus(contentItem.getHttpStatus());
		
		frontendContentItem.setUrlWords(urlWordsGenerator.makeUrlWordsFromName(contentItem.getName()));
		if (frontendContentItem.getType().equals("N")) {
			frontendContentItem.setUrlWords(urlWordsGenerator.markUrlForNewsitem((FrontendNewsitem) frontendContentItem));
		} else if (frontendContentItem.getType().equals("F")) {
			frontendContentItem.setUrlWords("feed/" + urlWordsGenerator.makeUrlWordsFromName(contentItem.getName()));
		}
		
		final List<FrontendTag> tags = Lists.newArrayList();
		for (Tag tag : Lists.newArrayList(taggingReturnsOfficerService.getIndexTagsForResource(contentItem))) {
			final FrontendTag frontendTag = new FrontendTag();
			frontendTag.setId(tag.getName());
			frontendTag.setName(tag.getDisplayName());
			tags.add(frontendTag);
		}
		frontendContentItem.setTags(tags);
		
		final Geocode contentItemGeocode = taggingReturnsOfficerService.getIndexGeocodeForResource(contentItem);
		if (contentItemGeocode != null) {
			Place place = mapGeocodeToPlace(contentItemGeocode);
			frontendContentItem.setPlace(place);
		}
				
		final String json = mapper.writeValueAsString(frontendContentItem);
		log.debug("Updating elastic search with json: " + json);
		return client.prepareIndex(INDEX, TYPE, Integer.toString(contentItem.getId())).setSource(json);
	}

	private Place mapGeocodeToPlace(final Geocode contentItemGeocode) {	// TODO duplication
		LatLong latLong = null;
		if (contentItemGeocode.getLatitude() != null && contentItemGeocode.getLongitude() != null) {
			latLong = new LatLong(contentItemGeocode.getLatitude(), contentItemGeocode.getLongitude());
		}
		OsmId osmId = null;
		if (contentItemGeocode.getOsmId() != null && contentItemGeocode.getOsmType() != null) {
			osmId = new OsmId(contentItemGeocode.getOsmId(), contentItemGeocode.getOsmType());
		}
		String displayName = contentItemGeocode.getDisplayName();
		Place place = new Place(displayName, latLong, osmId);
		return place;
	}
	
}
