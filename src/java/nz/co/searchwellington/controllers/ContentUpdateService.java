package nz.co.searchwellington.controllers;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.mail.Notifier;
import nz.co.searchwellington.model.LinkCheckerQueue;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.SuggestionDAO;

public class ContentUpdateService {

	private ResourceRepository resourceDAO;
	private SuggestionDAO suggestionsDAO;
	private LinkCheckerQueue linkCheckerQueue;
	private Notifier notifier;


	
	public ContentUpdateService(ResourceRepository resourceDAO, SuggestionDAO suggestionsDAO, LinkCheckerQueue linkCheckerQueue, Notifier notifier) {		
		this.resourceDAO = resourceDAO;
		this.suggestionsDAO = suggestionsDAO;
		this.linkCheckerQueue = linkCheckerQueue;
		this.notifier = notifier;
	}

	
	public void update(Resource resource, User loggedInUser, HttpServletRequest request) {				
		boolean resourceUrlHasChanged = false;
		boolean newSubmission = resource.getId() == 0;
		if (!newSubmission) {
			Resource existingResource = resourceDAO.loadResourceById(resource.getId());
			resourceUrlHasChanged = !resource.getUrl().equals(existingResource.getUrl());			
		}

		boolean needsLinkCheck = resourceUrlHasChanged || newSubmission;
		update(resource, needsLinkCheck);
		
        if (newSubmission && loggedInUser != null) {        	
        	resource.setOwner(loggedInUser);
        }
		
		final boolean isNewPublicSubmission = loggedInUser == null && newSubmission;
		if (isNewPublicSubmission) {			
			 notifier.sendSubmissionNotification("New submission", resource);
		}
	}

	
	public void update(Resource resource, boolean needsLinkCheck) {
		resourceDAO.saveResource(resource);
		if (resource.getType().equals("N")) {
			suggestionsDAO.removeSuggestion(resource.getUrl());
		}
		if (needsLinkCheck) {
			resource.setHttpStatus(0);
			linkCheckerQueue.add(resource);
		}		
	}
	
}
