package nz.co.searchwellington.controllers;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.UserRepository;

import org.apache.log4j.Logger;

public class LoggedInUserFilter {
	
	
	Logger log = Logger.getLogger(LoggedInUserFilter.class);
	   	
	private User loggedInUser;
	private UserRepository userDAO;
		
	public LoggedInUserFilter() {
	}

	public LoggedInUserFilter(UserRepository userDAO) {	
		this.loggedInUser = null;
		this.userDAO = userDAO; 
	}
	
	public void loadLoggedInUser(HttpServletRequest request) {		 
		if (request.getParameter("apikey") != null) {
			final String apiKey = request.getParameter("apikey");
			log.debug("Looking for an user by api key: " + apiKey);
			User user = userDAO.getUserByApiKey(apiKey);
			if (user != null) {
				this.loggedInUser = user;
				return;
			}
		}
		
		log.info("Looking for logged in user in session");
		if (request.getSession().getAttribute("user") != null) {
			User sessionUser =  (User) request.getSession().getAttribute("user");
			log.debug("Found user on session: " + sessionUser);
			this.loggedInUser = sessionUser;
			return;
		}
				
		this.loggedInUser = null;
	}

	public User getLoggedInUser() {
		return loggedInUser;
	}
	
}
