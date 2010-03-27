package nz.co.searchwellington.repositories.solr;

import java.util.concurrent.ConcurrentLinkedQueue;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.ResourceRepository;

import org.apache.log4j.Logger;

public class SolrUpdateQueue {
	
    static Logger log = Logger.getLogger(SolrUpdateQueue.class);

    private ResourceRepository resourceDAO;
    private ConcurrentLinkedQueue<Integer> queue;

    
	public SolrUpdateQueue(ResourceRepository resourceDAO) {
		this.resourceDAO = resourceDAO;
        queue = new ConcurrentLinkedQueue<Integer>();
	}


	public void add(Resource resource) {
		log.info("Adding resouce to solr update queue: " + resource.getId());
		if (!queue.contains(resource.getId())) {
			queue.offer(resource.getId());
			log.debug("Queue contains " + queue.size() + " items.");
		}
	}
	
	
	public boolean hasNext() {
		return !queue.isEmpty();
	}
	
	
	public Resource getNext() {
		 log.debug("Getting next from queue currently contains " + queue.size() + " items.");
		 if (queue.size() > 0) {
			 int nextId = queue.poll();
			 return resourceDAO.loadResourceById(nextId);
			 
		 } else {
			 return null;
		 }
	}
	
}
