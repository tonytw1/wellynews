package nz.co.searchwellington.model;

public interface TwitterSubmittable {
    
	public Long getTwitterId();
	public void setTwitterId(Long twitterId);
	
    public String getTwitterMessage();
    public void setTwitterMessage(String twitterMessage);
    
    public String getTwitterSubmitter();
    public void setTwitterSubmitter(String submitter);
    

}
