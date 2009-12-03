package nz.co.searchwellington.controllers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.admin.AdminRequestFilter;
import nz.co.searchwellington.controllers.admin.EditPermissionService;
import nz.co.searchwellington.feeds.RssfeedNewsitemService;
import nz.co.searchwellington.feeds.rss.RssNewsitemPrefetcher;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedNewsitem;
import nz.co.searchwellington.model.Image;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.PublishedResource;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Supression;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.TwitteredNewsitem;
import nz.co.searchwellington.model.UrlWordsGenerator;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.Watchlist;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.SuggestionDAO;
import nz.co.searchwellington.repositories.SupressionRepository;
import nz.co.searchwellington.spam.SpamFilter;
import nz.co.searchwellington.tagging.AutoTaggingService;
import nz.co.searchwellington.twitter.TwitterNewsitemBuilderService;
import nz.co.searchwellington.utils.UrlCleaner;
import nz.co.searchwellington.widgets.AcceptanceWidgetFactory;
import nz.co.searchwellington.widgets.PublisherSelectFactory;
import nz.co.searchwellington.widgets.TagWidgetFactory;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.sun.syndication.io.FeedException;


public class ResourceEditController extends BaseMultiActionController {
    
    Logger log = Logger.getLogger(ResourceEditController.class);
           
    private RssfeedNewsitemService rssfeedNewsitemService;
    private AdminRequestFilter adminRequestFilter;    
    private TagWidgetFactory tagWidgetFactory;
    private PublisherSelectFactory publisherSelectFactory;
    private SupressionRepository supressionDAO;
    private AutoTaggingService autoTagger;
    private AcceptanceWidgetFactory acceptanceWidgetFactory;
    private UrlCleaner urlCleaner;
    private RssNewsitemPrefetcher rssPrefetcher;
    private EditPermissionService editPermissionService;
    private TwitterNewsitemBuilderService twitterNewsitemBuilderService;
    private SuggestionDAO suggestionDAO;
    private SubmissionProcessingService submissionProcessingService;
    private ContentUpdateService contentUpdateService;
      
    public ResourceEditController(ResourceRepository resourceDAO, RssfeedNewsitemService rssfeedNewsitemService, AdminRequestFilter adminRequestFilter,
            TagWidgetFactory tagWidgetFactory, PublisherSelectFactory publisherSelectFactory, SupressionRepository supressionDAO,
            AutoTaggingService autoTagger, AcceptanceWidgetFactory acceptanceWidgetFactory,
            UrlCleaner urlCleaner, RssNewsitemPrefetcher rssPrefetcher, LoggedInUserFilter loggedInUserFilter, 
            EditPermissionService editPermissionService, UrlStack urlStack, TwitterNewsitemBuilderService twitterNewsitemBuilderService, 
            SuggestionDAO suggestionDAO, SubmissionProcessingService submissionProcessingService, ContentUpdateService contentUpdateService) {    	
        this.resourceDAO = resourceDAO;
        this.rssfeedNewsitemService = rssfeedNewsitemService;        
        this.adminRequestFilter = adminRequestFilter;       
        this.tagWidgetFactory = tagWidgetFactory;
        this.publisherSelectFactory = publisherSelectFactory;
        this.supressionDAO = supressionDAO;
        this.autoTagger = autoTagger;
        this.acceptanceWidgetFactory = acceptanceWidgetFactory;      
        this.urlCleaner = urlCleaner;
        this.rssPrefetcher = rssPrefetcher;
        this.loggedInUserFilter = loggedInUserFilter;
        this.editPermissionService = editPermissionService;
        this.urlStack = urlStack;
        this.twitterNewsitemBuilderService = twitterNewsitemBuilderService;
        this.suggestionDAO = suggestionDAO;
        this.submissionProcessingService = submissionProcessingService;
        this.contentUpdateService = contentUpdateService;
    }
   
    
       
