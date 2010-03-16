package nz.co.searchwellington.controllers.admin;

import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.filters.RequestFilter;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.User;

import org.apache.log4j.Logger;

public class EditPermissionService {

	Logger log = Logger.getLogger(EditPermissionService.class);
	   
	
	private LoggedInUserFilter loggedInUserFilter;
	private RequestFilter requestFilter;
	

	public EditPermissionService(LoggedInUserFilter loggedInUserFilter, RequestFilter requestFilter) {	
		this.loggedInUserFilter = loggedInUserFilter;
		this.requestFilter = requestFilter;
	}

	
	public boolean canEdit(Resource resource) {
		User loggedInUser = loggedInUserFilter.getLoggedInUser();
		boolean isOnRequest = requestFilter.getAnonResource() != null && requestFilter.getAnonResource().getId() == resource.getId();
		return isAdminOrOwner(resource, loggedInUser) || isOnRequest;
	}
	
	
	public boolean canDelete(Resource resource) {
		User loggedInUser = loggedInUserFilter.getLoggedInUser();
		return isAdminOrOwner(resource, loggedInUser);
	}
	
	public boolean canDecache(Resource resource) {
		User loggedInUser = loggedInUserFilter.getLoggedInUser();
		return loggedInUser != null && loggedInUser.isAdmin();
	}
	
	public boolean canRead(Resource resource) {
		User loggedInUser = loggedInUserFilter.getLoggedInUser();
		return loggedInUser != null && loggedInUser.isAdmin();
	}

	
	public boolean canCheck(Resource resource) {
		User loggedInUser = loggedInUserFilter.getLoggedInUser();
		return loggedInUser != null && loggedInUser.isAdmin();
	}
	
	
	public boolean canEditSuggestions() {
		User loggedInUser = loggedInUserFilter.getLoggedInUser();
		return loggedInUser != null && loggedInUser.isAdmin();
	}


	
	public boolean canEdit(Tag tag) {
		User loggedInUser = loggedInUserFilter.getLoggedInUser();		
		return loggedInUser != null && loggedInUser.isAdmin();
	}
	
	
	public boolean canAddWatchlistAndTag() {
		User loggedInUser = loggedInUserFilter.getLoggedInUser();		
		return loggedInUser != null && loggedInUser.isAdmin();
	}
	
	
	private boolean isAdminOrOwner(Resource resource, User loggedInUser) {
		if (loggedInUser == null) {
			return false;
		}
		
		if (loggedInUser.isAdmin()) {
			return true;
		}
				
		if (resource.getOwner() != null && loggedInUser.getId() == resource.getOwner().getId()) {
			return true;
		}
		return false;
	}

	public boolean canAcceptFeedItems(User loggedInUser) {
		return loggedInUser.isAdmin();
	}

	public boolean canDeleteTags(User loggedInUser) {
		return loggedInUser.isAdmin();
	}
		
}
