package nz.co.searchwellington.model;




public class UserImpl implements User {

    int id;
    String username;
    String profilename;
    String url;
    boolean admin;
    
  
    public UserImpl() {
    }
    
    public UserImpl(String username) {
    	this.username = username;
	}


	public int getId() {
        return id;
    }


    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    
    

	public String getProfilename() {
		return profilename;
	}

	public void setProfilename(String profilename) {
		this.profilename = profilename;
	}

	public boolean isAdmin() {
		return admin;
	}


	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
}