package nz.co.searchwellington.controllers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.mail.Notifier;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.widgets.TagWidgetFactory;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;


public class PublicTaggingController extends BaseMultiActionController {
        
    Logger log = Logger.getLogger(PublicTaggingController.class);
        
    private TagWidgetFactory tagWidgetFactory;
    private Notifier notifier;
	private SubmissionProcessingService submissionProcessingService;

    
    public PublicTaggingController(ResourceRepository resourceDAO,           
                TagWidgetFactory tagWidgetFactory, Notifier notifier, LoggedInUserFilter loggedInUserFilter, SubmissionProcessingService submissionProcessingService) {       
        this.resourceDAO = resourceDAO;      
        this.tagWidgetFactory = tagWidgetFactory;
        this.notifier = notifier;
        this.loggedInUserFilter = loggedInUserFilter;
        this.submissionProcessingService = submissionProcessingService;
    }
   
    
    
    public ModelAndView edit(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView("publicTagging");    
        mv.addObject("top_level_tags", resourceDAO.getTopLevelTags());
        mv.addObject("heading", "Tagging a Resource");
        
        User loggedInUser = loggedInUserFilter.getLoggedInUser();
           
        Resource editResource = null;
        
        if (request.getAttribute("resource") != null) {
            editResource = (Resource) request.getAttribute("resource");        
            log.info("Loaded resource #" + editResource.getId() + " for tagging.");
            mv.addObject("resource", editResource);
            mv.addObject("tag_select", tagWidgetFactory.createMultipleTagSelect(editResource.getTags()));
            
            if (loggedInUser != null) {
                mv.addObject("show_additional_tags", 1);
            }
            
        } else {
            log.info("No resource was loaded from the request.");
        }
             
        return mv;
    }


    
    
      
    public ModelAndView save(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {                
        ModelAndView modelAndView = new ModelAndView("publicTagged");   
        modelAndView.addObject("heading", "Resource Saved");
        modelAndView.addObject("top_level_tags", resourceDAO.getTopLevelTags());        
        
        User loggedInUser = loggedInUserFilter.getLoggedInUser();        
        Resource editResource = null;
        
        if (request.getAttribute("resource") != null) {         
            editResource = (Resource) request.getAttribute("resource");
            submissionProcessingService.processTags(request, editResource, loggedInUser);
            resourceDAO.saveResource(editResource);           
            notifier.sendTaggingNotification("tony@ditonics.com", "Public Tagging", editResource);            
            modelAndView.addObject("resource", editResource);    
        }
        
        return modelAndView;
    }



    

}
