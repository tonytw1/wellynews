package nz.co.searchwellington.repositories;

import nz.co.searchwellington.model.User;


public interface UserRepository {
	
    public User getUser(String username, String password);
	public User getUser(String username);

}




