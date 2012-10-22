package nz.co.searchwellington.controllers.admin;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.modification.ContentUpdateService;
import nz.co.searchwellington.repositories.HandTaggingDAO;
import nz.co.searchwellington.repositories.HibernateResourceDAO;
import nz.co.searchwellington.repositories.TagDAO;
import nz.co.searchwellington.repositories.solr.KeywordSearchService;
import nz.co.searchwellington.tagging.ImpliedTagService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class AutoTagController {

    private static Logger log = Logger.getLogger(AutoTagController.class);
    
    private HibernateResourceDAO resourceDAO;
    private AdminRequestFilter requestFilter;
    private ImpliedTagService autoTagService;
	private KeywordSearchService keywordSearchService;
	private ContentUpdateService contentUpateService;
	private TagDAO tagDAO;
	private HandTaggingDAO tagVoteDAO;
	private LoggedInUserFilter loggedInUserFilter;
    
	public AutoTagController() {
	}
	
	@Autowired
	public AutoTagController(HibernateResourceDAO resourceDAO,
			AdminRequestFilter requestFilter, ImpliedTagService autoTagService,
			KeywordSearchService keywordSearchService,
			ContentUpdateService contentUpateService, TagDAO tagDAO,
			HandTaggingDAO tagVoteDAO,
			LoggedInUserFilter loggedInUserFilter) {
		this.resourceDAO = resourceDAO;
		this.requestFilter = requestFilter;
		this.autoTagService = autoTagService;
		this.keywordSearchService = keywordSearchService;
		this.contentUpateService = contentUpateService;
		this.tagDAO = tagDAO;
		this.tagVoteDAO = tagVoteDAO;
		this.loggedInUserFilter = loggedInUserFilter;
	}
	
	@RequestMapping("/*/autotag")
	public ModelAndView prompt(HttpServletRequest request, HttpServletResponse response) {
		User loggedInUser = loggedInUserFilter.getLoggedInUser();
    	if (loggedInUser == null) {
    		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        	return null;
    	}
    	
        final ModelAndView mv = new ModelAndView();        
        mv.setViewName("autoTagPrompt");        
        mv.addObject("top_level_tags", tagDAO.getTopLevelTags());
        mv.addObject("heading", "Autotagging");
        
        requestFilter.loadAttributesOntoRequest(request);
        Tag tag = (Tag) request.getAttribute("tag");
        if (tag == null) {
        	response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        	return null;
        }
                
        mv.addObject("tag", tag);
        mv.addObject("resources_to_tag", getPossibleAutotagResources(loggedInUserFilter.getLoggedInUser(), tag));
        return mv;
    }
	
	@RequestMapping(value="/*/autotag/apply", method=RequestMethod.POST)
    public ModelAndView apply(HttpServletRequest request, HttpServletResponse response) {    	
    	User loggedInUser = loggedInUserFilter.getLoggedInUser();
    	if (loggedInUser == null) {
    		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        	return null;
    	}
        
        requestFilter.loadAttributesOntoRequest(request);
        Tag tag = (Tag) request.getAttribute("tag");
        if (tag == null) {
        	response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        	return null;
        }
        
        final ModelAndView mv = new ModelAndView();
        mv.setViewName("autoTagApply");
        mv.addObject("heading", "Autotagging");
        
        mv.addObject("top_level_tags", tagDAO.getTopLevelTags());
        mv.addObject("tag", tag);
        List<Resource> resourcesAutoTagged = new ArrayList<Resource>();
        String[] autotaggedResourceIds = request.getParameterValues("autotag");
        for (String resourceIdString : autotaggedResourceIds) {
        	int resourceId = Integer.parseInt(resourceIdString);
        	Resource resource = resourceDAO.loadResourceById(resourceId);

        	log.info("Applying tag " + tag.getName() + " to:" + resource.getName());
        	if (!autoTagService.alreadyHasTag(resource, tag)) {
                tagVoteDAO.addTag(loggedInUser, tag, resource);
        	}
        	contentUpateService.update(resource);
        	resourcesAutoTagged.add(resource);
        }        
        mv.addObject("resources_to_tag", resourcesAutoTagged);             
        return mv;
    }
    
    private List<FrontendResource> getPossibleAutotagResources(User user, Tag tag) {
    	return keywordSearchService.getResourcesMatchingKeywordsNotTaggedByUser(tag.getDisplayName(), true, user, tag);
	}
    
}
