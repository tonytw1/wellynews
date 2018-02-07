package nz.co.searchwellington.controllers.admin;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.feeds.FeedReader;
import nz.co.searchwellington.filters.AdminRequestFilter;
import nz.co.searchwellington.permissions.EditPermissionService;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedAcceptancePolicy;
import nz.co.searchwellington.urls.UrlBuilder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.sun.syndication.io.FeedException;

@Controller
public class AdminFeedController {
    
    private static final Logger log = Logger.getLogger(AdminFeedController.class);
    
    private AdminRequestFilter requestFilter;
    private FeedReader feedReader;
    private UrlBuilder urlBuilder;
    private EditPermissionService permissionService;
    private LoggedInUserFilter loggedInUserFilter;
    
    public AdminFeedController() {	
	}
    
    @Autowired
    public AdminFeedController(AdminRequestFilter requestFilter,
			FeedReader feedReader, UrlBuilder urlBuilder,
			EditPermissionService permissionService, LoggedInUserFilter loggedInUserFilter) {
		this.requestFilter = requestFilter;
		this.feedReader = feedReader;
		this.urlBuilder = urlBuilder;
		this.permissionService = permissionService;
		this.loggedInUserFilter = loggedInUserFilter;
	}
    
    @RequestMapping("/admin/feed/acceptall")
    public ModelAndView acceptAllFrom(HttpServletRequest request, HttpServletResponse response) throws IllegalArgumentException, FeedException, IOException {
        requestFilter.loadAttributesOntoRequest(request);
        if (request.getAttribute("feedAttribute") == null) {
            throw new RuntimeException("Not found");    // TODO
        }

        final Feed feed = (Feed) request.getAttribute("feedAttribute");
        if (!permissionService.canAcceptAllFrom(feed)) {
           	log.warn("Not allowed to read this feed"); // TODO return http auth error
            throw new RuntimeException("Not allowed");    // TODO
        }

        feedReader.processFeed(feed.getId(), loggedInUserFilter.getLoggedInUser(), FeedAcceptancePolicy.ACCEPT_EVEN_WITHOUT_DATES);

        return new ModelAndView(new RedirectView(urlBuilder.getFeedUrl(feed)));   
    }
    
}
