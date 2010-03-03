package nz.co.searchwellington.jobs;

import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.ConfigRepository;
import nz.co.searchwellington.repositories.FlickrDAO;
import nz.co.searchwellington.repositories.TagDAO;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

public class FlickrJob {

    Logger log = Logger.getLogger(FlickrJob.class);

    private FlickrDAO flickrDAO;
    private ConfigRepository configDAO;
    private TagDAO tagDAO;
    
    public FlickrJob() {}
    
    public FlickrJob(FlickrDAO flickrDAO, ConfigRepository configDAO, TagDAO tagDAO) {
        this.flickrDAO = flickrDAO;
        this.configDAO = configDAO;
        this.tagDAO = tagDAO;
    }
    
    
    // TODO This is quite a wide transaction.
    @Transactional
    public void run() {
        log.info("Running Flickr tag photo count update");
        
        String poolGroupId = configDAO.getFlickrPoolGroupId();        
        for (Tag  tag : tagDAO.getAllTags()) { 
            log.info("Updating Flickr photo count for tag: " + tag.getName());
            int tagPhotoCount = flickrDAO.getPoolPhotoCountForTag(tag, poolGroupId);
            tag.setFlickrCount(tagPhotoCount);
            tagDAO.saveTag(tag);
            log.info("Count: " + tagPhotoCount);           
        }
        
        log.info("Flickr update completed.");        
    }

}
