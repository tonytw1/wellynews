package nz.co.searchwellington.controllers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.models.ContentModelBuilderService;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import com.sun.syndication.io.FeedException;

@Controller
public class TagController extends MultiActionController {
	
    private ContentModelBuilderService contentModelBuilder;
    private UrlStack urlStack;
    
    public TagController() {
	}
    
	public TagController(ContentModelBuilderService contentModelBuilder, UrlStack urlStack) {
		this.contentModelBuilder = contentModelBuilder;
		this.urlStack = urlStack;
	}
	
	@RequestMapping(value={"/", "/*", "/search", "/archive/*/*", "/*/comment", "/*/geotagged", "/feed/*", "/feeds/inbox", "/*/json", "/*/rss", "/*/*/*/*/*"})
	public ModelAndView normal(HttpServletRequest request, HttpServletResponse response) throws IllegalArgumentException, FeedException, IOException {
		ModelAndView mv = contentModelBuilder.populateContentModel(request);
		if (mv != null) {
			if (isHtmlView(mv)) {
				urlStack.setUrlStack(request);
			}
			return mv;
		}
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		return null;
    }
	
	private boolean isHtmlView(ModelAndView mv) {
		return mv.getViewName() != null;
	}
	
}
