package nz.co.searchwellington.controllers.admin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.config.Config;
import nz.co.searchwellington.repositories.ConfigRepository;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.ecs.html.Option;
import org.apache.ecs.html.Select;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

public class ConfigEditController extends MultiActionController {
    
    private ConfigRepository configDAO;

    
    protected ConfigEditController(ConfigRepository configDAO) {     
        this.configDAO = configDAO;        
    }
    
    
   
    
    @SuppressWarnings("unchecked")
    public ModelAndView edit(HttpServletRequest request, HttpServletResponse response) {        
        ModelAndView modelAndView = new ModelAndView("editConfig");
        
        modelAndView.getModel().put("heading", "Editing Configuration");
        
        modelAndView.getModel().put("feedburner_widget", StringEscapeUtils.escapeHtml(configDAO.getFeedBurnerWidget()));
        modelAndView.getModel().put("stats_tracking_code", StringEscapeUtils.escapeHtml(configDAO.getStatsTracking()));
        modelAndView.getModel().put("flickr_pool_group_id", StringEscapeUtils.escapeHtml(configDAO.getFlickrPoolGroupId()));                
        modelAndView.getModel().put("clickthrough_tracking_select", makeClickThroughSelect(configDAO.getUseClickThroughCounter()).toString());
        return modelAndView;
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




    @SuppressWarnings("unchecked")
    public ModelAndView save(HttpServletRequest request, HttpServletResponse response) {
        
        ModelAndView modelAndView = new ModelAndView("savedConfig");
        modelAndView.getModel().put("heading", "Configuration Saved");
        
        Config config = configDAO.getConfig();
        config.setFeedBurnerWidget(request.getParameter("feedburner_widget"));
        config.setStatsTracking(request.getParameter("stats_tracking_code"));
        config.setFlickrPoolGroupId(request.getParameter("flickr_pool_group_id"));
        
        config.setUseClickthroughCounter(request.getParameter("use_clickthrough_tracking"));
        configDAO.saveConfig(config);
        return modelAndView;
    }

   
    
    

}
