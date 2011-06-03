package nz.co.searchwellington.linkchecking;

import java.io.IOException;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.modification.ContentUpdateService;
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
	
    private static final int CANT_CONNECT = -1;
    
    private ResourceRepository resourceDAO;
	private SnapshotDAO snapshotDAO;
	private ContentUpdateService contentUpdateService;
	private HttpFetcher httpFetcher;
    private LinkCheckerProcessor[] processers;
    
	public LinkChecker() {
    }
	
	public LinkChecker(ResourceRepository resourceDAO, SnapshotDAO snapshotDAO, ContentUpdateService contentUpdateService, HttpFetcher httpFetcher, LinkCheckerProcessor... processers) {
		this.resourceDAO = resourceDAO;
		this.snapshotDAO = snapshotDAO;
		this.contentUpdateService = contentUpdateService;
		this.httpFetcher = httpFetcher;
		this.processers = processers;
	}
	
	@Transactional
    public void scanResource(int checkResourceId) {
        Resource resource = resourceDAO.loadResourceById(checkResourceId);         
        if (resource != null) {
	        log.info("Checking: " + resource.getName() + " (" + resource.getUrl() + ")");        
			log.debug("Before status: " + resource.getHttpStatus());      
			
			final String pageContent = httpCheck(resource);

			log.info("Running linkchecking processors");
			for (LinkCheckerProcessor processor : processers) {
				log.info("Running processor: " + processor.getClass().toString());
				processor.process(resource, pageContent);
			}
			log.info("Finished linkchecking");
						
			log.debug("Saving resource and updating snapshot");
			resource.setLastScanned(new DateTime().toDate());
			snapshotDAO.setSnapshotContentForUrl(resource.getUrl(), pageContent);
			contentUpdateService.update(resource);
			
        } else {
        	log.warn("Could not check resource with id #" + checkResourceId + " as it was not found in the database");
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
        	
        	checkResource.setHttpStatus(httpResult.getStatus());
        	return null;
        	        	
        } catch (IllegalArgumentException e) {
        	log.error("Error while checking url: ", e);        
        } catch (IOException e) {
        	log.error("Error while checking url: ", e);
        }
        checkResource.setHttpStatus(CANT_CONNECT);
        return null;
    }
        
}
