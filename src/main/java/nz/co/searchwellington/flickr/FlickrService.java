package nz.co.searchwellington.flickr;

import nz.co.searchwellington.caching.MemcachedCache;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.ConfigDAO;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FlickrService {

    private static final int ONE_DAY = 3600 * 24;

	private static Logger log = Logger.getLogger(FlickrService.class);

    private FlickrApi flickerApiService;
    private ConfigDAO configDAO;
	private MemcachedCache cache;
    
	@Autowired
    public FlickrService(FlickrApi flickrApi, ConfigDAO configDAO, MemcachedCache cache) {
        this.flickerApiService = flickrApi;
        this.configDAO = configDAO;
        this.cache = cache;
    }    
    
    public int getFlickrPhotoCountFor(Tag tag) {
        log.info("Running Flickr tag photo count for tag: " + tag.getDisplayName());
        
        final String poolGroupId = configDAO.getFlickrPoolGroupId();   
        final String cacheKey = "flickrphotocount:" + poolGroupId + ":" + tag.getDisplayName().replaceAll("\\s", "");
        
        final Integer cachedCount = (Integer) cache.get(cacheKey);
        if (cachedCount != null) {
        	log.info("Returning cached count: " + cachedCount);
        	return cachedCount;
        }
        
        final int poolPhotoCountForTag = flickerApiService.getPoolPhotoCountForTag(tag.getDisplayName(), poolGroupId);
        log.info("Got pool photo count of " + poolPhotoCountForTag + " for tag '" + tag.getDisplayName() + "'");
        
        log.info("Caching count for tag: " + tag.getDisplayName());
        cache.put(cacheKey, ONE_DAY, poolPhotoCountForTag);
		return poolPhotoCountForTag;
    }

}
