package nz.co.searchwellington.controllers.admin;

import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.User;

import org.apache.log4j.Logger;

public class EditPermissionService {

	Logger log = Logger.getLogger(EditPermissionService.class);
	   
	
	private LoggedInUserFilter loggedInUserFilter;
	

	public EditPermissionService(LoggedInUserFilter loggedInUserFilter) {	
		this.loggedInUserFilter = loggedInUserFilter;
	}

	
	public boolean canEdit(Resource resource) {
		User loggedInUser = loggedInUserFilter.getLoggedInUser();		
		return loggedInUser != null;
	}
	
	public boolean canEdit(Tag tag) {
		User loggedInUser = loggedInUserFilter.getLoggedInUser();		
		return loggedInUser != null;
	}
		
}
