package nz.co.searchwellington.repositories;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.solr.SolrUpdateQueue;

@Component
public class FrontendContentUpdater {
	
	private static Logger log = Logger.getLogger(FrontendContentUpdater.class);
	
	private SolrUpdateQueue solrUpdateQueue;
	private SolrInputDocumentBuilder solrInputDocumentBuilder;
	
	public FrontendContentUpdater() {
	}
	
	@Autowired
	public FrontendContentUpdater(SolrUpdateQueue solrUpdateQueue, SolrInputDocumentBuilder solrInputDocumentBuilder) {
		this.solrUpdateQueue = solrUpdateQueue;
		this.solrInputDocumentBuilder = solrInputDocumentBuilder;
	}
	
	public void update(Resource updatedResource) {
		log.info("Adding resource to frontend update queue: " + updatedResource.getName());
		solrUpdateQueue.add(solrInputDocumentBuilder.buildResouceInputDocument(updatedResource));		
	}

}
