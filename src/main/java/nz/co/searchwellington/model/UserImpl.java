package nz.co.searchwellington.model;

import nz.co.searchwellington.model.User;




public class UserImpl implements User {

    int id;
    String openid;
    Integer twitterId;
    String profilename;
    String url;
    String name;
    String bio;

    String apikey;
    
    
    boolean admin;
    
  
    public UserImpl() {
    }
    
    public UserImpl(String openid) {
    	this.openid = openid;
	}
    
	public UserImpl(int twitterId) {		
		this.twitterId = twitterId;
	}

	public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOpenId() {
        return openid;
    }

    public void setOpenId(String openid) {
        this.openid = openid;
    }

	@Override
	public Integer getTwitterId() {
		return twitterId;
	}

	@Override
	public void setTwitterId(Integer twitterid) {
		this.twitterId = twitterid;
	}

	@Override
	public boolean isUnlinkedAnonAccount() {
		return (openid == null && twitterId == null) && this.profilename != "autotagger";	// TODO
	}

	public String getProfilename() {
		return profilename;
	}

	public void setProfilename(String profilename) {
		this.profilename = profilename;
	}

	@Override
	public String getVoterName() {
		return this.getProfilename();
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