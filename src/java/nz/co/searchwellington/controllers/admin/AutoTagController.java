package nz.co.searchwellington.controllers.admin;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.BaseMultiActionController;
import nz.co.searchwellington.controllers.UrlStack;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.modification.ContentUpdateService;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.TagDAO;
import nz.co.searchwellington.repositories.solr.KeywordSearchService;
import nz.co.searchwellington.tagging.ImpliedTagService;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

public class AutoTagController extends BaseMultiActionController {

    Logger log = Logger.getLogger(AutoTagController.class);
    
    private ResourceRepository resourceDAO;
    private AdminRequestFilter requestFilter;
    private ImpliedTagService autoTagService;
	private KeywordSearchService keywordSearchService;
	private ContentUpdateService contentUpateService;
	private TagDAO tagDAO;
    
	public AutoTagController(ResourceRepository resourceDAO, AdminRequestFilter requestFilter, UrlStack urlStack, ImpliedTagService autoTagService, KeywordSearchService keywordSearchService, TagDAO tagDAO, ContentUpdateService contentUpateService) {      
		this.resourceDAO = resourceDAO;        
        this.requestFilter = requestFilter;       
        this.urlStack = urlStack;
        this.autoTagService = autoTagService;
        this.keywordSearchService = keywordSearchService;
        this.tagDAO = tagDAO;
        this.contentUpateService = contentUpateService;
	}


	public ModelAndView prompt(HttpServletRequest request, HttpServletResponse response) {        
        ModelAndView mv = new ModelAndView();        
        mv.setViewName("autoTagPrompt");
        
        mv.addObject("top_level_tags", tagDAO.getTopLevelTags());
        mv.addObject("heading", "Autotagging");
        
        requestFilter.loadAttributesOntoRequest(request);
        Tag tag = (Tag) request.getAttribute("tag");
        mv.addObject("tag", tag);
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


    public ModelAndView apply(HttpServletRequest request, HttpServletResponse response) {        
        ModelAndView mv = new ModelAndView();
        mv.setViewName("autoTagApply");
        
        mv.addObject("top_level_tags", tagDAO.getTopLevelTags());
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
                contentUpateService.update(resource, false);
                resourcesAutoTagged.add(resource);
            }        
            mv.addObject("resources_to_tag", resourcesAutoTagged);
        }     
        return mv;
    }
    
    
    private List<Resource> getPossibleAutotagResources(Tag editTag) {
    	List<Resource> resources = keywordSearchService.getWebsitesMatchingKeywords(editTag.getDisplayName(), true, null);
    	resources.addAll(keywordSearchService.getNewsitemsMatchingKeywords(editTag.getDisplayName(), true, null));
    	return resources;    	
	}
        
}
