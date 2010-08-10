package nz.co.searchwellington.repositories;

import java.util.List;

import nz.co.searchwellington.model.User;


public interface UserRepository {
	
	public User getUserByOpenId(String openId);
	public void saveUser(User newUser);
	public User getUserByProfileName(String profilename);
	public User getUserByApiKey(String apiKey);
	public User getUserByTwitterId(int twitterId);
	public int getNextAvailableAnonUserNumber();
	public List<User> getActiveUsers();
	
}




