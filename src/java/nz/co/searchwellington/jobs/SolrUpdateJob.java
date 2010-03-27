package nz.co.searchwellington.jobs;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.solr.SolrQueryService;
import nz.co.searchwellington.repositories.solr.SolrUpdateQueue;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

public class SolrUpdateJob {
	
	private static Logger log = Logger.getLogger(SolrUpdateJob.class);
	
	private SolrUpdateQueue solrUpdateQueue;
	private SolrQueryService solrQueryService;
	
		
	public SolrUpdateJob() {
	}


	public SolrUpdateJob(SolrUpdateQueue solrUpdateQueue,
			SolrQueryService solrQueryService) {
		this.solrUpdateQueue = solrUpdateQueue;
		this.solrQueryService = solrQueryService;
	}
	
	
	@Transactional
    public void run() {    	// TODO wants commit grouping
    	log.info("Running solr index update");
    	while (solrUpdateQueue.hasNext()) {
    		Resource resource = solrUpdateQueue.getNext();
    		if (resource != null) {
    			log.info("Updating solr index for resource: " + resource.getName());
    			solrQueryService.updateIndexForResource(resource);
    		} else {
    			log.warn("Null resource returned from solr update queue.");    	
    		}
    	}
    	log.info("Finished solr index update");
    }
    
}
