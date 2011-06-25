package nz.co.searchwellington.repositories.solr;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;

public class SolrUpdateQueue {
	
	private static Logger log = Logger.getLogger(SolrUpdateQueue.class);
	
    private static final int MAX_BATCH_SIZE = 50;
    
    private ConcurrentLinkedQueue<SolrInputDocument> queue;
    
	public SolrUpdateQueue() {
        queue = new ConcurrentLinkedQueue<SolrInputDocument>();
	}
	
	public void add(SolrInputDocument resource) {
		log.debug("Adding resource to solr update queue");
		queue.offer(resource);
		log.debug("Queue contains " + queue.size() + " items.");		
	}

	public boolean hasNext() {
		return !queue.isEmpty();
	}
	
	public synchronized List<SolrInputDocument> getBatch() {
		log.debug("Getting next from queue currently contains " + queue.size() + " items.");		
		List<SolrInputDocument> batch = new ArrayList<SolrInputDocument>();		
		if (!queue.isEmpty()) {
			Object[] items = queue.toArray();
			for (int i = 0; i < items.length; i++) {
				if (i < MAX_BATCH_SIZE) {
					SolrInputDocument next = (SolrInputDocument) items[i];
					queue.remove(next);
					batch.add(next);				
				}
			}
		}		
		return batch;
	}
	
}
