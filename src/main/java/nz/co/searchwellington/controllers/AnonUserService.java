package nz.co.searchwellington.controllers;

import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.UserImpl;
import nz.co.searchwellington.repositories.HibernateBackedUserDAO;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AnonUserService {

	private static Logger log = Logger.getLogger(AnonUserService.class);
    
	private HibernateBackedUserDAO userDAO;
		
	public AnonUserService() {
	}

	@Autowired
	public AnonUserService(HibernateBackedUserDAO userDAO) {
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
