package nz.co.searchwellington.repositories;

import java.util.List;

import nz.co.searchwellington.controllers.ShowBrokenDecisionService;
import nz.co.searchwellington.model.Resource;

public class ContentRetrievalService {

	private ResourceRepository resourceDAO;
	private ShowBrokenDecisionService showBrokenDecisionService;

	public ContentRetrievalService(ResourceRepository resourceDAO, ShowBrokenDecisionService showBrokenDecisionService) {
		this.resourceDAO = resourceDAO;
		this.showBrokenDecisionService = showBrokenDecisionService;
	}

	public List<Resource> getAllWatchlists() {
		return resourceDAO.getAllWatchlists(showBrokenDecisionService.shouldShowBroken());
	}
		
}
