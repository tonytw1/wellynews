package nz.co.searchwellington.controllers.admin;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.CommonModelObjectsService;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.modification.ContentUpdateService;
import nz.co.searchwellington.repositories.HibernateResourceDAO;
import nz.co.searchwellington.urls.UrlParser;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Lists;

@Controller
public class PublisherAutoGatherController {

    private static Logger log = Logger.getLogger(PublisherAutoGatherController.class);
    
    private AdminRequestFilter requestFilter;
    private HibernateResourceDAO resourceDAO;
    private ContentUpdateService contentUpdateService;
    private CommonModelObjectsService commonModelObjectsService;
	private UrlParser urlParser;

    public PublisherAutoGatherController() {
	}
    
	@Autowired
	public PublisherAutoGatherController(AdminRequestFilter requestFilter,
			HibernateResourceDAO resourceDAO,
			ContentUpdateService contentUpdateService,
			CommonModelObjectsService commonModelObjectsService,
			UrlParser urlParser) {
		this.requestFilter = requestFilter;
		this.resourceDAO = resourceDAO;
		this.contentUpdateService = contentUpdateService;
		this.commonModelObjectsService = commonModelObjectsService;
		this.urlParser = urlParser;
	}
    
	@RequestMapping("/admin/gather/prompt")
	public ModelAndView prompt(HttpServletRequest request, HttpServletResponse response) {        
        ModelAndView mv = new ModelAndView();
        
        mv.setViewName("autoGatherPrompt");       
        mv.addObject("heading", "Auto Gathering");
        commonModelObjectsService.populateCommonLocal(mv);
        
        requestFilter.loadAttributesOntoRequest(request);
        Website publisher = (Website) request.getAttribute("publisher");
        mv.addObject("publisher", publisher);
        
        if (publisher != null) {    
        	List<Resource> resourcesToAutoTag = Lists.newArrayList();   
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
        mv.addObject("heading", "Auto Gathering");
        commonModelObjectsService.populateCommonLocal(mv);
        
        requestFilter.loadAttributesOntoRequest(request);
        Website publisher = (Website) request.getAttribute("publisher");
        mv.addObject("publisher", publisher);
                
        if (publisher != null) {                       
            List<Resource> resourcesAutoTagged = Lists.newArrayList();            
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
		final String publishersUrlStem = urlParser.extractHostnameFrom(publisher.getUrl());
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
    
}
