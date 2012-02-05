package nz.co.searchwellington.controllers.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.controllers.SubmissionProcessingService;
import nz.co.searchwellington.controllers.admin.AdminRequestFilter;
import nz.co.searchwellington.feeds.FeedItemAcceptor;
import nz.co.searchwellington.feeds.RssfeedNewsitemService;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.PublishedResource;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.modification.ContentUpdateService;
import nz.co.searchwellington.repositories.HandTaggingDAO;
import nz.co.searchwellington.repositories.ResourceFactory;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.SupressionService;
import nz.co.searchwellington.tagging.AutoTaggingService;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import com.sun.syndication.io.FeedException;

public class ApiController extends MultiActionController {

	private static Logger log = Logger.getLogger(ApiController.class);
	
	private final ResourceRepository resourceDAO;
	private final AdminRequestFilter requestFilter;
	private final LoggedInUserFilter loggedInUserFilter;
	private final SupressionService suppressionService;
	private final RssfeedNewsitemService rssfeedNewsitemService;
	private final ContentUpdateService contentUpdateService;
	private final SubmissionProcessingService submissionProcessingService;
	private final AutoTaggingService autoTagger;
	private final HandTaggingDAO tagVoteDAO;
	private final ResourceFactory resourceFactory;
	private final FeedItemAcceptor feedItemAcceptor;
	
    public ApiController(ResourceRepository resourceDAO, AdminRequestFilter requestFilter, LoggedInUserFilter loggedInUserFilter, SupressionService suppressionService, RssfeedNewsitemService rssfeedNewsitemService, ContentUpdateService contentUpdateService, SubmissionProcessingService submissionProcessingService, AutoTaggingService autoTagger, HandTaggingDAO tagVoteDAO, FeedItemAcceptor feedItemAcceptor, ResourceFactory resourceFactory) {
		this.resourceDAO = resourceDAO;
		this.requestFilter = requestFilter;
		this.loggedInUserFilter = loggedInUserFilter;
		this.suppressionService = suppressionService;
		this.rssfeedNewsitemService = rssfeedNewsitemService;
		this.contentUpdateService = contentUpdateService;
		this.submissionProcessingService = submissionProcessingService;
		this.autoTagger = autoTagger;
		this.tagVoteDAO = tagVoteDAO;
		this.feedItemAcceptor = feedItemAcceptor;
		this.resourceFactory = resourceFactory;
	}
    
    @Transactional
    public ModelAndView submit(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
    	ModelAndView mv = new ModelAndView();
    	request.setCharacterEncoding("UTF-8");
    	
        User loggedInUser = loggedInUserFilter.getLoggedInUser();
        if (isAuthorised(loggedInUser)) {
        	log.info("Accepting newsitem submission from api call by user: " + loggedInUser.getName());
	        Resource resource = resourceFactory.createNewNewsitem();
	        
	        submissionProcessingService.processUrl(request, resource);	         
	    	submissionProcessingService.processTitle(request, resource);
	    	log.info("Calling geocode");
	    	resource.setGeocode(submissionProcessingService.processGeocode(request));
	    	submissionProcessingService.processDate(request, resource);
	    	submissionProcessingService.processDescription(request, resource);
	    	submissionProcessingService.processTags(request, resource, loggedInUser);
	    	submissionProcessingService.processPublisher(request, resource);
	    	
	    	if (resource.getType().equals("N")) {
	    		submissionProcessingService.processImage(request, (Newsitem) resource, loggedInUser);            
	    	}
	         
	    	// Set publisher field.
	    	boolean isPublishedResource = resource instanceof PublishedResource;
	    	if (isPublishedResource) {
	    		((PublishedResource) resource).setPublisher((Website) request.getAttribute("publisher"));           
	    	}
	    	
	    	log.info("Saving api submitted newsitem: " + resource.getName());
	    	contentUpdateService.update(resource);
	    	log.info("Id after save is: " + resource.getId());
	    	mv.setViewName("apiResponseOK");
	    	
        } else {
        	response.setStatus(HttpStatus.SC_FORBIDDEN);
        }
        mv.setViewName("apiResponseERROR");
    	return mv;      
    }
    
