package nz.co.searchwellington.controllers.admin;

import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.User;

import org.apache.log4j.Logger;

public class EditPermissionService {

	static Logger log = Logger.getLogger(EditPermissionService.class);
	   
	
	private LoggedInUserFilter loggedInUserFilter;
	

	public EditPermissionService(LoggedInUserFilter loggedInUserFilter) {	
		this.loggedInUserFilter = loggedInUserFilter;
	}

	
	public boolean canEdit(FrontendResource resource) {
		User loggedInUser = loggedInUserFilter.getLoggedInUser();
		return isAdminOrOwner(resource, loggedInUser);
	}
	
	
	public boolean canDelete(FrontendResource resource) {
		User loggedInUser = loggedInUserFilter.getLoggedInUser();
		return isAdminOrOwner(resource, loggedInUser);
	}
	
	public boolean canDecache(FrontendResource resource) {
		User loggedInUser = loggedInUserFilter.getLoggedInUser();
		return loggedInUser != null && loggedInUser.isAdmin();
	}
	
	public boolean canRead(FrontendResource resource) {
		User loggedInUser = loggedInUserFilter.getLoggedInUser();
		return loggedInUser != null && loggedInUser.isAdmin();
	}

	
	public boolean canCheck(FrontendResource resource) {
		User loggedInUser = loggedInUserFilter.getLoggedInUser();
		return loggedInUser != null && loggedInUser.isAdmin();
	}
	
	
	public boolean canSeeLocalPage(Newsitem newsitem) {
		User loggedInUser = loggedInUserFilter.getLoggedInUser();
		return loggedInUser != null && loggedInUser.isAdmin();
	}
	
	
	public boolean canEditSuggestions() {
		User loggedInUser = loggedInUserFilter.getLoggedInUser();
		return loggedInUser != null && loggedInUser.isAdmin();
	}

	public boolean canAddTag() {
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
	
	
	private boolean isAdminOrOwner(FrontendResource resource, User loggedInUser) {
		if (loggedInUser == null) {
			return false;
		}
		
		if (loggedInUser.isAdmin()) {
			return true;
		}
			
		// TODO reimplement for frontend resource.s
		//if (resource.getOwner() != null && loggedInUser.getId() == resource.getOwner().getId()) {
		//	return true;
		//}
		return false;
	}

	public boolean canAcceptFeedItems(User loggedInUser) {
		return loggedInUser != null && loggedInUser.isAdmin();
	}

	public boolean canDeleteTags(User loggedInUser) {
		return loggedInUser != null && loggedInUser.isAdmin();
	}
		
}
