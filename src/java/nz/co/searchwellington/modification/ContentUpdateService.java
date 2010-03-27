
package nz.co.searchwellington.modification;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.mail.Notifier;
import nz.co.searchwellington.model.LinkCheckerQueue;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.SuggestionRepository;
import nz.co.searchwellington.repositories.solr.SolrQueryService;
import nz.co.searchwellington.repositories.solr.SolrUpdateQueue;

import org.springframework.transaction.annotation.Transactional;

public class ContentUpdateService {

	private ResourceRepository resourceDAO;
	private SuggestionRepository suggestionsDAO;
	private LinkCheckerQueue linkCheckerQueue;
	private Notifier notifier;
	private SolrUpdateQueue solrUpdateQueue;

		
	public ContentUpdateService(ResourceRepository resourceDAO,
			SuggestionRepository suggestionsDAO,
			LinkCheckerQueue linkCheckerQueue, Notifier notifier,
			SolrUpdateQueue solrUpdateQueue) {
		super();
		this.resourceDAO = resourceDAO;
		this.suggestionsDAO = suggestionsDAO;
		this.linkCheckerQueue = linkCheckerQueue;
		this.notifier = notifier;
		this.solrUpdateQueue = solrUpdateQueue;
	}


	public ContentUpdateService() {
	}

	
	@Transactional
	public void update(Resource resource) {				
		boolean resourceUrlHasChanged = false;
		boolean newSubmission = resource.getId() == 0;
		if (!newSubmission) {
			Resource existingResource = resourceDAO.loadResourceById(resource.getId());
			resourceUrlHasChanged = !resource.getUrl().equals(existingResource.getUrl());			
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
			resource.setHttpStatus(0);
			linkCheckerQueue.add(resource);
		}
		solrUpdateQueue.add(resource);
	}
	
	
	public void save(Resource resource) {
		resourceDAO.saveResource(resource);
		if (resource.getType().equals("N")) {
			suggestionsDAO.removeSuggestion(resource.getUrl());
		}
	}
	
}
