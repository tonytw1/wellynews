package nz.co.searchwellington.controllers.submission;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.utils.UrlCleaner;

@Component
public class UrlProcessor implements SubmissionProcessor {
	
	private UrlCleaner urlCleaner;

	@Autowired
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
