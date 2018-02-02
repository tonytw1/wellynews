package nz.co.searchwellington.controllers.submission;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.utils.UrlCleaner;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UrlProcessor implements SubmissionProcessor {
	
	private final static Logger log = Logger.getLogger(UrlProcessor.class);

	private final static String URL = "url";
	
	private final  UrlCleaner urlCleaner;

	@Autowired
	public UrlProcessor(UrlCleaner urlCleaner) {
		this.urlCleaner = urlCleaner;
	}

	@Override
	public void process(HttpServletRequest request, Resource editResource) {
		if (request.getParameter(URL) != null) {	
			final String url = request.getParameter(URL);
			log.debug("Request url is: " + url);
			// TODO editResource.setUrl(urlCleaner.cleanSubmittedItemUrl(url));
			log.debug("Resource url set to: " + editResource.getUrl());			
		} else {
			log.debug("Url was null");
		}
	}

}
