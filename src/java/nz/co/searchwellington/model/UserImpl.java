package nz.co.searchwellington.model;

import nz.co.searchwellington.model.User;




public class UserImpl implements User {

    int id;
    String username;
    String profilename;
    String url;
    String name;
    String bio;

    String apikey;
    
    
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBio() {
		return bio;
	}

	public void setBio(String bio) {
		this.bio = bio;
	}

	public String getApikey() {
		return apikey;
	}

	public void setApikey(String apikey) {
		this.apikey = apikey;
	}
		
}