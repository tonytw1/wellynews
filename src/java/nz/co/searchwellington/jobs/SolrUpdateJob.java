package nz.co.searchwellington.jobs;

import java.util.ArrayList;
import java.util.List;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.solr.SolrQueryService;
import nz.co.searchwellington.repositories.solr.SolrUpdateQueue;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

public class SolrUpdateJob {
	
	static Logger log = Logger.getLogger(SolrUpdateJob.class);
	
	private static final int MAX_BATCH_SIZE = 50;
	
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
    		List<Resource> resources = getNextBatch();    	
    		if (!resources.isEmpty()) {
    			log.info("Passing " + resources.size() + " items to solr");
    			solrQueryService.updateIndexForResources(resources);
    		}
    	}    	    	
    	log.info("Finished solr index update");
    }


	private List<Resource> getNextBatch() {
		List<Resource> resources = new ArrayList<Resource>();
		int counter = 0;
		while (solrUpdateQueue.hasNext() && counter < MAX_BATCH_SIZE) {
				Resource resource = solrUpdateQueue.getNext();
				if (resource != null) {
					log.info("Updating solr index for resource: " + resource.getName());
					resources.add(resource);
					counter++;
				} else {
					log.warn("Null resource returned from solr update queue.");    	
				}
		}
		return resources;
	}
    
}
