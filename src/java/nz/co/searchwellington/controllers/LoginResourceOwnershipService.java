package nz.co.searchwellington.controllers;

import nz.co.searchwellington.filters.RequestFilter;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.User;

public class LoginResourceOwnershipService {
	
	private RequestFilter requestFilter;
	
	public LoginResourceOwnershipService(RequestFilter requestFilter) {
		this.requestFilter = requestFilter;
	}
	
	// TODO should get logged in user from filer bean
	public void assignOwnershipOfAnonSessionResourcesToLoggedInUser(User user) {
		Resource unownedResource = requestFilter.getAnonResource();
		if (user != null && unownedResource != null) {
			assignOwnership(user, unownedResource);
		}
	}


	public void assignOwnership(User user, Resource unownedResource) {
		unownedResource.setOwner(user);
		// TODO really want transactional to work so we don have todo this explict save		
	}


}
