package nz.co.searchwellington.repositories.solr;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.ResourceRepository;

import org.apache.log4j.Logger;

public class SolrUpdateQueue {
	
	private static Logger log = Logger.getLogger(SolrUpdateQueue.class);
	
    private static final int MAX_BATCH_SIZE = 50;

    private ResourceRepository resourceDAO;
    
    private ConcurrentLinkedQueue<Integer> queue;
    
	public SolrUpdateQueue(ResourceRepository resourceDAO) {
		this.resourceDAO = resourceDAO;
        queue = new ConcurrentLinkedQueue<Integer>();
	}
	
	public void add(Resource resource) {
		log.info("Adding resource to solr update queue: " + resource.getName() + "(#" + resource.getId() + ")");
		if (!queue.contains(resource.getId())) {
 			queue.offer(resource.getId());
			log.debug("Queue contains " + queue.size() + " items.");
		}
	}

	public boolean hasNext() {
		return !queue.isEmpty();
	}
	
	public synchronized List<Resource> getBatch() {
		log.debug("Getting next from queue currently contains " + queue.size() + " items.");		
		List<Resource> batch = new ArrayList<Resource>();		
		if (!queue.isEmpty()) {
			Object[] items = queue.toArray();
			for (int i = 0; i < items.length; i++) {
				if (i < MAX_BATCH_SIZE) {
					int nextId = (Integer) items[i];
					queue.remove(nextId);
					log.debug("Next resource is: " + nextId);
				 
					Resource resource = resourceDAO.loadResourceById(nextId);
					if (resource != null) {
						batch.add(resource);
					} else {
						log.warn("Solr queued resource id was null after database load: " + nextId);
					}
				}
			}
		}		
		return batch;		
	}
	
}
