package nz.co.searchwellington.controllers;

import org.apache.log4j.Logger;

import net.fortuna.ical4j.model.Calendar;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.UserImpl;
import nz.co.searchwellington.repositories.UserRepository;

public class AnonUserService {

    static Logger log = Logger.getLogger(AnonUserService.class);
    
	UserRepository userDAO;
	
	public AnonUserService(UserRepository userDAO) {
		this.userDAO = userDAO;
	}

	public User createAnonUser() {		
		final int userNumber = userDAO.getNextAvailableAnonUserNumber();
		
		User anonUser = new UserImpl();		
		anonUser.setProfilename("anon" + userNumber);
		
		log.info("Created new anon user: " + anonUser.getProfilename());
		userDAO.saveUser(anonUser);
		return anonUser;
	}

}
