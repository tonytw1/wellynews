package nz.co.searchwellington.repositories;

import org.apache.log4j.Logger;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.solr.SolrUpdateQueue;

public class FrontendContentUpdater {
	
	private static Logger log = Logger.getLogger(FrontendContentUpdater.class);
	
	private SolrUpdateQueue solrUpdateQueue;
	private SolrInputDocumentBuilder solrInputDocumentBuilder;
	
	public FrontendContentUpdater(SolrUpdateQueue solrUpdateQueue, SolrInputDocumentBuilder solrInputDocumentBuilder) {
		this.solrUpdateQueue = solrUpdateQueue;
		this.solrInputDocumentBuilder = solrInputDocumentBuilder;
	}
	
	public void update(Resource updatedResource) {
		log.info("Adding resource to frontend update queue: " + updatedResource.getName());
		solrUpdateQueue.add(solrInputDocumentBuilder.buildResouceInputDocument(updatedResource));		
	}

}
