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
    
    public String getPassword();
    public void setPassword(String password);
    
 
    
}
