package nz.co.searchwellington.controllers;

import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Website;

import org.apache.log4j.Logger;

public class PublisherNewsitemCountService {
	
	Logger log = Logger.getLogger(PublisherNewsitemCountService.class);
	  
	// TODO make count showBroken conditional based on loggedInUserFilter.
	public int getNewsitemCount(Website publisher) {
		int count = publisher.getNewsitems().size();
		boolean showBroken = false;
		if (!showBroken && count > 0) {
			log.info("Manually counting " + count + " ok newsitems for publisher: " + publisher.getName());
			count = 0;
			for (Resource newsitem : publisher.getNewsitems()) {
				if (newsitem.getHttpStatus() == HttpServletResponse.SC_OK) {
					count++;
				}
			}
			
		}
		return count;
	}
	
}