    @Transactional
    public ModelAndView edit(HttpServletRequest request, HttpServletResponse response) throws IOException {    	
    	adminRequestFilter.loadAttributesOntoRequest(request);    	
    	User loggedInUser = loggedInUserFilter.getLoggedInUser();
    	
    	Resource editResource = (Resource) request.getAttribute("resource");    	
    	if (request.getAttribute("resource") != null && userIsAllowedToEdit(editResource, request, loggedInUser)) {    		
    		ModelAndView mv = new ModelAndView("editResource");
    		populateCommonLocal(mv);
    		mv.addObject("heading", "Editing a Resource");
    		
            mv.addObject("resource", editResource);
            mv.addObject("tag_select", tagWidgetFactory.createMultipleTagSelect(editResource.getTags()));
            mv.addObject("show_additional_tags", 1);
            
            boolean userIsLoggedIn = loggedInUser != null;
            populatePublisherField(mv, userIsLoggedIn, editResource);
                
            if (editResource.getType().equals("F")) {            
            	mv.addObject("acceptance_select", acceptanceWidgetFactory.createAcceptanceSelect (((Feed)editResource).getAcceptancePolicy()));                   
            }                
            return mv;
        }
       
    	return new ModelAndView(new RedirectView(urlStack.getExitUrlFromStack(request)));   	
    }


    @Transactional
    public ModelAndView accept(HttpServletRequest request, HttpServletResponse response) throws IllegalArgumentException, FeedException, IOException {        
        ModelAndView modelAndView = new ModelAndView("acceptResource");       
        populateCommonLocal(modelAndView);
        modelAndView.addObject("heading", "Accepting a submission");
        
        User loggedInUser = loggedInUserFilter.getLoggedInUser();
        boolean userIsLoggedIn = loggedInUser != null;
        
        adminRequestFilter.loadAttributesOntoRequest(request);
        
        Newsitem newsitem = null;
        if (request.getParameter("item") != null) {
        	int item = (Integer) request.getAttribute("item");    	  
        	if (request.getAttribute("feedAttribute") != null) {
        		newsitem = getRequestedFeedItemByFeedAndItemNumber(request, newsitem, item);
        	}
      
        } else if (request.getParameter("url") != null) {
        	newsitem = getRequestedFeedItemByUrl(request.getParameter("url"));        	
        }
        
        if (newsitem != null) {        	      
        	boolean newsitemHasNoDate = (newsitem.getDate() == null);
            if (newsitemHasNoDate) {
            	final Date today = Calendar.getInstance().getTime();
            	newsitem.setDate(today);
            }
                    
            modelAndView.addObject("resource", newsitem); 
            modelAndView.addObject("publisher_select",
            		publisherSelectFactory.createPublisherSelectWithNoCounts(newsitem.getPublisher(), 
            		userIsLoggedIn).toString()); // TODO select still in use? should be the autocomplete field now.
            modelAndView.addObject("tag_select", tagWidgetFactory.createMultipleTagSelect(new HashSet<Tag>()));
        }
        
        populateSecondaryFeeds(modelAndView, loggedInUser);
        return modelAndView;
    }



	private Newsitem getRequestedFeedItemByFeedAndItemNumber(
			HttpServletRequest request, Newsitem newsitem, int item) {
		log.info("Looking for feeditem by feed and item number: " + item);
		Feed feed = (Feed) request.getAttribute("feedAttribute");   	     	  
		
		List <FeedNewsitem> feednewsItems = rssfeedNewsitemService.getFeedNewsitems(feed);
		if (item > 0 && item <= feednewsItems.size()) {                    
			FeedNewsitem feednewsitem = feednewsItems.get(item-1);
			newsitem = rssfeedNewsitemService.makeNewsitemFromFeedItem(feednewsitem, feed);
		}
		return newsitem;
	}
    
    
    
    private Newsitem getRequestedFeedItemByUrl(String url) {
    	return rssfeedNewsitemService.getFeedNewsitemByUrl(url);    	
    }
    
    
    
    @Transactional
    public ModelAndView twitteraccept(HttpServletRequest request, HttpServletResponse response) throws IllegalArgumentException, FeedException, IOException {
        ModelAndView modelAndView = new ModelAndView("acceptResource");       
        
        adminRequestFilter.loadAttributesOntoRequest(request);
        if (request.getAttribute("twitterId") != null) {        
            Long twitterId = (Long) request.getAttribute("twitterId");
            TwitteredNewsitem twittedNewsitem = twitterNewsitemBuilderService.getPossibleSubmissionByTwitterId(twitterId);
            
            if (twittedNewsitem != null) {
            	log.info("Attempting to accept newsitem from twitter id: " + twitterId);
            	final Newsitem newsitem = twitterNewsitemBuilderService.makeNewsitemFromTwitteredNewsitem(twittedNewsitem);
            	
            	if (newsitem != null) {            		
            		modelAndView.addObject("resource", newsitem);
            		
            	} else {
            		log.info("Could not extract a newsitem from this twit");            		
            	}
            	
            } else {
            	log.warn("Could not find twitter with id: " + twitterId);            	
            }
            
        } else {
        	log.warn("No twitted id found on request");
        }
        
		return modelAndView;
    }




    
	private void populateSpamQuestion(HttpServletRequest request, ModelAndView modelAndView) {
        User loggedInUser = loggedInUserFilter.getLoggedInUser();
        if (loggedInUser == null) {
            modelAndView.addObject("spam_question", "The capital of New Zealand is (10 letters)");
        }
    }
    
    
  
    
    
    
    @Transactional
    public ModelAndView submitWebsite(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
        ModelAndView modelAndView = new ModelAndView("submitWebsite");
        modelAndView.addObject("heading", "Submitting a Website");        
        Resource editResource = resourceDAO.createNewWebsite();
        modelAndView.addObject("resource", editResource);
       
        populateSubmitCommonElements(request, modelAndView);
        
        populateSpamQuestion(request, modelAndView);
        modelAndView.addObject("publisher_select", null);
        return modelAndView;
    }



   
    
