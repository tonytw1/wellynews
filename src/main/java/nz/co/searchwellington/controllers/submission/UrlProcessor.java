package nz.co.searchwellington.controllers.submission;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.utils.UrlCleaner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UrlProcessor implements SubmissionProcessor {
	
	private static final String URL = "url";
	
	private UrlCleaner urlCleaner;

	@Autowired
	public UrlProcessor(UrlCleaner urlCleaner) {
		this.urlCleaner = urlCleaner;
	}

	@Override
	public void process(HttpServletRequest request, Resource editResource) {
		if (request.getParameter(URL) != null) {
			String url = request.getParameter(URL);
			editResource.setUrl(urlCleaner.cleanSubmittedItemUrl(url));
		}
	}

}