    @Transactional
    public ModelAndView accept(HttpServletRequest request, HttpServletResponse response) throws IllegalArgumentException, FeedException, IOException {
    	ModelAndView mv = new ModelAndView();

    	User loggedInUser = loggedInUserFilter.getLoggedInUser();
    	if (isAuthorised(loggedInUser)) { 
    		if (request.getParameter("url") != null) {    			
    			final String url = request.getParameter("url");
    			log.info("Attempting to accept feed item with url: " + url);
    			Newsitem newsitemToAccept = rssfeedNewsitemService.getFeedNewsitemByUrl(url);
    			if (newsitemToAccept != null) {
    				feedItemAcceptor.acceptFeedItem(loggedInUser, newsitemToAccept);
    				autoTagger.autotag(newsitemToAccept);   // TODO in the wrong place - should be behind the content update service?
    				contentUpdateService.update(newsitemToAccept);
    			}
    		}
    	}
    	
 		mv.setViewName("apiResponseERROR");
		return mv;
    }
    
    @Transactional
    public ModelAndView changeUrl(HttpServletRequest request, HttpServletResponse response) throws IOException {    		
    	 ModelAndView mv = new ModelAndView();
         User loggedInUser = loggedInUserFilter.getLoggedInUser();
         if (isAuthorised(loggedInUser)) {                
         	requestFilter.loadAttributesOntoRequest(request);        
         	final String resourceUrl = request.getParameter("url");
         	final String newUrl = (String) request.getAttribute("newurl");
         	
         	if (resourceUrl != null && newUrl != null) {
         		Resource resource = resourceDAO.loadResourceByUniqueUrl(resourceUrl);
         		if (resource != null) {
         			log.info("Changed url of resource '" + resource.getName() + " from '" + resourceUrl + "'to resource: " + resource.getUrl());
         			contentUpdateService.update(resource);
         			mv.setViewName("apiResponseOK");
         			return mv;
         			
         		} else {
         			log.info("No unique resource found for url: " + resourceUrl);
         		}
         	} else {
         		log.info("No resource url or valid new url found");
         	}
         } else {
        	 response.setStatus(HttpStatus.SC_FORBIDDEN);
         }
         
 		mv.setViewName("apiResponseERROR");
 		return mv;    	
    }
    
    @Transactional
    public ModelAndView suppress(HttpServletRequest request, HttpServletResponse response) {
    	ModelAndView mv = new ModelAndView();	
    	User loggedInUser = loggedInUserFilter.getLoggedInUser();
    	if (isAuthorised(loggedInUser)) {                
    		final String urlToSupress = request.getParameter("url");    	    		
    		if (urlToSupress != null) {    			 
    			suppressionService.suppressUrl(urlToSupress);
    			mv.setViewName("apiResponseOK");
    			return mv;
    		}
    		
    	} else {
    		response.setStatus(HttpStatus.SC_FORBIDDEN);
        }    	
    	mv.setViewName("apiResponseERROR");
    	return mv;    
	}
    
    @Transactional
    public ModelAndView tag(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        User loggedInUser = loggedInUserFilter.getLoggedInUser();
        if (isAuthorised(loggedInUser)) {                
        	requestFilter.loadAttributesOntoRequest(request);        
        	final String resourceUrl = request.getParameter("url");
        	final Tag tag = (Tag) request.getAttribute("tag");
        	
        	if (resourceUrl != null && tag != null) {
        		Resource resource = resourceDAO.loadResourceByUniqueUrl(resourceUrl);
        		if (resource != null) {
        			tagVoteDAO.addTag(loggedInUser, tag, resource);
        			log.info("Applied tag: " + tag.getDisplayName() + " to resource: " + resource.getName());
        			contentUpdateService.update(resource);
        			mv.setViewName("apiResponseOK");
        			return mv;
        			
        		} else {
        			log.info("No unique resource found for url: " + resourceUrl);
        		}
        	} else {
        		log.info("No resource url or valid tag found");
        	}
        } else {
        	response.setStatus(HttpStatus.SC_FORBIDDEN);
        }
        
		mv.setViewName("apiResponseERROR");
        return mv; 		
    }
    
    private boolean isAuthorised(User loggedInUser) {	// TODO wants to delegate to the permissions service.
    	if (loggedInUser != null) {
    		log.info("API call user is " + loggedInUser.getName());
    	} else {
    		log.info("API call user is null");
    	}
    	boolean isAuthorised = loggedInUser != null && loggedInUser.isAdmin();
    	log.info("User is authorised: " + isAuthorised);
    	return isAuthorised;
    }

}
