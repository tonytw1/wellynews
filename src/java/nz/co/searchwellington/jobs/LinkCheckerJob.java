package nz.co.searchwellington.jobs;

import nz.co.searchwellington.controllers.LinkChecker;
import nz.co.searchwellington.model.LinkCheckerQueue;

import org.apache.log4j.Logger;

public class LinkCheckerJob  {
    
    
    private LinkCheckerQueue queue;

    Logger log = Logger.getLogger(LinkCheckerJob.class);
    private LinkChecker linkChecker;
    
    
    public LinkCheckerJob() {
    }
    
    public LinkCheckerJob(LinkCheckerQueue queue, LinkChecker linkChecker) {
        this.queue = queue;
        this.linkChecker = linkChecker;
        
    }
  
    public void run() {

        log.debug("Queue contains " + queue.getSize() + " items.");
        while (queue.getSize() > 0) {
            int resourceId = queue.getNext();
            log.info("Checking resource: " + resourceId);
            linkChecker.scanResource(resourceId);
        }
    }
    
}