    @Transactional
    public ModelAndView submitNewsitem(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView modelAndView = new ModelAndView("submitNewsitem");
        modelAndView.addObject("heading", "Submitting a Newsitem");
        Resource editResource = resourceDAO.createNewNewsitem();
        modelAndView.addObject("resource", editResource);
        
        populateSubmitCommonElements(request, modelAndView);         
       
        populateSpamQuestion(request, modelAndView);
        
        return modelAndView;
    }



    @Transactional
    public ModelAndView submitCalendar(HttpServletRequest request, HttpServletResponse response) throws IOException {        
        ModelAndView modelAndView = new ModelAndView("submitCalendar");
        modelAndView.addObject("heading", "Submitting a Calendar");
        Resource editResource = resourceDAO.createNewCalendarFeed("");
        modelAndView.addObject("resource", editResource);
       
        populateSpamQuestion(request, modelAndView);
        populateSubmitCommonElements(request, modelAndView);        
        return modelAndView;
    }
    
    
    
    @Transactional
    public ModelAndView submitFeed(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView modelAndView = new ModelAndView("submitFeed");
        modelAndView.addObject("heading", "Submitting a Feed");
        Resource editResource = resourceDAO.createNewFeed();
        modelAndView.addObject("resource", editResource);
        modelAndView.addObject("acceptance_select", acceptanceWidgetFactory.createAcceptanceSelect(null));
        
        populateSubmitCommonElements(request, modelAndView);
        
        return modelAndView;
    }
    
    
    @Transactional
    public ModelAndView submitWatchlist(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView modelAndView = new ModelAndView("submitWatchlist");
        modelAndView.addObject("heading", "Submitting a Watchlist Item");
        Resource editResource = resourceDAO.createNewWebsite();
        modelAndView.addObject("resource", editResource);
        
        populateSubmitCommonElements(request, modelAndView);
        
        return modelAndView;
    }
   
    
    
    @Transactional
    public ModelAndView delete(HttpServletRequest request, HttpServletResponse response) throws IOException {    
        ModelAndView modelAndView = new ModelAndView("deletedResource");
        populateCommonLocal(modelAndView);
        modelAndView.addObject("heading", "Resource Deleted");
        
        adminRequestFilter.loadAttributesOntoRequest(request);    
        Resource editResource = (Resource) request.getAttribute("resource");    
        User loggedInUser = loggedInUserFilter.getLoggedInUser();
        if (editResource != null && loggedInUser != null && loggedInUser.isAdmin()) {
            modelAndView.addObject("resource", editResource);
            editResource = (Resource) request.getAttribute("resource");
            
            if (editResource.getType().equals("W")) {
            	removePublisherFromPublishersContent(editResource);            	
            }
            
            if (editResource.getType().equals("F")) {
            	removeFeedFromFeedNewsitems((Feed) editResource);
            }
            
            if (editResource.getType().equals("N")) {
            	Newsitem deletedNewsitem = (Newsitem) editResource;
            	if (deletedNewsitem.getFeed() != null) {            		
            		if (rssfeedNewsitemService.getFeedNewsitemByUrl(deletedNewsitem.getFeed(), deletedNewsitem.getUrl()) != null) {
            			log.info("Deleting a newsitem whose url still appears in a feed; suppressing the url: " + deletedNewsitem.getUrl());
            			supressionDAO.createSupression(deletedNewsitem.getUrl());
            		}
            	}           		            	
            }
            
            resourceDAO.deleteResource(editResource);
        }
        // TODO need to given failure message if we didn't actually remove the item.
        return modelAndView;
    }



