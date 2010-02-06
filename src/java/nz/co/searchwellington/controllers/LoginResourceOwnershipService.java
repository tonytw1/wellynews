package nz.co.searchwellington.controllers;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.ResourceRepository;

public class LoginResourceOwnershipService {
	
	private ResourceRepository resourceDAO;
	
	
	public LoginResourceOwnershipService(ResourceRepository resourceDAO) {
		this.resourceDAO = resourceDAO;
	}


	public void reassignOwnership(User previousOwner, User newOwner) {
		for (Resource resource : resourceDAO.getOwnedBy(previousOwner, 1000)) {
			resource.setOwner(newOwner);
			resourceDAO.saveResource(resource);
		}
		
	}


}
