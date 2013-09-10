package nz.co.searchwellington.repositories.elasticsearch;

import java.util.List;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.model.mappers.FrontendResourceMapper;

import org.apache.log4j.Logger;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
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
	private final FrontendResourceMapper frontendResourceMapper;
	private final ObjectMapper mapper;
	
	@Autowired
	public ElasticSearchIndexUpdateService(ElasticSearchClientFactory elasticSearchClientFactory,
			FrontendResourceMapper frontendResourceMapper) {
		this.elasticSearchClientFactory = elasticSearchClientFactory;
		this.frontendResourceMapper = frontendResourceMapper;
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
		final FrontendResource frontendContentItem = frontendResourceMapper.createFrontendResourceFrom(contentItem);
		
		final String json = mapper.writeValueAsString(frontendContentItem);
		log.debug("Updating elastic search with json: " + json);
		return client.prepareIndex(INDEX, TYPE, Integer.toString(contentItem.getId())).setSource(json);
	}
	
}
