package nz.co.searchwellington.repositories;

import java.util.List;

import nz.co.searchwellington.model.User;


public interface UserRepository {
	
	public User getUser(String username);
	public void saveUser(User newUser);
	public User getUserByProfileName(String profilename);
	public User getUserByApiKey(String apiKey);
	public User getUserByTwitterName(String screenName);
	public int getNextAvailableAnonUserNumber();
	public List<User> getActiveUsers();
	
}




