package nz.co.searchwellington.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import nz.co.searchwellington.model.User;

@Component
public class ShowBrokenDecisionService {

	private LoggedInUserFilter loggedInUserFilter;
	
	@Autowired
	public ShowBrokenDecisionService(LoggedInUserFilter loggedInUserFilter) {
		this.loggedInUserFilter = loggedInUserFilter;
	}
	
	public boolean shouldShowBroken() {
		User loggedInUser = loggedInUserFilter.getLoggedInUser();
		return loggedInUser != null && loggedInUser.isAdmin();
	}

}
