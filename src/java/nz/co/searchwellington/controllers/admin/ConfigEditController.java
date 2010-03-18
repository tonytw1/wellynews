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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

public class ConfigEditController extends MultiActionController {
    
    private ConfigRepository configDAO;
	private LoggedInUserFilter loggedInUserFilter;

           
    public ConfigEditController(ConfigRepository configDAO, LoggedInUserFilter loggedInUserFilter) {
		this.configDAO = configDAO;
		this.loggedInUserFilter = loggedInUserFilter;
	}


	public ModelAndView edit(HttpServletRequest request, HttpServletResponse response) {
    	User loggedInUser = loggedInUserFilter.getLoggedInUser();
    	if (!(loggedInUser != null && loggedInUser.isAdmin())) {
    		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        	return null;
    	}
    	    	
        ModelAndView modelAndView = new ModelAndView("editConfig");        
        modelAndView.addObject("heading", "Editing Configuration");      
        modelAndView.addObject("stats_tracking_code", StringEscapeUtils.escapeHtml(configDAO.getStatsTracking()));
        modelAndView.addObject("flickr_pool_group_id", StringEscapeUtils.escapeHtml(configDAO.getFlickrPoolGroupId()));
        modelAndView.addObject("clickthrough_tracking_select", makeClickThroughSelect(configDAO.getUseClickThroughCounter()).toString());
        modelAndView.addObject("twitter_listener_is_enabled_select", makeTwitterEnabledSelect(configDAO.isTwitterListenerEnabled()).toString());
        return modelAndView;
    }
    
    
    
    @Transactional
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

   
    
    private Select makeTwitterEnabledSelect(boolean selected) {
    	  Select select = new Select("twitter_listener_is_enabled");

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


	private Select makeClickThroughSelect(boolean selected) {
        Select select = new Select("use_clickthrough_tracking");

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
