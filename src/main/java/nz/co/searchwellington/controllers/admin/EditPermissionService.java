package nz.co.searchwellington.controllers.admin;

import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.frontend.FrontendResource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

@Component
public class EditPermissionService {
	
	private LoggedInUserFilter loggedInUserFilter;
	
	@Autowired
	public EditPermissionService(LoggedInUserFilter loggedInUserFilter) {	
		this.loggedInUserFilter = loggedInUserFilter;
	}
	
	public boolean canEdit(FrontendResource resource) {
		User loggedInUser = loggedInUserFilter.getLoggedInUser();
		return isAdminOrOwner(resource, loggedInUser);
	}
	
	public boolean canEdit(Resource resource) {
		User loggedInUser = loggedInUserFilter.getLoggedInUser();
		return isAdminOrOwner(resource, loggedInUser);
	}
	
	public boolean canDelete(FrontendResource resource) {
		User loggedInUser = loggedInUserFilter.getLoggedInUser();
		return isAdminOrOwner(resource, loggedInUser);
	}
	
	public boolean canDelete(Resource resource) {
		User loggedInUser = loggedInUserFilter.getLoggedInUser();
		return isAdminOrOwner(resource, loggedInUser);
	}
	
	public boolean canAcceptAllFrom(FrontendResource resource) {
		User loggedInUser = loggedInUserFilter.getLoggedInUser();
		return loggedInUser != null && loggedInUser.isAdmin();
	}

	public boolean canAcceptAllFrom(Feed feed) {
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
		if (!Strings.isNullOrEmpty(resource.getOwner()) && loggedInUser.getProfilename() == resource.getOwner()) {
			return true;
		}
		return false;
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
		return loggedInUser != null && loggedInUser.isAdmin();
	}

	public boolean canDeleteTags(User loggedInUser) {
		return loggedInUser != null && loggedInUser.isAdmin();
	}
		
}
