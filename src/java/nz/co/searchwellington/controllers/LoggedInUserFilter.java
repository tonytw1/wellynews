package nz.co.searchwellington.controllers;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.model.User;

import org.apache.log4j.Logger;

public class LoggedInUserFilter {
	
	
	Logger log = Logger.getLogger(LoggedInUserFilter.class);
	   	
	private User loggedInUser;
	
	public LoggedInUserFilter() {	
		this.loggedInUser = null;
	}
	
	public void loadLoggedInUser(HttpServletRequest request) {
		log.info("Looking for logged in user in session");
		if (request.getSession().getAttribute("user") != null) {
			User sessionUser =  (User) request.getSession().getAttribute("user");
			log.info("Found user on session: " + sessionUser);
			this.loggedInUser = sessionUser;
			return;
		}
		this.loggedInUser = null;
	}

	public User getLoggedInUser() {
		return loggedInUser;
	}
	
}
