package nz.co.searchwellington.repositories;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Set;

import nz.co.searchwellington.model.Resource;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;


public class SolrIndexRebuildService {

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


	public void buildIndex() {		
		Set<Integer> resourceIdsToIndex = resourceDAO.getAllResourceIds();
		log.info("Number of resources to update in solr index: " + resourceIdsToIndex.size());
		try {
			SolrServer solr = new CommonsHttpSolrServer(solrUrl);
			deleteAllFromIndex(solr);
	
			if (resourceIdsToIndex.size() > 0) {
				reindexResources(resourceIdsToIndex, solr);			
			}
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	
	private void deleteAllFromIndex(SolrServer solr) throws SolrServerException, IOException {
		final String deleteAll = "*:*";
		UpdateResponse deleteAllQuery = solr.deleteByQuery(deleteAll);
		log.info(deleteAllQuery.toString());
		solr.commit(true, true);			
		solr.optimize();
	}

	
	private void reindexResources(Set<Integer> resourceIdsToIndex, SolrServer solr) throws SolrServerException, IOException {
		UpdateRequest updateRequest = new UpdateRequest();
		for (Integer id : resourceIdsToIndex) {
			Resource resource = resourceDAO.loadResourceById(id);
			log.info("Adding solr record: " + resource.getId() + " - " + resource.getName() + " - " + resource.getType());			
			SolrInputDocument inputDocument = solrInputDocumentBuilder.buildResouceInputDocument(resource);
		updateRequest.add(inputDocument);
		}
		
		updateRequest.process(solr);
		solr.commit();
		solr.optimize();
	}
	
}
