package nz.co.searchwellington.controllers.admin;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.UrlStack;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.TagDAO;
import nz.co.searchwellington.widgets.TagWidgetFactory;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

public class TagEditController extends MultiActionController {
    
    Logger log = Logger.getLogger(TagEditController.class);


    private ResourceRepository resourceDAO;
    private AdminRequestFilter requestFilter;
    private TagWidgetFactory tagWidgetFactory;
    private UrlStack urlStack;
    private TagDAO tagDAO;

    
    public TagEditController() {       
    }


    public TagEditController(ResourceRepository resourceDAO, AdminRequestFilter requestFilter, TagWidgetFactory tagWidgetFactory, UrlStack urlStack, TagDAO tagDAO) {
        this.resourceDAO = resourceDAO;
        this.requestFilter = requestFilter;
        this.tagWidgetFactory = tagWidgetFactory;
        this.urlStack = urlStack;
        this.tagDAO = tagDAO;
        
    }
    
    
    @Transactional
    public ModelAndView submit(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView("submitTag");    
        modelAndView.addObject("top_level_tags", tagDAO.getTopLevelTags());
        modelAndView.addObject("heading", "Submitting a Tag");

        Tag editTag = resourceDAO.createNewTag();

        modelAndView.addObject("tag", editTag);
        modelAndView.addObject("tag_select", tagWidgetFactory.createTagSelect("parent", editTag.getParent(), new HashSet<Tag>()).toString());
        return modelAndView;
    }
    
    
    @Transactional
    public ModelAndView edit(HttpServletRequest request, HttpServletResponse response) {        
        ModelAndView mv = new ModelAndView("editTag");   
        mv.addObject("top_level_tags", tagDAO.getTopLevelTags());
        mv.addObject("heading", "Editing a Tag");

        Tag editTag = null;
        requestFilter.loadAttributesOntoRequest(request);
           
        if (request.getAttribute("tag") != null) {
            editTag = (Tag) request.getAttribute("tag");         
            mv.addObject("tag", editTag);
            mv.addObject("tag_select", tagWidgetFactory.createTagSelect("parent", editTag.getParent(), editTag.getChildren()).toString());
            mv.addObject("related_feed_select", tagWidgetFactory.createRelatedFeedSelect("feed", editTag.getRelatedFeed()));           
        }
        
        return mv;
    }
    
    
        
    @Transactional
    public ModelAndView delete(HttpServletRequest request, HttpServletResponse response) throws IOException {    
        ModelAndView mv = new ModelAndView("deleteTag"); 
        mv.addObject("top_level_tags", tagDAO.getTopLevelTags());
        mv.addObject("heading", "Editing a Tag");
        
        Tag tag = null;
        requestFilter.loadAttributesOntoRequest(request);
           
        if (request.getAttribute("tag") != null) {
            tag = (Tag) request.getAttribute("tag");         
            mv.addObject("tag", tag);
            
            List<Resource> taggedResources = resourceDAO.getResourcesWithTag(tag);
            log.info("Tag to be deleted has " + taggedResources.size() + " resources.");
            for (Resource resource : taggedResources) {            	
            	log.info("Removing tag from: " + resource.getName());
                resource.getRemoveTag(tag);
                resourceDAO.saveResource(resource);
            }
                    
            if (tag.getParent() != null) {
                tag.getParent().getChildren().remove(tag);
            }
            log.info("Deleting tag " + tag.getName());
            tagDAO.deleteTag(tag);
            
            urlStack.setUrlStack(request, "/index");
        }              
        return mv;
    }
    
    

    @Transactional(propagation=Propagation.REQUIRED)
    public ModelAndView save(HttpServletRequest request, HttpServletResponse response) {
        
        ModelAndView modelAndView = new ModelAndView("savedTag");    
        modelAndView.addObject("heading", "Tag Saved");

        Tag editTag = null;
        requestFilter.loadAttributesOntoRequest(request);

        if (request.getAttribute("tag") != null) {
            editTag = (Tag) request.getAttribute("tag");
            log.info("Found tag " + editTag.getName() + " on request.");
        } else {
            log.info("No tag seen on request; creating a new instance.");
            editTag = resourceDAO.createNewTag();
        }
        
        editTag.setName(request.getParameter("name"));
        editTag.setDisplayName(request.getParameter("displayName"));
        
        populateRelatedTwitter(request, editTag);        
        populateAutotagHints(request, editTag);
                
        Feed relatedFeed = null;      
        if (request.getAttribute("feedAttribute") != null) {
            relatedFeed = (Feed) request.getAttribute("feedAttribute");            
        }
        log.info("Setting related feed to: " + relatedFeed);
        editTag.setRelatedFeed(relatedFeed);
        
        readImageFieldFromRequest(editTag, request);
             
        final Tag parentTag = (Tag) request.getAttribute("parent_tag");      
        if (parentTag != null) {
            final boolean newParentIsOneOfOurChildren = editTag.getChildren().contains(parentTag);
            if (!newParentIsOneOfOurChildren) {
                log.info("Setting parent tag to: " + parentTag.getName()); 
                editTag.setParent(parentTag);
            } else {
                log.warn("Not setting parent to one of our current children; this would be a circular reference");
            }
        } else {
            log.info("Making top level tag; setting parent to null.");
            editTag.setParent(null);
        }
        
        
        // TODO validate.
        tagDAO.saveTag(editTag);
        
        modelAndView.addObject("tag", editTag);
        modelAndView.addObject("top_level_tags", tagDAO.getTopLevelTags());    
        return modelAndView;
    }


	private void populateAutotagHints(HttpServletRequest request, Tag editTag) {
		final String autotagHints = request.getParameter("autotag_hints");
        if (autotagHints != null && !autotagHints.trim().equals("")) {
        	editTag.setAutotagHints(autotagHints);
        } else {	
        	editTag.setAutotagHints(null);
        }
	}


	private void populateRelatedTwitter(HttpServletRequest request, Tag editTag) {
		final String requestTwitter = request.getParameter("twitter");
        if (requestTwitter != null && !requestTwitter.trim().equals("")) {
        	editTag.setRelatedTwitter(requestTwitter);        	
        } else {
        	editTag.setRelatedTwitter(null);
        }
	}
    
    
    private void readImageFieldFromRequest(Tag editTag, HttpServletRequest request) {
        String mainImage = request.getParameter("main_image");
        String secondaryImage = request.getParameter("secondary_image");
        if (mainImage != null && mainImage.trim().equals("")) {
            mainImage = null;
        }
        if (secondaryImage != null && secondaryImage.trim().equals("")) {
            secondaryImage = null;
        }                
        editTag.setMainImage(mainImage);
        editTag.setSecondaryImage(secondaryImage);
    }
    
}
