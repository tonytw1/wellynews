package nz.co.searchwellington.controllers.submission;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.utils.UrlCleaner;

public class UrlProcessor implements SubmissionProcessor {
	
	private UrlCleaner urlCleaner;

	public UrlProcessor(UrlCleaner urlCleaner) {
		this.urlCleaner = urlCleaner;
	}

	@Override
	public void process(HttpServletRequest request, Resource editResource) {
		if (request.getParameter("url") != null) {
			String url = request.getParameter("url");
			editResource.setUrl(urlCleaner.cleanSubmittedItemUrl(url));
		}
	}

}
