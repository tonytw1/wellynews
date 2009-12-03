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


	
	public void update(Resource resource, User loggedInUser, HttpServletRequest request, boolean newSubmission, boolean resourceUrlHasChanged) {
		resourceDAO.saveResource(resource);
		if (resource.getType().equals("N")) {
			suggestionsDAO.removeSuggestion(resource.getUrl());
		}
		
		if (resourceUrlHasChanged || newSubmission) {
			resource.setHttpStatus(0);
			linkCheckerQueue.add(resource.getId());
		}
		
		final boolean isNewPublicSubmission = loggedInUser == null && newSubmission;
		if (isNewPublicSubmission) {
			 request.getSession().setAttribute("owned", new Integer(resource.getId()));
			 notifier.sendSubmissionNotification("New submission", resource);
		}		
	}
	
}
