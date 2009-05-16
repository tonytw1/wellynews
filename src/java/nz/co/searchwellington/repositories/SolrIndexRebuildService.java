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
		
	
	public SolrIndexRebuildService(ResourceRepository resourceDAO, SolrInputDocumentBuilder solrInputDocumentBuilder) {		
		this.resourceDAO = resourceDAO;
		this.solrInputDocumentBuilder = solrInputDocumentBuilder;
	}

	
	public void buildIndex() {		
		Set<Integer> newsitemIdsToIndex = resourceDAO.getAllResourceIds();
		log.info("Number of resources to update in lucene index: " + newsitemIdsToIndex.size());
		try {
			SolrServer solr = new CommonsHttpSolrServer("http://localhost:8080/apache-solr-1.3.0");
			final String deleteAll = "*:*";
			UpdateResponse deleteAllQuery = solr.deleteByQuery(deleteAll);
			log.info(deleteAllQuery.toString());
			solr.commit(true, true);			
			solr.optimize();
	
			UpdateRequest updateRequest = new UpdateRequest();					
			for (Integer id : newsitemIdsToIndex) {
				Resource resource = resourceDAO.loadResourceById(id);
				log.info("Adding solr record: " + resource.getId() + " - " + resource.getName() + " - " + resource.getType());			
				SolrInputDocument inputDocument = solrInputDocumentBuilder.buildResouceInputDocument(resource);
				updateRequest.add(inputDocument);
			}
			
			updateRequest.process(solr);
			solr.commit();
			solr.optimize();			
			
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
	
}
