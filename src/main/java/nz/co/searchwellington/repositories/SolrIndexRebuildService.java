package nz.co.searchwellington.repositories;

import java.io.IOException;
import java.util.List;

import nz.co.searchwellington.model.Resource;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
public class SolrIndexRebuildService {

	private static Logger log = Logger.getLogger(SolrIndexRebuildService.class);

	private static final int BATCH_COMMIT_SIZE = 1000;
	
	private HibernateResourceDAO resourceDAO;
	private SolrInputDocumentBuilder solrInputDocumentBuilder;
    
	@Value("#{config['solr.url']}")
	private String solrUrl;
	
    private boolean running;
	
	public SolrIndexRebuildService() {
		running = false;
	}

	@Autowired
	public SolrIndexRebuildService(HibernateResourceDAO resourceDAO, SolrInputDocumentBuilder solrInputDocumentBuilder) {		
		this.resourceDAO = resourceDAO;
		this.solrInputDocumentBuilder = solrInputDocumentBuilder;
	}

	
	public void setSolrUrl(String solrUrl) {
		this.solrUrl = solrUrl;
	}


	public boolean buildIndex(boolean deleteAll) {
		if (running) {
			log.warn("The index builder is already running; cannot start another process");
			return false;
		}
				
		running = true;
		List<Integer> resourceIdsToIndex = resourceDAO.getAllResourceIds();
		log.info("Number of resources to update in solr index: " + resourceIdsToIndex.size());
	
		try {
			SolrServer solr = new CommonsHttpSolrServer(solrUrl);	// TODO This should be injected
			if (deleteAll) {
				log.info("Deleting all existing records");
				this.deleteAllFromIndex(solr);
			}
			if (resourceIdsToIndex.size() > 0) {
				reindexResources(resourceIdsToIndex, solr);
				running = false;
				return true;
			}
			
		} catch (Exception e) {
			log.error("Exception during index rebuild", e);
		}	
		running = false;
		return false;
	}
	
	
	private void reindexResources(List<Integer> resourceIdsToIndex, SolrServer solr) throws SolrServerException, IOException {		
		int batchCounter = 0;		
		final List<Integer> all = Lists.newArrayList(resourceIdsToIndex);
		while (all.size() > batchCounter + BATCH_COMMIT_SIZE) {
			List<Integer> batch = all.subList(batchCounter, batchCounter + BATCH_COMMIT_SIZE);
			log.info("Processing batch starting at " + batchCounter + " / " + all.size());
			reindexBatch(batch, solr);
			batchCounter = batchCounter + BATCH_COMMIT_SIZE;			
		}
		
		List<Integer> batch = all.subList(batchCounter, all.size()-1);
		reindexBatch(batch, solr);
		log.info("Optimizing");
		solr.optimize();
		log.info("Index rebuild complete");
	}
	
	private void reindexBatch(List<Integer> batch, SolrServer solr) throws SolrServerException, IOException {		
		UpdateRequest updateRequest = new UpdateRequest();
		for (Integer id : batch) {
			Resource resource = resourceDAO.loadResourceById(id);
			log.debug("Adding solr record: " + resource.getId() + " - " + resource.getName() + " - " + resource.getType());			
			SolrInputDocument inputDocument = solrInputDocumentBuilder.buildResouceInputDocument(resource);			
			updateRequest.add(inputDocument);
		}
		processAndCommit(solr, updateRequest);
	}
	
	private void processAndCommit(SolrServer solr, UpdateRequest updateRequest) throws SolrServerException, IOException {
		log.info("Committing");
		updateRequest.process(solr);				
		solr.commit();
	}
	
	private void deleteAllFromIndex(SolrServer solr) throws SolrServerException, IOException {
		final String deleteAll = "*:*";
		UpdateResponse deleteAllQuery = solr.deleteByQuery(deleteAll);
		log.info(deleteAllQuery.toString());
		solr.commit(true, true);			
		solr.optimize();
	}
	
}
