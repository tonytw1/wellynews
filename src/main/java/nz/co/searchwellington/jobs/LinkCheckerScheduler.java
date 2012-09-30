package nz.co.searchwellington.jobs;

import java.util.Date;

import nz.co.searchwellington.model.LinkCheckerQueue;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.HibernateResourceDAO;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.transaction.annotation.Transactional;


public class LinkCheckerScheduler {
    
    Logger log = Logger.getLogger(LinkCheckerScheduler.class);
    
    private HibernateResourceDAO resourceDAO;
    private LinkCheckerQueue linkCheckerQueue;


    
    public LinkCheckerScheduler() {        
    }
    
    
    
    public LinkCheckerScheduler(HibernateResourceDAO resourceDAO, LinkCheckerQueue linkCheckerQueue) {
        super();
        this.resourceDAO = resourceDAO;
        this.linkCheckerQueue = linkCheckerQueue;
    }
    
    
    @Transactional()
    public void queueFeeds() {
        log.info("Queuing feeds for checking.");
        for (Resource resource : resourceDAO.getAllFeeds()) {
            log.info("Queuing feed item for checking: " + resource.getName());
            linkCheckerQueue.add(resource);
        }
    }


    
    // Run every 24 hours.
    @Transactional()
    public void queueWatchlistItems() {    
        log.info("Queuing watchlist items for checking.");
        for (Resource resource : resourceDAO.getAllWatchlists()) {
            log.info("Queuing watchlist item for checking: " + resource.getName());
            linkCheckerQueue.add(resource);
        }       
    }

    
    @Transactional()
    public void queueExpiredItems() {  
        final int numberOfItemsToQueue = 10;

        log.info("Queuing items launched within the last 24 hours with but not scanned within the last 4 hours");
        Date oneDayAgo = new DateTime().minusDays(1).toDate();
        Date fourHoursAgo = new DateTime().minusHours(4).toDate();
        
        for (Resource resource: resourceDAO.getNotCheckedSince(oneDayAgo, fourHoursAgo, numberOfItemsToQueue)) {
        	if (resource.getType().equals("N")) {
        		log.info("Queying recent newsitem for checking: " + resource.getName() + " - " + resource.getLastScanned());
        		linkCheckerQueue.add(resource);
        	}
        }
        
        log.info("Queuing " + numberOfItemsToQueue + " items not scanned for more than one month.");        
        Date oneMonthAgo = new DateTime().minusMonths(1).toDate();
        for (Resource resource: resourceDAO.getNotCheckedSince(oneMonthAgo, numberOfItemsToQueue)) {
            log.info("Queuing for scheduled checking: " + resource.getName() + " - " + resource.getLastScanned());	// TODO time since on this log entire.
            linkCheckerQueue.add(resource);
        }                
    }
    
}
