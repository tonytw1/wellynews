package nz.co.searchwellington.linkchecking;

import java.io.IOException;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.modification.ContentUpdateService;
import nz.co.searchwellington.repositories.HibernateResourceDAO;
import nz.co.searchwellington.utils.HttpFetchResult;
import nz.co.searchwellington.utils.HttpFetcher;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Strings;

import uk.co.eelpieconsulting.archiving.FilesystemSnapshotArchive;
import uk.co.eelpieconsulting.archiving.Snapshot;
import uk.co.eelpieconsulting.archiving.SnapshotArchive;

@Component
public class LinkChecker {
    
	private static Logger log = Logger.getLogger(LinkChecker.class);
	
    private static final int CANT_CONNECT = -1;
    
    private HibernateResourceDAO resourceDAO;
	private SnapshotArchive snapshotArchive;
	private ContentUpdateService contentUpdateService;
	private HttpFetcher httpFetcher;
    private LinkCheckerProcessor[] processers;
    
	public LinkChecker() {
    }
	
	@Autowired
	public LinkChecker(HibernateResourceDAO resourceDAO, ContentUpdateService contentUpdateService, HttpFetcher httpFetcher, LinkCheckerProcessor... processers) {
		this.resourceDAO = resourceDAO;
		this.snapshotArchive = new FilesystemSnapshotArchive("/home/tony/snapshots");
		this.contentUpdateService = contentUpdateService;
		this.httpFetcher = httpFetcher;
		this.processers = processers;
	}
	
	@Transactional
    public void scanResource(int checkResourceId) {
        final Resource resource = resourceDAO.loadResourceById(checkResourceId);         
        if (resource != null && !Strings.isNullOrEmpty(resource.getUrl())) {
	        log.info("Checking: " + resource.getName() + " (" + resource.getUrl() + ")");        
			
			final String pageContent = httpCheck(resource);

			for (LinkCheckerProcessor processor : processers) {
				log.debug("Running processor: " + processor.getClass().toString());
				try {
					processor.process(resource, pageContent);	// TODO should any of these run if the page content is null?
					
				} catch (Exception e) {
					log.error("An exception occured while running a link checker processor", e);
				}
			}
						
			log.debug("Saving resource and updating snapshot");
			resource.setLastScanned(DateTime.now().toDate());
			
			if (pageContent != null) {
				snapshotArchive.put(new Snapshot(resource.getUrl(), DateTime.now().toDate(), pageContent));
			}
			
			contentUpdateService.update(resource);
			log.info("Finished linkchecking");
			
        } else {
        	log.warn("Could not check resource with id #" + checkResourceId + " as it was not found in the database");
        }
    }
	
    private String httpCheck(Resource checkResource) {
        try {
        	String pageContent = null;
        	HttpFetchResult httpResult = httpFetcher.httpFetch(checkResource.getUrl());
        	checkResource.setHttpStatus(httpResult.getStatus());
        	log.info("Http status for " + checkResource.getUrl() + " set to: " + checkResource.getHttpStatus());
        	
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
