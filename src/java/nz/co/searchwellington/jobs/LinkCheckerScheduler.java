package nz.co.searchwellington.jobs;

import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import nz.co.searchwellington.model.LinkCheckerQueue;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.ResourceRepository;


public class LinkCheckerScheduler {
    
    Logger log = Logger.getLogger(LinkCheckerScheduler.class);
    
    private ResourceRepository resourceDAO;
    private LinkCheckerQueue linkCheckerQueue;


    
    public LinkCheckerScheduler() {        
    }
    
    
    
    public LinkCheckerScheduler(ResourceRepository resourceDAO, LinkCheckerQueue linkCheckerQueue) {
        super();
        this.resourceDAO = resourceDAO;
        this.linkCheckerQueue = linkCheckerQueue;
    }
    
    
    @Transactional()
    public void queueFeeds() {
        log.info("Queuing feeds for checking.");
        for (Resource resource : resourceDAO.getAllFeeds()) {
            log.info("Queuing feed item for checking: " + resource.getName());
            linkCheckerQueue.add(resource.getId());
        }
    }


    
    // Run every 24 hours.
    @Transactional()
    public void queueWatchlistItems() {    
        log.info("Queuing watchlist items for checking.");
        for (Resource resource : resourceDAO.getAllWatchlists(true)) {
            log.info("Queuing watchlist item for checking: " + resource.getName());
            linkCheckerQueue.add(resource.getId());
        }
        
       
    }
    
    // Add items older than a month.
    @Transactional()
    public void queueExpiredItems() {
        
        final int numberOfItemsToQueue = 10;
        log.info("Queuing " + numberOfItemsToQueue + " items not scanned for more than one month.");
        
        Date oneMonthAgo = Calendar.getInstance().getTime();
        for (Resource resource: resourceDAO.getNotCheckedSince(oneMonthAgo, numberOfItemsToQueue)) {
            log.info("Queuing for scheduled checking: " + resource.getName() + " - " + resource.getLastScanned());
            linkCheckerQueue.add(resource.getId());
        }                
    }
    
    
}
