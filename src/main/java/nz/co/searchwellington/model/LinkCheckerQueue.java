package nz.co.searchwellington.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.log4j.Logger;


public class LinkCheckerQueue {
    
    Logger log = Logger.getLogger(LinkCheckerQueue.class);
    
    private ConcurrentLinkedQueue<Integer> queue;
       
    public LinkCheckerQueue() {
        queue = new ConcurrentLinkedQueue<Integer>();
    }

    public int getSize() {
        return queue.size();
    }

    public Integer getNext() {
    	log.debug("Getting next from queue currently contains " + queue.size() + " items.");
        if (queue.size() > 0) {
            int nextId = queue.poll();
            return nextId;
        } else {
            return null;
        }
    }
    
    public List<Integer> getContents() {
    	log.debug("Getting queue contents; currently contains " + queue.size() + " items.");
    	List<Integer> contents = new ArrayList<Integer>();
    	Iterator<Integer> contentIds = queue.iterator();
    	while(contentIds.hasNext()) {
    		contents.add(contentIds.next());
    	}
    	return contents;
    }

    public void add(Resource resource) {
    	this.add(resource.getId());		

    }

    
    private void add(int id) {
        log.debug("Adding id to queue: " + id);
        if (!queue.contains(id)) {
            queue.offer(id);
            log.debug("Queue contains " + queue.size() + " items.");
        } else {
           log.warn("Queue already contains id: " + id);
        }
    }

    
}