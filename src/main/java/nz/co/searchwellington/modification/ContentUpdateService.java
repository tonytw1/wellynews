package nz.co.searchwellington.modification;

import nz.co.searchwellington.model.LinkCheckerQueue;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.FrontendContentUpdater;
import nz.co.searchwellington.repositories.HibernateResourceDAO;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ContentUpdateService {
	
	private static Logger log = Logger.getLogger(ContentUpdateService.class);
	
	private HibernateResourceDAO resourceDAO;
	private LinkCheckerQueue linkCheckerQueue;
	private FrontendContentUpdater frontendContentUpdater;
	
	public ContentUpdateService() {
	}
	
	@Autowired
	public ContentUpdateService(HibernateResourceDAO resourceDAO,
			LinkCheckerQueue linkCheckerQueue,
			FrontendContentUpdater frontendContentUpdater) {
		this.resourceDAO = resourceDAO;
		this.linkCheckerQueue = linkCheckerQueue;
		this.frontendContentUpdater = frontendContentUpdater;
	}

	@Transactional
	public void update(Resource resource) {
		log.info("Updating content for: " + resource.getName());
		try {
			boolean resourceUrlHasChanged = false;
			boolean newSubmission = resource.getId() == 0;
			if (!newSubmission) {
				Resource existingResource = resourceDAO.loadResourceById(resource.getId());
				resourceUrlHasChanged = !resource.getUrl().equals(existingResource.getUrl());			
			}
			
			if (newSubmission || resourceUrlHasChanged) {
				resource.setHttpStatus(0);
			}
			
			final boolean needsLinkCheck = resourceUrlHasChanged || newSubmission;
	
			save(resource);
			
			if (needsLinkCheck) {
				linkCheckerQueue.add(resource);
			}
			
		} catch (Exception e) {
			log.error("Error: ", e);
		}
	}
	
	@Transactional
	public void create(Resource resource) {
		resource.setHttpStatus(0);
		save(resource);		
		linkCheckerQueue.add(resource);	
	}
	
	private void save(Resource resource) {
		resourceDAO.saveResource(resource);
		frontendContentUpdater.update(resource);
	}
	
}
