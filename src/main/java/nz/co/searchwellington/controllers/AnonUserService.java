package nz.co.searchwellington.controllers;

import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.UserImpl;
import nz.co.searchwellington.repositories.UserRepository;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AnonUserService {

	private static Logger log = Logger.getLogger(AnonUserService.class);
    
	private UserRepository userDAO;
		
	public AnonUserService() {
	}

	@Autowired
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
