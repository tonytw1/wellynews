package nz.co.searchwellington.model;

import nz.co.searchwellington.model.taggingvotes.voters.TaggingVoter;

public interface User extends TaggingVoter {

    public int getId();
    public void setId(int id);
    
    public String getOpenId();
    public void setOpenId(String openID);
    public Long getTwitterId();
	public void setTwitterId(long twitterid);
	
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
	
	public boolean isUnlinkedAccount();
       
}
