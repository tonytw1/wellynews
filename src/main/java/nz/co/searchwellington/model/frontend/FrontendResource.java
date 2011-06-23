package nz.co.searchwellington.model.frontend;

import java.util.Date;

public interface FrontendResource {

	public int getId();
	public String getName();
	public String getUrl();
	public int getHttpStatus();
	public Date getDate();
	public String getDescription();
	
}
