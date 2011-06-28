package nz.co.searchwellington.model.frontend;

import java.util.Date;
import java.util.List;

import nz.co.searchwellington.model.Tag;

public interface FrontendResource {

	public int getId();
	public String getName();
	public String getUrl();
	public int getHttpStatus();
	public Date getDate();
	public String getDescription();
	public Date getLiveTime();
	
	public List<Tag> getTags();
	public List<Tag> getHandTags();
	
}
