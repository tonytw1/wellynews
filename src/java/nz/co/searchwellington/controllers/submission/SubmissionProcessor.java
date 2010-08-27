package nz.co.searchwellington.controllers.submission;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.model.Resource;

public interface SubmissionProcessor {

	void process(HttpServletRequest request, Resource editResource);

}
