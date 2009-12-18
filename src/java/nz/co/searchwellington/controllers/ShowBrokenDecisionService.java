package nz.co.searchwellington.controllers;

import nz.co.searchwellington.model.User;

public class ShowBrokenDecisionService {

	private LoggedInUserFilter loggedInUserFilter;
	
	
	public ShowBrokenDecisionService(LoggedInUserFilter loggedInUserFilter) {
		this.loggedInUserFilter = loggedInUserFilter;
	}


	public boolean shouldShowBroken() {
		User loggedInUser = loggedInUserFilter.getLoggedInUser();
		return loggedInUser != null && loggedInUser.isAdmin();
	}

}