    private void removePublisherFromPublishersContent(Resource editResource) {
    	Website publisher = (Website) editResource;
    	for (Newsitem newsitem : publisher.getNewsitems()) {
    		newsitem.setPublisher(null);
    		resourceDAO.saveResource(newsitem);					
    	}
    	for (Feed feed : publisher.getFeeds()) {
    		feed.setPublisher(null);
    		resourceDAO.saveResource(feed);					
    	}
    	for (Watchlist watchlist : publisher.getWatchlist()) {
    		watchlist.setPublisher(null);
    		resourceDAO.saveResource(watchlist);					
    	}
    }
    
    
    
    private void removeFeedFromFeedNewsitems(Feed feed) {    	
    	for (Newsitem newsitem : resourceDAO.getNewsitemsForFeed(feed)) {
    		newsitem.setFeed(null);
    		resourceDAO.saveResource(newsitem);					
    	}    	
    }
    
    
    @Transactional
    public ModelAndView deleteAndSupress(HttpServletRequest request, HttpServletResponse response) throws IOException {   
        ModelAndView modelAndView = new ModelAndView("deletedResource");
        populateCommonLocal(modelAndView);
        modelAndView.addObject("heading", "Resource Deleted");
        
        adminRequestFilter.loadAttributesOntoRequest(request);    
        Resource editResource = (Resource) request.getAttribute("resource");       
        if (editResource != null) {             
            modelAndView.addObject("resource", editResource);
            
            String urlToSupress = urlCleaner.cleanSubmittedItemUrl(editResource.getUrl());
            log.info("Deleting resource.");
            resourceDAO.deleteResource(editResource);
            
            if (!supressionDAO.isSupressed(urlToSupress)) {
                log.info("Supressing url: " + urlToSupress);               
                Supression supression = supressionDAO.createSupression(urlToSupress);
                supressionDAO.addSupression(supression);
            } else {
                log.info("Deleted url is already supressed.");
            }
        }
        return modelAndView;
    }
    
    
       
