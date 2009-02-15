package nz.co.searchwellington.model;

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
        if (queue.size() > 0) {
            int nextId = queue.poll();
            return nextId;
        } else {
            return null;
        }
    }

    public void add(int id) {
        log.debug("Adding id to queue: " + id);
        if (!queue.contains(id)) {
            queue.offer(id);
            log.debug("Queue contains " + queue.size() + " items.");
        } else {
           log.warn("Queue already contains id: " + id);
        }
    }
   
}