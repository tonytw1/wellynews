package nz.co.searchwellington.jobs;

import java.util.Date;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.queues.LinkCheckerQueue;
import nz.co.searchwellington.repositories.HibernateResourceDAO;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LinkCheckerScheduler {
    
	private final static Logger log = Logger.getLogger(LinkCheckerScheduler.class);
    
    private HibernateResourceDAO resourceDAO;
    private LinkCheckerQueue linkCheckerQueue;
    
    public LinkCheckerScheduler() {        
    }
    
    @Autowired
    public LinkCheckerScheduler(HibernateResourceDAO resourceDAO, LinkCheckerQueue linkCheckerQueue) {
        this.resourceDAO = resourceDAO;
        this.linkCheckerQueue = linkCheckerQueue;
    }
    
    @Scheduled(fixedRate=86400000)
    @Transactional()
    public void queueWatchlistItems() {    
        log.info("Queuing watchlist items for checking.");
        for (Resource resource : resourceDAO.getAllWatchlists()) {
            log.info("Queuing watchlist item for checking: " + resource.getName());
            linkCheckerQueue.add(resource.getId());
        }       
    }
    
    @Scheduled(fixedRate=3600000)
    @Transactional()
    public void queueExpiredItems() {  
        final int numberOfItemsToQueue = 10;	// TODO queue them all
        
        log.info("Queuing items launched within the last 24 hours with but not scanned within the last 4 hours");
        Date oneDayAgo = new DateTime().minusDays(1).toDate();
        Date fourHoursAgo = new DateTime().minusHours(4).toDate();
        
        for (Resource resource: resourceDAO.getNotCheckedSince(oneDayAgo, fourHoursAgo, numberOfItemsToQueue)) {
        	if (resource.getType().equals("N")) {
        		log.info("Queying recent newsitem for checking: " + resource.getName() + " - " + resource.getLastScanned());
        		linkCheckerQueue.add(resource.getId());
        	}
        }
        
        log.info("Queuing " + numberOfItemsToQueue + " items not scanned for more than one month.");        
        Date oneMonthAgo = new DateTime().minusMonths(1).toDate();
        for (Resource resource: resourceDAO.getNotCheckedSince(oneMonthAgo, numberOfItemsToQueue)) {
            log.info("Queuing for scheduled checking: " + resource.getName() + " - " + resource.getLastScanned());	// TODO time since on this log entire.
            linkCheckerQueue.add(resource.getId());
        }                
    }
    
}
