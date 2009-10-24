package nz.co.searchwellington.controllers;

import java.io.IOException;

import nz.co.searchwellington.linkchecking.LinkCheckerProcessor;
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
	private SnapshotDAO snapshotDAO;
	private HttpFetcher httpFetcher;    
    private LinkCheckerProcessor[] processers;

	
	public LinkChecker() {
    }
	
	
	public LinkChecker(ResourceRepository resourceDAO, SnapshotDAO snapshotDAO, HttpFetcher httpFetcher, LinkCheckerProcessor... processers) {
		this.resourceDAO = resourceDAO;
		this.snapshotDAO = snapshotDAO;
		this.httpFetcher = httpFetcher;
		this.processers = processers;
	}


	@Transactional
    public void scanResource(int checkResourceId) {
        Resource checkResource = resourceDAO.loadResourceById(checkResourceId);         
        if (checkResource != null) {
	        log.info("Checking: " + checkResource.getName() + "(" + checkResource.getUrl() + ")");        
			log.debug("Before status: " + checkResource.getHttpStatus());      
			
			final String pageContentAfterHttpCheck = httpCheck(checkResource);
			final String pageContent = snapshotDAO.loadContentForUrl(checkResource.getUrl());

			log.info("Running linkchecking processors");
			for (LinkCheckerProcessor processor : processers) {
				log.info("Running processor: " + processor.getClass().toString());
				processor.process(checkResource, pageContent);
			}
			log.info("Finished linkchecking");
			
			log.debug("Saving resource.");
			checkResource.setLastScanned(new DateTime().toDate());
			snapshotDAO.setSnapshotContentForUrl(checkResource.getUrl(), pageContentAfterHttpCheck);
			resourceDAO.saveResource(checkResource);
        }     
    }

	
    private String httpCheck(Resource checkResource) {
        try {
        	String pageContent = null;
        	HttpFetchResult httpResult = httpFetcher.httpFetch(checkResource.getUrl());
        	checkResource.setHttpStatus(httpResult.getStatus());
                
        	if (httpResult.getStatus() == HttpStatus.SC_OK) {
        		pageContent = httpResult.readEncodedResponse("UTF-8");
        		return pageContent;
        	}
        	
        } catch (IllegalArgumentException e) {
        	log.error("Error while checking url: ", e);        
        } catch (IOException e) {
        	log.error("Error while checking url: ", e);
        }
        checkResource.setHttpStatus(-1);
        return null;
    }
    
    
}
