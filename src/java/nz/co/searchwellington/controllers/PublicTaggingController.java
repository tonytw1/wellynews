package nz.co.searchwellington.controllers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.filters.RequestFilter;
import nz.co.searchwellington.mail.Notifier;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.FeedRepository;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.widgets.TagWidgetFactory;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;


/**
 * Allows the great unwashed to tag previously untagged items.
 * 
 * @author tony
 *
 */
public class PublicTaggingController extends BaseTagEditingController {

        
    Logger log = Logger.getLogger(PublicTaggingController.class);
    
    
    private RequestFilter requestFilter;    
    private TagWidgetFactory tagWidgetFactory;
    private Notifier notifier;

    
    public PublicTaggingController(ResourceRepository resourceDAO, FeedRepository feedDAO, 
                RequestFilter requestFilter, ItemMaker itemMaker, 
                TagWidgetFactory tagWidgetFactory, Notifier notifier) {       
        this.resourceDAO = resourceDAO;      
        this.requestFilter = requestFilter;
        this.itemMaker = itemMaker;
        this.tagWidgetFactory = tagWidgetFactory;
        this.notifier = notifier;
    }
   
    
        
    @SuppressWarnings("unchecked")
    public ModelAndView edit(HttpServletRequest request, HttpServletResponse response) throws IOException {

        ModelAndView mv = new ModelAndView("publicTagging");    
        mv.getModel().put("top_level_tags", resourceDAO.getTopLevelTags());
        mv.getModel().put("heading", "Tagging a Resource");
        
        User loggedInUser = setLoginState(request, mv);
           
        Resource editResource = null;
        requestFilter.loadAttributesOntoRequest(request);
           
        if (request.getAttribute("resource") != null) {
            editResource = (Resource) request.getAttribute("resource");        
            log.info("Loaded resource #" + editResource.getId() + " for tagging.");
            mv.getModel().put("resource", editResource);
            mv.getModel().put("tag_select", tagWidgetFactory.createMultipleTagSelect(editResource.getTags()));
            
            if (loggedInUser != null) {
                mv.getModel().put("show_additional_tags", 1);
            }
            
        } else {
            log.info("No resource was loaded from the request.");
        }
             
        return mv;
    }


    
    
    
    @SuppressWarnings("unchecked")
    public ModelAndView save(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
                
        ModelAndView modelAndView = new ModelAndView("publicTagged");   
        modelAndView.getModel().put("heading", "Resource Saved");
        modelAndView.getModel().put("top_level_tags", resourceDAO.getTopLevelTags());        
        
        User loggedInUser = setLoginState(request, modelAndView);
        
        Resource editResource = null;
        requestFilter.loadAttributesOntoRequest(request);   
        
        
        if (request.getAttribute("resource") != null) {         
            editResource = (Resource) request.getAttribute("resource");
                        
            processTags(request, editResource, loggedInUser);
            resourceDAO.saveResource(editResource);
           
            notifier.sendTaggingNotification("tony@ditonics.com", "Public Tagging", editResource);
            
            modelAndView.getModel().put("resource", editResource);    
        }
        
        return modelAndView;
    }



    

}
