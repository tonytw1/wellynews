package nz.co.searchwellington.jobs;

import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.ConfigRepository;
import nz.co.searchwellington.repositories.FlickrDAO;
import nz.co.searchwellington.repositories.ResourceRepository;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

public class FlickrJob {

    Logger log = Logger.getLogger(FlickrJob.class);

    private FlickrDAO flickrDAO;
    private ResourceRepository resourceDAO;
    private ConfigRepository configDAO;
    
    
  
    public FlickrJob() {
    }


    public FlickrJob(FlickrDAO flickrDAO, ResourceRepository resourceDAO, ConfigRepository configDAO) {
        this.flickrDAO = flickrDAO;
        this.resourceDAO = resourceDAO;
        this.configDAO = configDAO;
    }
    
    
    // TODO This is quite a wide transaction.
    @Transactional
    public void run() {
        log.info("Running Flickr tag photo count update");
        
        String poolGroupId = configDAO.getFlickrPoolGroupId();        
        for (Tag  tag : resourceDAO.getAllTags()) { 
            log.info("Updating Flickr photo count for tag: " + tag.getName());
            int tagPhotoCount = flickrDAO.getPoolPhotoCountForTag(tag, poolGroupId);
            tag.setFlickrCount(tagPhotoCount);
            resourceDAO.saveTag(tag);
            log.info("Count: " + tagPhotoCount);           
        }
        
        log.info("Flickr update completed.");        
    }

}
