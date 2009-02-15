package nz.co.searchwellington.repositories;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import nz.co.searchwellington.model.Snapshot;

import org.apache.log4j.Logger;

public class SnapshotDAO {
		
	private static final String SNAPSHOTS_CACHE_NAME = "snapshots";
    Logger log = Logger.getLogger(SnapshotDAO.class);
    
	public SnapshotDAO() {    
    }

    public Snapshot loadSnapshot(String url) {		
		return getSnapshotFromCache(url);        
	}
	
	public void saveSnapshot(String url, Snapshot snapshot) {
	    putSyndFeedIntoCache(url, snapshot);
	}
        
    private void putSyndFeedIntoCache(String url, Snapshot snapshot) {
        try {
            CacheManager manager = CacheManager.create();
            Cache cache = manager.getCache(SNAPSHOTS_CACHE_NAME);

            if (cache != null && snapshot != null) {
                Element cachedFeedElement = new Element(url, snapshot);                
                cache.put(cachedFeedElement);
                log.info("Caching snapshot for url: " + url);
            }
        } catch (CacheException e) {
            log.error("CacheException while trying to cache snapshot.", e);
        }
    }

    
    private Snapshot getSnapshotFromCache(String url) {
        try {
            CacheManager manager = CacheManager.create();
            Cache cache = manager.getCache(SNAPSHOTS_CACHE_NAME);

            if (cache != null) {
                Element cacheElement = cache.get(url);
                if (cacheElement != null) {
                    Snapshot snapshot = (Snapshot) cacheElement.getObjectValue();
                    log.info("Found snapshot in cache for url: " + url);                  
                    return snapshot;
                } else {
                    log.info("No cached snapshot found for url: " + url);
                }
            }
        } catch (CacheException e) {
            log.error("CacheException while trying to access snapshot from cache.", e);
        }
        return null;
    }

}
