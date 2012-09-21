package nz.co.searchwellington.controllers.admin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.config.Config;
import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.ConfigRepository;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.ecs.html.Option;
import org.apache.ecs.html.Select;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ConfigEditController {
    
    private ConfigRepository configDAO;
	private LoggedInUserFilter loggedInUserFilter;
	
	public ConfigEditController() {
	}
	
	public ConfigEditController(ConfigRepository configDAO, LoggedInUserFilter loggedInUserFilter) {
		this.configDAO = configDAO;
		this.loggedInUserFilter = loggedInUserFilter;
	}
	
	@RequestMapping("/admin/config/edit")
	public ModelAndView edit(HttpServletRequest request, HttpServletResponse response) {
    	final User loggedInUser = loggedInUserFilter.getLoggedInUser();
    	if (!(loggedInUser != null && loggedInUser.isAdmin())) {
    		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    		return null;
    	}
    	    	
    	final ModelAndView modelAndView = new ModelAndView("editConfig");        
        modelAndView.addObject("heading", "Editing Configuration");      
        modelAndView.addObject("stats_tracking_code", StringEscapeUtils.escapeHtml(configDAO.getStatsTracking()));
        modelAndView.addObject("flickr_pool_group_id", StringEscapeUtils.escapeHtml(configDAO.getFlickrPoolGroupId()));
        modelAndView.addObject("clickthrough_tracking_select", buildBooleanSelect(configDAO.getUseClickThroughCounter(), "use_clickthrough_tracking").toString());        
        modelAndView.addObject("feed_reading_enabled_select", buildBooleanSelect(configDAO.isFeedReadingEnabled(), "feed_reading_enabled").toString());
        
        modelAndView.addObject("twitter_listener_is_enabled_select", buildBooleanSelect(configDAO.isTwitterListenerEnabled(), "twitter_listener_is_enabled").toString());        
        return modelAndView;
    }
	
	@Transactional
	@RequestMapping(value="/admin/config/save", method=RequestMethod.POST)
    public ModelAndView save(HttpServletRequest request, HttpServletResponse response) {
    	User loggedInUser = loggedInUserFilter.getLoggedInUser();
    	if (!(loggedInUser != null && loggedInUser.isAdmin())) {
    		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    		return null;
    	}
    	
        ModelAndView modelAndView = new ModelAndView("savedConfig");
        modelAndView.addObject("heading", "Editing Configuration");
        
        Config config = configDAO.getConfig();
        config.setStatsTracking(request.getParameter("stats_tracking_code"));
        config.setFlickrPoolGroupId(request.getParameter("flickr_pool_group_id"));
        config.setUseClickthroughCounter(request.getParameter("use_clickthrough_tracking"));
        
        boolean twitterIsEnabled = false;
        final String twitter = request.getParameter("twitter_listener_is_enabled");
        if (twitter != null && twitter.equals("1")) {
        	twitterIsEnabled = true;
        }
        config.setTwitterListenerEnabled(twitterIsEnabled);
        
        configDAO.saveConfig(config);
        return modelAndView;
    }
	
    private Select buildBooleanSelect(boolean selected, String fieldname) {
		Select select = new Select(fieldname);

        Option noOption = new Option("0");
        noOption.setFilterState(true);        
        noOption.addElement("No");
        
        Option yesOption = new Option("1");
        yesOption.setFilterState(true);
        yesOption.addElement("Yes");
        
        if (selected) {
            yesOption.setSelected(true);
        } else {
            noOption.setSelected(true);
        }
        
        select.addElement(noOption);
        select.addElement(yesOption);
        return select;
	}

}
