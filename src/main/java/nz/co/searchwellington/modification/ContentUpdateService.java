package nz.co.searchwellington.modification;

import nz.co.searchwellington.model.LinkCheckerQueue;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.FrontendContentUpdater;
import nz.co.searchwellington.repositories.HibernateResourceDAO;
import nz.co.searchwellington.repositories.SuggestionDAO;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ContentUpdateService {
	
	private static Logger log = Logger.getLogger(ContentUpdateService.class);
	
	private HibernateResourceDAO resourceDAO;
	private SuggestionDAO suggestionsDAO;
	private LinkCheckerQueue linkCheckerQueue;
	private FrontendContentUpdater frontendContentUpdater;
	
	public ContentUpdateService() {
	}
	
	@Autowired
	public ContentUpdateService(HibernateResourceDAO resourceDAO,
			SuggestionDAO suggestionsDAO,
			LinkCheckerQueue linkCheckerQueue,
			FrontendContentUpdater frontendContentUpdater) {
		this.resourceDAO = resourceDAO;
		this.suggestionsDAO = suggestionsDAO;
		this.linkCheckerQueue = linkCheckerQueue;
		this.frontendContentUpdater = frontendContentUpdater;
	}

	@Transactional
	public void update(Resource resource) {
		log.info("Updating content for: " + resource.getName());
		boolean resourceUrlHasChanged = false;
		boolean newSubmission = resource.getId() == 0;
		if (!newSubmission) {
			Resource existingResource = resourceDAO.loadResourceById(resource.getId());
			resourceUrlHasChanged = !resource.getUrl().equals(existingResource.getUrl());			
		}
		
		if (newSubmission || resourceUrlHasChanged) {
			resource.setHttpStatus(0);
		}
		
		boolean needsLinkCheck = resourceUrlHasChanged || newSubmission;
		
		//if (newSubmission) {        // TODO is wrong place	
		//	resource.setOwner(loggedInUser);
		//}
		
		save(resource);
		// TODO is in wrong place
		//if ((newSubmission && (loggedInUser == null || !loggedInUser.isAdmin()))) {
		//	 notifier.sendSubmissionNotification("New submission", resource);
		//}
		if (needsLinkCheck) {
			linkCheckerQueue.add(resource);
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
		if (resource.getType().equals("N")) {
			suggestionsDAO.removeSuggestion(resource.getUrl());
		}
	}
	
}
