package nz.co.searchwellington.jobs;

import java.util.List;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.solr.SolrQueryService;
import nz.co.searchwellington.repositories.solr.SolrUpdateQueue;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

public class SolrUpdateJob {
	
	static Logger log = Logger.getLogger(SolrUpdateJob.class);
		
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
    public void run() {
    	log.info("Running solr index update");    	
    	while (solrUpdateQueue.hasNext()) {
    		List<Resource> resources = solrUpdateQueue.getBatch();    	
    		if (!resources.isEmpty()) {
    			log.info("Passing " + resources.size() + " items to solr");
    			solrQueryService.updateIndexForResources(resources);
    		}
    	}    	    	
    	log.info("Finished solr index update");
    }
    
}
