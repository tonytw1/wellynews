package nz.co.searchwellington.controllers.admin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.CommonModelObjectsService;
import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.controllers.SubmissionProcessingService;
import nz.co.searchwellington.controllers.UrlStack;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.UrlWordsGenerator;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.modification.TagModificationService;
import nz.co.searchwellington.repositories.TagDAO;
import nz.co.searchwellington.widgets.TagsWidgetFactory;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class TagEditController {
    
    private static Logger log = Logger.getLogger(TagEditController.class);
    
    private AdminRequestFilter requestFilter;
    private TagsWidgetFactory tagWidgetFactory;
    private UrlStack urlStack;
    private TagDAO tagDAO;
    private TagModificationService tagModifcationService;
	private LoggedInUserFilter loggedInUserFilter;
    private EditPermissionService editPermissionService;
    private SubmissionProcessingService submissionProcessingService;
    private CommonModelObjectsService commonModelObjectsService;
	private UrlWordsGenerator urlWordsGenerator;
    
    public TagEditController() {       
    }
    
	@Autowired
	public TagEditController(AdminRequestFilter requestFilter,
			TagsWidgetFactory tagWidgetFactory, UrlStack urlStack,
			TagDAO tagDAO, TagModificationService tagModifcationService,
			LoggedInUserFilter loggedInUserFilter,
			EditPermissionService editPermissionService,
			SubmissionProcessingService submissionProcessingService,
			CommonModelObjectsService commonModelObjectsService,
			UrlWordsGenerator urlWordsGenerator) {
		this.requestFilter = requestFilter;
		this.tagWidgetFactory = tagWidgetFactory;
		this.urlStack = urlStack;
		this.tagDAO = tagDAO;
		this.tagModifcationService = tagModifcationService;
		this.loggedInUserFilter = loggedInUserFilter;
		this.editPermissionService = editPermissionService;
		this.submissionProcessingService = submissionProcessingService;
		this.commonModelObjectsService = commonModelObjectsService;
		this.urlWordsGenerator = urlWordsGenerator;
	}
    
	@Transactional	// TODO required? read only?
	@RequestMapping("/edit/tag/submit")
    public ModelAndView submit(HttpServletRequest request, HttpServletResponse response) {
		final ModelAndView mv = new ModelAndView("submitTag");
        mv.addObject("heading", "Submitting a Tag");
        commonModelObjectsService.populateCommonLocal(mv);
        return mv;
    }
	
    @Transactional
    @RequestMapping("/edit/tag/*")
    public ModelAndView edit(HttpServletRequest request, HttpServletResponse response) {        
       final  ModelAndView mv = new ModelAndView("editTag");   
        commonModelObjectsService.populateCommonLocal(mv);
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
    @RequestMapping("/edit/tag/delete")			
    public ModelAndView delete(HttpServletRequest request, HttpServletResponse response) {	
    	User loggedInUser = loggedInUserFilter.getLoggedInUser();
    	if (!editPermissionService.canDeleteTags(loggedInUser)) {
    		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        	return null;
    	}
    	    	        
        requestFilter.loadAttributesOntoRequest(request);           
        if (request.getAttribute("tag") == null) {
        	response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        	return null;
        }
        
        final ModelAndView mv = new ModelAndView("deleteTag"); 
        mv.addObject("heading", "Editing a Tag");
        commonModelObjectsService.populateCommonLocal(mv);
        
        Tag tag = (Tag) request.getAttribute("tag");         
        mv.addObject("tag", tag);            
        tagModifcationService.deleteTag(tag);            
        urlStack.setUrlStack(request, "");                     
        return mv;
    }
    
	@RequestMapping("/edit/tag/add")
    public ModelAndView add(HttpServletRequest request, HttpServletResponse response) {
    	  ModelAndView modelAndView = new ModelAndView("savedTag");    
          modelAndView.addObject("heading", "Tag Added");
          
          final String displayName = request.getParameter("displayName");
          if (displayName != null) {        	  
        	  String tagUrlWords = urlWordsGenerator.makeUrlWordsFromName(displayName);
        	  if (tagDAO.loadTagByName(tagUrlWords) == null) {
        		  Tag newTag = tagDAO.createNewTag(tagUrlWords, displayName);        		  
        		  log.info("Adding new tag: " + tagUrlWords);
        		  tagDAO.saveTag(newTag);
        		  modelAndView.addObject("tag", newTag);
                  return modelAndView;

        	  } else {
        		  log.info("A tag already exists with url words: " + tagUrlWords + ". Not adding.");
        	  }
          }          
          return new ModelAndView(new RedirectView(urlStack.getExitUrlFromStack(request)));
	}
	
    @Transactional
	@RequestMapping(value="/edit/tag/save", method=RequestMethod.POST)	// TODO use redirect view
    public ModelAndView save(HttpServletRequest request, HttpServletResponse response) {        
        final ModelAndView mv = new ModelAndView("savedTag");    
        mv.addObject("heading", "Tag Saved");
        commonModelObjectsService.populateCommonLocal(mv);

        Tag editTag = null;
        requestFilter.loadAttributesOntoRequest(request);

        if (request.getAttribute("tag") != null) {
            editTag = (Tag) request.getAttribute("tag");
            log.info("Found tag " + editTag.getName() + " on request.");
        } else {
            log.info("No tag seen on request; creating a new instance.");
            editTag = tagDAO.createNewTag();
        }
        
        editTag.setName(request.getParameter("name"));
        editTag.setDisplayName(request.getParameter("displayName"));
        editTag.setDescription(request.getParameter("description"));
        final boolean isFeatured = request.getParameter("featured") != null;
        editTag.setFeatured(isFeatured);
        
        editTag.setGeocode(submissionProcessingService.processGeocode(request));
        
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
        	boolean parentTagHasChanged = parentTag != editTag.getParent();
        	if (parentTagHasChanged) {
        		final boolean newParentIsOneOfOurChildren = editTag.getChildren().contains(parentTag);
        		if (!newParentIsOneOfOurChildren) {
        			tagModifcationService.updateTagParent(editTag, parentTag);        			
        		} else {
        			log.warn("Not setting parent to one of our current children; this would be a circular reference");
        		}
        	}
        	
        } else {
        	log.info("Making top level tag; setting parent to null.");
        	editTag.setParent(null);
        }
               
        // TODO validate.
        tagDAO.saveTag(editTag);	// TODO should go through the TMS surely?
        
        mv.addObject("tag", editTag);
        commonModelObjectsService.populateCommonLocal(mv);
        return mv;
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
