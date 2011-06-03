package nz.co.searchwellington.controllers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.models.ContentModelBuilderService;
import nz.co.searchwellington.repositories.TagDAO;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import com.sun.syndication.io.FeedException;

public class TagController extends MultiActionController {
	
    static Logger log = Logger.getLogger(TagController.class);
   
    private ContentModelBuilderService contentModelBuilder;
    private TagDAO tagDAO;
    private UrlStack urlStack;

    public TagController(UrlStack urlStack,
			ContentModelBuilderService contentModelBuilder,
			TagDAO tagDAO) {
		this.urlStack = urlStack;
		this.contentModelBuilder = contentModelBuilder;
		this.tagDAO = tagDAO;
	}
    
	public ModelAndView normal(HttpServletRequest request, HttpServletResponse response) throws IllegalArgumentException, FeedException, IOException {
        log.info("Starting normal content");                                  
        boolean showBroken = false;
        
		ModelAndView mv = contentModelBuilder.populateContentModel(request);
		if (mv != null) {			
			boolean isHtmlView = isHtmlView(mv);			
			if (isHtmlView) {
				urlStack.setUrlStack(request);
				addCommonModelElements(mv, showBroken);
			}			
			return mv;
		}
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		return null;
    }
	
	private boolean isHtmlView(ModelAndView mv) {
		return mv.getViewName() != null;
	}
		
	private void addCommonModelElements(ModelAndView mv, boolean showBroken) throws IOException {
		mv.addObject("top_level_tags", tagDAO.getTopLevelTags());      
	}
    
}
