package nz.co.searchwellington.model;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;

@Component
public class LinkCheckerQueue {
    
	private static Logger log = Logger.getLogger(LinkCheckerQueue.class);
    
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
    	return ImmutableList.<Integer>builder().addAll(queue.iterator()).build();    	
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