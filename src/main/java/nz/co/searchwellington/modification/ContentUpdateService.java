package nz.co.searchwellington.modification;

import nz.co.searchwellington.model.LinkCheckerQueue;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.FrontendContentUpdater;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.SuggestionRepository;

import org.springframework.transaction.annotation.Transactional;

public class ContentUpdateService {

	private ResourceRepository resourceDAO;
	private SuggestionRepository suggestionsDAO;
	private LinkCheckerQueue linkCheckerQueue;
	private FrontendContentUpdater frontendContentUpdater;
	
	public ContentUpdateService() {
	}
	
	public ContentUpdateService(ResourceRepository resourceDAO,
			SuggestionRepository suggestionsDAO,
			LinkCheckerQueue linkCheckerQueue,
			FrontendContentUpdater frontendContentUpdater) {
		this.resourceDAO = resourceDAO;
		this.suggestionsDAO = suggestionsDAO;
		this.linkCheckerQueue = linkCheckerQueue;
		this.frontendContentUpdater = frontendContentUpdater;
	}

	@Transactional
	public void update(Resource resource) {				
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
