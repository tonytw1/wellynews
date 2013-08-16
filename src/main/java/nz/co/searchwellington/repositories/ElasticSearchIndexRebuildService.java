package nz.co.searchwellington.repositories;

import java.util.List;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.elasticsearch.ElasticSearchIndexUpdateService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;

@Component
public class ElasticSearchIndexRebuildService {

	private static Logger log = Logger.getLogger(ElasticSearchIndexRebuildService.class);

	private static final int BATCH_COMMIT_SIZE = 10;
	
	private HibernateResourceDAO resourceDAO;
	private final ElasticSearchIndexUpdateService elasticSearchIndexUpdateService;
    	
    private boolean running;

	@Autowired
	public ElasticSearchIndexRebuildService(HibernateResourceDAO resourceDAO, ElasticSearchIndexUpdateService elasticSearchIndexUpdateService) {		
		this.resourceDAO = resourceDAO;
		this.elasticSearchIndexUpdateService = elasticSearchIndexUpdateService;
		this.running = false;
	}

	public boolean buildIndex(boolean deleteAll) throws JsonProcessingException {
		if (running) {
			log.warn("The index builder is already running; cannot start another process");
			return false;
		}
		
		running = true;
		List<Integer> resourceIdsToIndex = resourceDAO.getAllResourceIds();
		log.info("Number of resources to update in solr index: " + resourceIdsToIndex.size());

		if (resourceIdsToIndex.size() > 0) {
			reindexResources(resourceIdsToIndex);
			running = false;
			return true;
		}
		
		running = false;
		return false;
	}
	
	private void reindexResources(List<Integer> resourceIdsToIndex) throws JsonProcessingException {		
		int batchCounter = 0;		
		final List<Integer> all = Lists.newArrayList(resourceIdsToIndex);
		while (all.size() > batchCounter + BATCH_COMMIT_SIZE) {
			List<Integer> batch = all.subList(batchCounter, batchCounter + BATCH_COMMIT_SIZE);
			log.info("Processing batch starting at " + batchCounter + " / " + all.size());
			reindexBatch(batch);
			batchCounter = batchCounter + BATCH_COMMIT_SIZE;			
		}
		
		List<Integer> batch = all.subList(batchCounter, all.size()-1);
		reindexBatch(batch);
		log.info("Index rebuild complete");
	}
	
	private void reindexBatch(List<Integer> batch) throws JsonProcessingException {
		List<Resource> resources = Lists.newArrayList();
		for (Integer id : batch) {
			Resource resource = resourceDAO.loadResourceById(id);
			log.debug("Adding solr record: " + resource.getId() + " - " + resource.getName() + " - " + resource.getType());			
			resources.add(resource);			
			elasticSearchIndexUpdateService.updateMultipleContentItems(resources);			
		}
	}
	
}
