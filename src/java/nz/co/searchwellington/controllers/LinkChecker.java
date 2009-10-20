package nz.co.searchwellington.controllers;

import java.io.IOException;
import java.util.Date;

import nz.co.searchwellington.commentfeeds.guessers.CommentFeedGuesser;
import nz.co.searchwellington.feeds.RssfeedNewsitemService;
import nz.co.searchwellington.linkchecking.FeedAutodiscoveryProcesser;
import nz.co.searchwellington.linkchecking.FirstLiveTimeSetter;
import nz.co.searchwellington.linkchecking.LinkCheckerProcessor;
import nz.co.searchwellington.linkchecking.NewsitemCommentReader;
import nz.co.searchwellington.linkchecking.TechnoratiCountUpdater;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.SnapshotDAO;
import nz.co.searchwellington.utils.HttpFetchResult;
import nz.co.searchwellington.utils.HttpFetcher;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.transaction.annotation.Transactional;

public class LinkChecker {
    
    private static Logger log = Logger.getLogger(LinkChecker.class);
       
    private ResourceRepository resourceDAO;
    private RssfeedNewsitemService rssfeedNewsitemService;
	private SnapshotDAO snapshotDAO;
	private HttpFetcher httpFetcher;    
    private LinkCheckerProcessor[] processers;

	
	public LinkChecker() {
    }
	
	
	
	public LinkChecker(ResourceRepository resourceDAO, RssfeedNewsitemService rssfeedNewsitemService, SnapshotDAO snapshotDAO, HttpFetcher httpFetcher,
			LinkCheckerProcessor... processers) {
		this.resourceDAO = resourceDAO;
		this.rssfeedNewsitemService = rssfeedNewsitemService;
		this.snapshotDAO = snapshotDAO;
		this.httpFetcher = httpFetcher;
		this.processers = processers;
	}


	@Transactional
    public void scanResource(int checkResourceId) {
        Resource checkResource = resourceDAO.loadResourceById(checkResourceId);         
        if (checkResource != null) {
	        scanLoadedResource(checkResource);
        }        
    }


	private void scanLoadedResource(Resource checkResource) {
		log.info("Checking: " + checkResource.getName() + "(" + checkResource.getUrl() + ")");        
		log.debug("Before status: " + checkResource.getHttpStatus());      
						
		final String beforePageContent = snapshotDAO.loadContentForUrl(checkResource.getUrl());									     		
		DateTime currentTime = new DateTime();
		httpCheck(checkResource);
		
		final String pageContent = snapshotDAO.loadContentForUrl(checkResource.getUrl());
		checkForChangeUsingSnapshots(checkResource, beforePageContent, currentTime, pageContent);            
		if (checkResource.getType().equals("F")) {
			updateLatestFeedItem((Feed) checkResource);
		}
		
		for (LinkCheckerProcessor processor : processers) {
			processor.process(checkResource, pageContent);
		}
		
		checkResource.setLastScanned(currentTime.toDate());				
		log.debug("Saving resource.");
		resourceDAO.saveResource(checkResource);
	}



	
    private void updateLatestFeedItem(Feed checkResource) {            
        Date latestPublicationDate = rssfeedNewsitemService.getLatestPublicationDate(checkResource);     
        checkResource.setLatestItemDate(latestPublicationDate);
        log.debug("Latest item publication date for this feed was: " + checkResource.getLatestItemDate());      
    }

    
    private void checkForChangeUsingSnapshots(Resource checkResource, String before, DateTime currentTime, String after) {             
        log.info("Comparing content before and after snapshots from content change.");
        boolean contentChanged = contentChanged(before, after);                       
                   
        if (contentChanged) {
            log.info("Change in content checksum detected. Setting last changed.");
            checkResource.setLastChanged(currentTime.toDate());
            
        } else {
            log.info("No change in content detected.");
        }
    }
    
    
    
    protected boolean contentChanged(String before, String after) {
        boolean contentChanged = false;
        if (before != null && after != null) {
            contentChanged = !after.equals(before);
        } else {
            final boolean bothAreNull = (before == null) && (after == null);           
            if (bothAreNull) {
                contentChanged = false;
            } else {
                contentChanged = true;
            }
        }
        return contentChanged;
    }
   
    
    private void httpCheck(Resource checkResource) {
        String url = checkResource.getUrl();
        try {
            	String pageContent = null;
                HttpFetchResult httpResult = httpFetcher.httpFetch(checkResource.getUrl());
                if (httpResult.getStatus() == HttpStatus.SC_OK) {
                    pageContent = httpResult.readEncodedResponse("UTF-8");
                }
                checkResource.setHttpStatus(httpResult.getStatus());                
                snapshotDAO.setSnapshotContentForUrl(url, pageContent);
                return;
                
        } catch (IllegalArgumentException e) {
        	log.error("Error while checking url: ", e);        
        } catch (IOException e) {
        	log.error("Error while checking url: ", e);
        }
        checkResource.setHttpStatus(-1);            
    }
    
}
