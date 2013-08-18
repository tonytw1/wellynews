package nz.co.searchwellington.model.frontend;

import java.util.Date;

public interface FrontendFeed extends FrontendResource {

	String getPublisherName();
	String getUrlWords();
	public Date getLatestItemDate();
	public void setLatestItemDate(Date latestItemDate);
	
}