   @Transactional
    public ModelAndView save(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {       
	   	// TODO is this needed?
        request.setCharacterEncoding("UTF-8");                
        ModelAndView modelAndView = new ModelAndView("savedResource");
        populateCommonLocal(modelAndView);       
        modelAndView.addObject("heading", "Resource Saved");
        
        User loggedInUser = loggedInUserFilter.getLoggedInUser();
        
        Resource editResource = null;
        adminRequestFilter.loadAttributesOntoRequest(request);   
        
        
        if (request.getAttribute("resource") != null) {
            editResource = (Resource) request.getAttribute("resource");
            
        } else {
            log.info("Creating new resource.");
            if (request.getParameter("type") != null) {
                String type = request.getParameter("type");
                if (type.equals("W")) {
                    editResource = resourceDAO.createNewWebsite(); 
                } else if (type.equals("N")) {
                    editResource = resourceDAO.createNewNewsitem(); 
                } else if (type.equals("F")) {
                    editResource = resourceDAO.createNewFeed();                     
                } else if (type.equals("L")) {                    
                    editResource = resourceDAO.createNewWatchlist();                   
                } else if (type.equals("C")) {
                    editResource = resourceDAO.createNewCalendarFeed("");                   
                } else {
                    // TODO this should be a caught error.
                    editResource = resourceDAO.createNewWebsite();
                }
            }
            
            if (loggedInUser != null) {
            	log.info("New resource assigned to owner: " + loggedInUser.getUsername());
            	editResource.setOwner(loggedInUser);
            }
            
        }
       
        log.info("In save");
        if (editResource != null) {                      
            boolean newSubmission = editResource.getId() == 0;
                   
            boolean resourceUrlHasChanged = submissionProcessingService.processUrl(request, editResource);
            submissionProcessingService.processTitle(request, editResource);
            log.info("Calling geocode");
            submissionProcessingService.processGeocode(request, editResource);
            submissionProcessingService.processDate(request, editResource);
            submissionProcessingService.processDescription(request, editResource);
            submissionProcessingService.processTags(request, editResource);            
            submissionProcessingService.processPublisher(request, editResource);
            
            if (editResource.getType().equals("N")) {
            	processImage(request, (Newsitem) editResource, loggedInUser);            
            }
            
            
            processFeedAcceptancePolicy(request, editResource);
                        
            // Apply the auto tagger if this submission is by a logged in user.
            if (newSubmission && loggedInUser != null) {
                log.info("Applying the auto tagged to new submission.");
                autoTagger.autotag(editResource);
            }
            
            
            // Update urlwords.
            if (editResource.getType().equals("W") || editResource.getType().equals("F")) {
            	editResource.setUrlWords(UrlWordsGenerator.makeUrlWordsFromName(editResource.getName()));            	
            }
                        
            // New submissions from the public are spam filtered.
            boolean spamQuestionAnswered = false;
            if (newSubmission && loggedInUser == null) {
                String spamAnswer = request.getParameter("spam_question");
                log.info("Spam question answer was: " + spamAnswer);
                if (spamAnswer != null && spamAnswer.trim().toLowerCase().equals("wellington")) {
                    spamQuestionAnswered = true;
                    log.info("Spam question was answered correctly.");
                } else {
                    log.info("Spam question was answered incorrectly.");
                }
            }
            
            SpamFilter spamFilter = new SpamFilter();
            boolean isSpamUrl = spamFilter.isSpam(editResource);
         
            boolean okToSave = !newSubmission || (spamQuestionAnswered && !isSpamUrl) || loggedInUser != null;
            // TODO validate. - what exactly?
            if (okToSave) {            	
                saveResource(request, loggedInUser, editResource, newSubmission, resourceUrlHasChanged);
                
            } else {
                log.info("Could not save resource. Spam question not answered?");                
            }
           
            modelAndView.addObject("item", editResource);
          
            
        } else {
            log.warn("No edit resource could be setup.");
        }
       
        return modelAndView;
    }



private void processFeedAcceptancePolicy(HttpServletRequest request,
		Resource editResource) {
	if (editResource.getType().equals("F")) {              
	    ((Feed) editResource).setAcceptancePolicy(request.getParameter("acceptance"));
	    log.debug("Feed acceptance policy set to: " + ((Feed) editResource).getAcceptancePolicy());
	    rssPrefetcher.decacheAndLoad((Feed) editResource);
	}
}



   	
	private void saveResource(HttpServletRequest request, User loggedInUser, Resource editResource, boolean newSubmission, boolean resourceUrlHasChanged) {		
		contentUpdateService.update(editResource, loggedInUser, request, newSubmission, resourceUrlHasChanged);
	}


	private void removeSuggestion(Resource editResource) {
		if (suggestionDAO.isSuggested(editResource.getUrl())) {            		
			suggestionDAO.removeSuggestion(editResource.getUrl());
		}
	}


    private void processImage(HttpServletRequest request, Newsitem editResource, User loggedInUser) {
    	Image image = (Image) request.getAttribute("image");
    	editResource.setImage(image);
    }

    
    private boolean userIsAllowedToEdit(Resource editResource, HttpServletRequest request, User loggedInUser) {    
    	return editPermissionService.canEdit(editResource);
    }



    private void populateCommonLocal(ModelAndView mv) {      
        mv.addObject("top_level_tags", resourceDAO.getTopLevelTags());      
    }

    
    private void populateSubmitCommonElements(HttpServletRequest request, ModelAndView modelAndView) throws IOException {
        populateCommonLocal(modelAndView);
        modelAndView.addObject("tag_select", tagWidgetFactory.createMultipleTagSelect(new HashSet<Tag>()));
   
        User loggedInUser = loggedInUserFilter.getLoggedInUser();
        boolean userIsLoggedIn = loggedInUser != null;       
        modelAndView.addObject("publisher_select", publisherSelectFactory.createPublisherSelectWithNoCounts(null, userIsLoggedIn).toString());
        
        if (userIsLoggedIn) {
            // TODO duplication - also - what does this do?
            modelAndView.addObject("show_additional_tags", 1);
        }
    }
    
    
    protected void populatePublisherField(ModelAndView modelAndView, boolean userIsLoggedIn, Resource editResource) throws IOException {
        boolean isPublishedResource = editResource instanceof PublishedResource;  
        if (isPublishedResource) {
            Website publisher = null;
            if (((PublishedResource) editResource).getPublisher() != null ) {
                publisher = ((PublishedResource) editResource).getPublisher();              
            }
            modelAndView.addObject("publisher_select", publisherSelectFactory.createPublisherSelectWithNoCounts(publisher, userIsLoggedIn).toString());   
        } else {
            log.info("Edit resource is not a publiser resource.");
        }
    }

    
}
