package nz.co.searchwellington.controllers;

import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.UserImpl;
import nz.co.searchwellington.repositories.UserRepository;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

public class AnonUserService {

    static Logger log = Logger.getLogger(AnonUserService.class);
    
	UserRepository userDAO;
	
	
	public AnonUserService() {
	}

	public AnonUserService(UserRepository userDAO) {
		this.userDAO = userDAO;
	}

	@Transactional
	public User createAnonUser() {		
		final int userNumber = userDAO.getNextAvailableAnonUserNumber();
		
		User anonUser = new UserImpl();		
		anonUser.setProfilename("anon" + userNumber);
		
		log.info("Created new anon user: " + anonUser.getProfilename());
		userDAO.saveUser(anonUser);
		return anonUser;
	}

}
