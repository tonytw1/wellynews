package nz.co.searchwellington.openid;

import nz.co.searchwellington.filters.RequestFilter;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.ResourceRepository;

public class LoginResourceOwnershipService {
	
	private ResourceRepository resourceDAO;
	private RequestFilter requestFilter;
	public LoginResourceOwnershipService(ResourceRepository resourceDAO,
			RequestFilter requestFilter) {
		super();
		this.resourceDAO = resourceDAO;
		this.requestFilter = requestFilter;
	}
	
	
	public void assignOwnership(User user) {
		Resource unownedResource = requestFilter.getAnonResource();
		if (unownedResource != null) {
			unownedResource.setOwner(user);
			resourceDAO.saveResource(unownedResource);			
		}		
	}
	
}
