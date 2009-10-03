package nz.co.searchwellington.repositories;

import nz.co.searchwellington.model.User;


public interface UserRepository {
	
	public User getUser(String username);
	public void saveUser(User newUser);
	public User getUserByProfileName(String profilename);
	public User getUserByApiKey(String apiKey);

}




