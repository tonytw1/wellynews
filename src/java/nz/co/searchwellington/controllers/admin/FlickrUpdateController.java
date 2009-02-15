package nz.co.searchwellington.controllers.admin;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.BaseMultiActionController;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.ConfigRepository;
import nz.co.searchwellington.repositories.FlickrDAO;
import nz.co.searchwellington.repositories.ResourceRepository;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;


public class FlickrUpdateController extends BaseMultiActionController {

	final Logger log = Logger.getLogger(FlickrUpdateController.class);
    
    private ResourceRepository resourceDAO;
	private FlickrDAO flickrDAO;
	// TODO move configDAO into flickrDAO bean?
    private ConfigRepository configDAO;
        
   
  
    public FlickrUpdateController(ResourceRepository resourceDAO, FlickrDAO flickrDAO, ConfigRepository configDAO) {       
        this.resourceDAO = resourceDAO;
        this.flickrDAO = flickrDAO;
        this.configDAO = configDAO;
    }

    
    @SuppressWarnings("unchecked")
    public ModelAndView update(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();   
        for (Tag tag : resourceDAO.getAllTags()) {
            flickrDAO.getPoolPhotoCountForTag(tag, configDAO.getFlickrPoolGroupId());            
        }
      
        mv.setViewName("flickrUpdate");      
        return mv;
    }

}
