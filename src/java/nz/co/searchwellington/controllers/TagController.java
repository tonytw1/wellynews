package nz.co.searchwellington.controllers;


import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.models.ContentModelBuilderService;
import nz.co.searchwellington.filters.RequestFilter;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.ConfigRepository;
import nz.co.searchwellington.repositories.EventsDAO;
import nz.co.searchwellington.repositories.FeedRepository;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.statistics.StatsTracking;

import org.springframework.web.servlet.ModelAndView;

import com.sun.syndication.io.FeedException;


public class TagController extends BaseMultiActionController {

    private RequestFilter requestFilter;
    private LoggedInUserFilter loggedInUserFilter;
    private RssUrlBuilder rssUrlBuilder;    
    private ContentModelBuilderService contentModelBuilder;



    public TagController(ResourceRepository resourceDAO, 
    		RequestFilter requestFilter, 
    		LoggedInUserFilter loggedInUserFilter,
    		UrlStack urlStack, 
    		ConfigRepository configDAO,    		   		
    		RssUrlBuilder rssUrlBuilder,     		
    		ContentModelBuilderService contentModelBuilder
    		) {
        this.resourceDAO = resourceDAO;    
        this.requestFilter = requestFilter;
        this.loggedInUserFilter = loggedInUserFilter;
        this.urlStack = urlStack;
        this.configDAO = configDAO;       
        this.rssUrlBuilder = rssUrlBuilder;        
        this.contentModelBuilder = contentModelBuilder;        
    }
    
       
	public ModelAndView normal(HttpServletRequest request, HttpServletResponse response) throws IllegalArgumentException, FeedException, IOException {
        logger.info("Starting normal content");                                  
        requestFilter.loadAttributesOntoRequest(request);
        loggedInUserFilter.loadLoggedInUser(request);
        boolean showBroken = false;
        
		ModelAndView mv = contentModelBuilder.populateContentModel(request);
		if (mv != null) {
			addCommonModelElements(mv, showBroken);
			return mv;
		}
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		return null;
    }
	
    private void addCommonModelElements(ModelAndView mv, boolean showBroken) throws IOException {
		mv.addObject("top_level_tags", resourceDAO.getTopLevelTags());		
        final List<Newsitem> latestNewsitems = resourceDAO.getLatestNewsitems(5, showBroken);
        mv.addObject("latest_newsitems", latestNewsitems);
	}


    
    
    
    
    public ModelAndView geotagged(HttpServletRequest request, HttpServletResponse response) throws IOException {        
        ModelAndView mv = new ModelAndView();        
        mv.setViewName("geotagged");        
        
        User loggedInUser = setLoginState(request, mv);
        boolean showBroken = loggedInUser != null;
        
        requestFilter.loadAttributesOntoRequest(request);
        mv.addObject("tags", request.getAttribute("tags"));
        
        if (request.getAttribute("tag") != null) {
            Tag tag = (Tag) request.getAttribute("tag");
            populateCommon(request, mv, showBroken, tag);

            mv.addObject("geotagged_tags", resourceDAO.getGeotaggedTags(showBroken));   
            
            mv.addObject("tag", tag);
            mv.addObject("heading", tag.getDisplayName() + " related geotagged");
            // TODO want areaname back in here.
            mv.addObject("description", tag.getDisplayName() + " listings");                        
            
            mv.addObject("main_heading", null);
            //populateGeocoded(mv, showBroken, null, tag);
            populateSecondaryLatestNewsitems(mv, loggedInUser);

        } else {
            throw new RuntimeException("Invalid tag name.");            
        }
        
        return mv;        
    }    
   
    
    
    

    public ModelAndView newsArchive(HttpServletRequest request, HttpServletResponse response) throws IOException {        
        ModelAndView mv = new ModelAndView();        
        mv.setViewName("tagNewsArchive");
                
        User loggedInUser = setLoginState(request, mv);
        boolean showBroken = loggedInUser != null;
        
        requestFilter.loadAttributesOntoRequest(request);
        mv.addObject("tags", request.getAttribute("tags"));
        
        if (request.getAttribute("tag") != null) {
            Tag tag = (Tag) request.getAttribute("tag");
            populateCommon(request, mv, showBroken, tag);

            mv.addObject("tag", tag);
            mv.addObject("heading", tag.getDisplayName() + " related newsitems");
            mv.addObject("description", tag.getDisplayName() + " related newsitems.");            
                        
            final List<Resource> allTagNewsitems = resourceDAO.getTaggedNewitems(tag, showBroken, 500);
            mv.addObject("main_content", allTagNewsitems);
            mv.addObject("main_heading", null);

            if (allTagNewsitems.size() > 0) {               
                setTagRss(mv, tag);               
            }
            populateSecondaryLatestNewsitems(mv, loggedInUser);

        } else {
            throw new RuntimeException("Invalid tag name.");            
        }
        
        return mv;
    }


    private void setTagRss(ModelAndView mv, Tag tag) {
        setRss(mv, rssUrlBuilder.getRssTitleForTag(tag), rssUrlBuilder.getRssUrlForTag(tag));
    }
    
    
    
    
    
    
     private void populateCommon(HttpServletRequest request, ModelAndView mv, boolean showBroken, Tag tag) {
        urlStack.setUrlStack(request);
        populateAds(request, mv, showBroken);
        StatsTracking.setRecordPageImpression(mv, configDAO.getStatsTracking());       
        mv.addObject("top_level_tags", resourceDAO.getTopLevelTags());                
        if (tag != null && tag.getName().equals("realestate")) {      
        	mv.addObject("use_big_ads", 1);
        }
    }

}
