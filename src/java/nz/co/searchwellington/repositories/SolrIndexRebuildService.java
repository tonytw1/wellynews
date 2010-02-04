package nz.co.searchwellington.repositories;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nz.co.searchwellington.model.Resource;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;


public class SolrIndexRebuildService {

	private static final int BATCH_COMMIT_SIZE = 1000;

	Logger log = Logger.getLogger(SolrIndexRebuildService.class);

	private ResourceRepository resourceDAO;
	private SolrInputDocumentBuilder solrInputDocumentBuilder;
	
	private String solrUrl;

	
	public SolrIndexRebuildService(ResourceRepository resourceDAO, SolrInputDocumentBuilder solrInputDocumentBuilder) {		
		this.resourceDAO = resourceDAO;
		this.solrInputDocumentBuilder = solrInputDocumentBuilder;
	}

	
	public void setSolrUrl(String solrUrl) {
		this.solrUrl = solrUrl;
	}


	public boolean buildIndex() {		
		List<Integer> resourceIdsToIndex = resourceDAO.getAllResourceIds();
		log.info("Number of resources to update in solr index: " + resourceIdsToIndex.size());
	
		try {
			SolrServer solr = new CommonsHttpSolrServer(solrUrl);
			if (resourceIdsToIndex.size() > 0) {
				reindexResources(resourceIdsToIndex, solr);
				return true;
			}
			
		} catch (SolrServerException e) {
			log.error("Exception during index rebuild", e);
		} catch (IOException e) {
			log.error("Exception during index rebuild", e);
		}		
		return false;
	}
	
	
	private void reindexResources(List<Integer> resourceIdsToIndex, SolrServer solr) throws SolrServerException, IOException {		
		List<Integer> all = new ArrayList<Integer>(resourceIdsToIndex);
		int batchCounter = 0;
		
		while (all.size() > batchCounter + BATCH_COMMIT_SIZE) {
			List<Integer> batch = all.subList(batchCounter, batchCounter + BATCH_COMMIT_SIZE);
			log.info("Processing batch starting at " + batchCounter + " / " + all.size());
			reindexBatch(batch, solr);
			batchCounter = batchCounter + BATCH_COMMIT_SIZE;			
		}
		
		List<Integer> batch = all.subList(batchCounter, all.size()-1);
		reindexBatch(batch, solr);
		solr.optimize();
	}


	private void reindexBatch(List<Integer> batch, SolrServer solr) throws SolrServerException, IOException {		
		UpdateRequest updateRequest = new UpdateRequest();
		for (Integer id : batch) {
			Resource resource = resourceDAO.loadResourceById(id);
			log.info("Adding solr record: " + resource.getId() + " - " + resource.getName() + " - " + resource.getType());			
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
