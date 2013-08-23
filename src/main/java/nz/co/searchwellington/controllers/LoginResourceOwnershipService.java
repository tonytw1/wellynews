package nz.co.searchwellington.controllers;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.HandTaggingService;
import nz.co.searchwellington.repositories.HibernateBackedUserDAO;
import nz.co.searchwellington.repositories.HibernateResourceDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoginResourceOwnershipService {
	
	private HibernateResourceDAO resourceDAO;
	private HibernateBackedUserDAO userDAO;
	private HandTaggingService handTaggingService;
	
	@Autowired
	public LoginResourceOwnershipService(HibernateResourceDAO resourceDAO, HibernateBackedUserDAO userDAO, HandTaggingService handTaggingService) {
		this.resourceDAO = resourceDAO;
		this.userDAO = userDAO;
		this.handTaggingService = handTaggingService;
	}
	
	public void reassignOwnership(User previousOwner, User newOwner) {
		for (Resource resource : resourceDAO.getOwnedBy(previousOwner, 1000)) {
			resource.setOwner(newOwner);
			resourceDAO.saveResource(resource);
		}		
		handTaggingService.transferVotes(previousOwner, newOwner);
		userDAO.deleteUser(previousOwner);		
	}

}
