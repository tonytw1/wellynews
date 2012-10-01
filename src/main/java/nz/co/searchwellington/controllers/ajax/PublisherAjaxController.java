package nz.co.searchwellington.controllers.ajax;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.repositories.ContentRetrievalService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import uk.co.eelpieconsulting.common.views.ViewFactory;

@Controller
public class PublisherAjaxController extends BaseAjaxController {
	
    private static Logger log = Logger.getLogger(PublisherAjaxController.class);
	
    private ContentRetrievalService contentRetrievalService;
    
    public PublisherAjaxController() {
	}
    
    @Autowired
	public PublisherAjaxController(ViewFactory viewFactory, ContentRetrievalService contentRetrievalService) {
		this.viewFactory = viewFactory;
		this.contentRetrievalService = contentRetrievalService;
	}

	@RequestMapping("/ajax/publishers")
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
		return super.handleRequest(request, response);
	}
	
	protected List<String> getSuggestions(String q) {
        log.info("Looking up possible publishers starting with: " + q);
        return contentRetrievalService.getPublisherNamesByStartingLetters(q);    
	}
    
}
