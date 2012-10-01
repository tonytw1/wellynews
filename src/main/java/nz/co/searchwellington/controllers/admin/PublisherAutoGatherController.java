package nz.co.searchwellington.controllers.admin;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.modification.ContentUpdateService;
import nz.co.searchwellington.repositories.HibernateResourceDAO;
import nz.co.searchwellington.repositories.TagDAO;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class PublisherAutoGatherController {

    private static Logger log = Logger.getLogger(PublisherAutoGatherController.class);
    
    private AdminRequestFilter requestFilter;
    private TagDAO tagDAO;
    private HibernateResourceDAO resourceDAO;
    private ContentUpdateService contentUpdateService;

    public PublisherAutoGatherController() {
	}
    
    public PublisherAutoGatherController(AdminRequestFilter requestFilter,
			TagDAO tagDAO, HibernateResourceDAO resourceDAO,
			ContentUpdateService contentUpdateService) {
		this.requestFilter = requestFilter;
		this.tagDAO = tagDAO;
		this.resourceDAO = resourceDAO;
		this.contentUpdateService = contentUpdateService;
	}
    
	@RequestMapping("/admin/gather/prompt")
	public ModelAndView prompt(HttpServletRequest request, HttpServletResponse response) {        
        ModelAndView mv = new ModelAndView();
        
        mv.setViewName("autoGatherPrompt");       
        mv.addObject("top_level_tags", tagDAO.getTopLevelTags());
        mv.addObject("heading", "Auto Gathering");
        
        requestFilter.loadAttributesOntoRequest(request);
        Website publisher = (Website) request.getAttribute("publisher");
        mv.addObject("publisher", publisher);
        
        if (publisher != null) {    
        	List<Resource> resourcesToAutoTag = new ArrayList<Resource>();      
        	for (Resource resource : getPossibleAutotagResources(publisher)) {
        		if (needsPublisher((Newsitem) resource, publisher)) {
        			resourcesToAutoTag.add(resource);
        		}
        	}
            mv.addObject("resources_to_tag", resourcesToAutoTag);
        }
        return mv;
    }
	
	@RequestMapping(value="/admin/gather/apply", method=RequestMethod.POST)
	public ModelAndView apply(HttpServletRequest request, HttpServletResponse response) {        
        ModelAndView mv = new ModelAndView();
        mv.setViewName("autoGatherApply");      
        mv.addObject("top_level_tags", tagDAO.getTopLevelTags());
        mv.addObject("heading", "Auto Gathering");
        
        requestFilter.loadAttributesOntoRequest(request);
        Website publisher = (Website) request.getAttribute("publisher");
        mv.addObject("publisher", publisher);
                
        if (publisher != null) {                       
            List<Resource> resourcesAutoTagged = new ArrayList<Resource>();            
            String[] autotaggedResourceIds = request.getParameterValues("autotag");            
            for (String resourceIdString : autotaggedResourceIds) {
                int resourceId = Integer.parseInt(resourceIdString);
                Resource resource = resourceDAO.loadResourceById(resourceId);

                if (resource.getType().equals("N")) {
                	log.info("Applying publisher " + publisher.getName() + " to:" + resource.getName());
                	((Newsitem) resource).setPublisher(publisher);
                	contentUpdateService.update(resource);
                	resourcesAutoTagged.add(resource);
                }
            }
            mv.addObject("resources_to_tag", resourcesAutoTagged);
        }     
        return mv;
    }
	
	    
    private List<Resource> getPossibleAutotagResources(Resource publisher) {
		final String publishersUrlStem = calculateUrlStem(publisher.getUrl());
		return resourceDAO.getNewsitemsMatchingStem(publishersUrlStem);
	}
    
	
    private boolean needsPublisher(Newsitem resource, Website proposedPublisher) {
    	if (resource.getPublisher() == null) {
    		return true;
    	}
    	if (resource.getPublisher() != proposedPublisher) {
    		return true;    		
    	}
		return false;
	}

        
    // TODO duplication with Publisher guessing service.
    private String calculateUrlStem(String fullURL) {
        String urlStem = null;        
        try {
            URL url = new URL(fullURL);
            String stem = new String(url.getHost());
            urlStem = stem;        
        } catch (MalformedURLException e) {
            urlStem = null;
        }        
        return urlStem;
    }
    
}
