package nz.co.searchwellington.controllers.permissions;

import com.google.common.base.Strings;
import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.model.*;
import nz.co.searchwellington.model.frontend.FrontendResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EditPermissionService {
	
	private LoggedInUserFilter loggedInUserFilter;
	
	@Autowired
	public EditPermissionService(LoggedInUserFilter loggedInUserFilter) {	
		this.loggedInUserFilter = loggedInUserFilter;
	}
	
	public boolean canEdit(FrontendResource resource) {
        return isAdminOrOwner(resource, loggedInUserFilter.getLoggedInUser());
	}
	
	public boolean canEdit(Resource resource) {
        return isAdminOrOwner(resource, loggedInUserFilter.getLoggedInUser());
	}
	
	public boolean canDelete(FrontendResource resource) {
        return isAdminOrOwner(resource, loggedInUserFilter.getLoggedInUser());
	}
	
	public boolean canDelete(Resource resource) {
        return isAdminOrOwner(resource, loggedInUserFilter.getLoggedInUser());
	}
	
	public boolean canAcceptAll() {
        return isAdmin(loggedInUserFilter.getLoggedInUser());
	}

	public boolean canAcceptAllFrom(Feed feed) {
        return canAcceptAll();
	}
	
	public boolean canCheck(FrontendResource resource) {
        return isAdmin(loggedInUserFilter.getLoggedInUser());
	}

	public boolean canSeeLocalPage(Newsitem newsitem) {
        return isAdmin(loggedInUserFilter.getLoggedInUser());
	}

	public boolean canEditSuggestions() {
		User loggedInUser = loggedInUserFilter.getLoggedInUser();
		return isAdmin(loggedInUser);
	}

	public boolean canAddTag() {
        return isAdmin(loggedInUserFilter.getLoggedInUser());
	}
	
	public boolean canEdit(Tag tag) {
		User loggedInUser = loggedInUserFilter.getLoggedInUser();		
		return loggedInUser != null && loggedInUser.isAdmin();
	}
	
	public boolean canAddWatchlistAndTag() {
        return isAdmin(loggedInUserFilter.getLoggedInUser());
	}

    public boolean canAcceptFeedItems(User loggedInUser) {
        return isAdmin(loggedInUser);
    }

    public boolean canDeleteTags(User loggedInUser) {
        return isAdmin(loggedInUser);
    }
	
	private boolean isAdminOrOwner(FrontendResource resource, User loggedInUser) {
        if (isAdmin(loggedInUser)) {
            return true;
        }
		if (!Strings.isNullOrEmpty(resource.getOwner()) && loggedInUser.getProfilename() == resource.getOwner()) {
			return true;
		}
		return false;
	}

    private boolean isAdmin(User loggedInUser) {
        if (loggedInUser == null) {
            return false;
        }
        return loggedInUser.isAdmin();
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

}
