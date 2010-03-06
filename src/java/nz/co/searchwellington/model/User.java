package nz.co.searchwellington.model;


/**
 * Defines a toplevel user object which can login and out of a web application.
 * Shared across multiple web applications; allows the login servlet to be reused.
 * 
 * @author tony
 *
 */
public interface User {

    public int getId();
    public void setId(int id);
    
    public String getUsername();
    public void setUsername(String username);
	public boolean isAdmin();
	public void setAdmin(boolean admin);
	public String getUrl();
	public void setUrl(String url);
	public String getProfilename();
	public void setProfilename(String profilename);
	public String getName();
	public void setName(String name);
	public String getBio();
	public void setBio(String bio);
	public String getApikey();
	public void setApikey(String apikey);
	
	public boolean isUnlinkedAnonAccount();
       
}
