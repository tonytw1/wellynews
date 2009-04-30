package nz.co.searchwellington.controllers.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.BaseMultiActionController;
import nz.co.searchwellington.controllers.UrlStack;
import nz.co.searchwellington.filters.RequestFilter;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.tagging.ImpliedTagService;

import org.apache.log4j.Logger;
import org.apache.lucene.queryParser.ParseException;
import org.springframework.web.servlet.ModelAndView;

public class AutoTagController extends BaseMultiActionController {

    Logger log = Logger.getLogger(AutoTagController.class);
    
    private RequestFilter requestFilter;
    private ImpliedTagService autoTagService;
    
	public AutoTagController(ResourceRepository resourceDAO, RequestFilter requestFilter, UrlStack urlStack, ImpliedTagService autoTagService) {      
		this.resourceDAO = resourceDAO;        
        this.requestFilter = requestFilter;       
        this.urlStack = urlStack;
        this.autoTagService = autoTagService;
	}


	@SuppressWarnings("unchecked")
    public ModelAndView prompt(HttpServletRequest request, HttpServletResponse response) throws IOException, ParseException {        
        ModelAndView mv = new ModelAndView();        
        mv.setViewName("autoTagPrompt");
        
        mv.getModel().put("top_level_tags", resourceDAO.getTopLevelTags());
        mv.getModel().put("heading", "Autotagging");
        
        requestFilter.loadAttributesOntoRequest(request);
        Tag tag = (Tag) request.getAttribute("tag");
        mv.getModel().put("tag", tag);
        if (tag != null) {            
        	List<Resource> resourcesToAutoTag = new ArrayList<Resource>();      
            for (Resource resource : getPossibleAutotagResources(tag)) {
                if (!autoTagService.alreadyHasTag(resource, tag)) {
                    resourcesToAutoTag.add(resource);
                }
            }
            mv.addObject("resources_to_tag", resourcesToAutoTag);
        }
        return mv;
    }


    public ModelAndView apply(HttpServletRequest request, HttpServletResponse response) throws IOException, ParseException {        
        ModelAndView mv = new ModelAndView();
        mv.setViewName("autoTagApply");
        
        mv.addObject("top_level_tags", resourceDAO.getTopLevelTags());
        mv.addObject("heading", "Autotagging");
        
        requestFilter.loadAttributesOntoRequest(request);
        Tag editTag = (Tag) request.getAttribute("tag");
        if (editTag != null) {
            mv.addObject("tag", editTag);
            
            List<Resource> resourcesAutoTagged = new ArrayList<Resource>();            
            String[] autotaggedResourceIds = request.getParameterValues("autotag");            
            for (String resourceIdString : autotaggedResourceIds) {
                int resourceId = Integer.parseInt(resourceIdString);
                Resource resource = resourceDAO.loadResourceById(resourceId);

                log.info("Applying tag " + editTag.getName() + " to:" + resource.getName());
                if (!autoTagService.alreadyHasTag(resource, editTag)) {
                	resource.addTag(editTag);
                }
                resourceDAO.saveResource(resource);
                resourcesAutoTagged.add(resource);
            }        
            mv.addObject("resources_to_tag", resourcesAutoTagged);
        }     
        return mv;
    }
    
    
    private List<Resource> getPossibleAutotagResources(Tag editTag) throws IOException, ParseException {
		List<Resource> resources = resourceDAO.getWebsitesMatchingKeywords(editTag.getDisplayName(), true);
		resources.addAll(resourceDAO.getNewsitemsMatchingKeywords(editTag.getDisplayName(), true));
		return resources;
	}
    
    
}
