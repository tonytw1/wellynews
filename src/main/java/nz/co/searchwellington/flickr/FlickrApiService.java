package nz.co.searchwellington.flickr;

import java.io.IOException;
import java.util.Collection;

import javax.xml.parsers.ParserConfigurationException;

import nz.co.searchwellington.model.Tag;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.aetrion.flickr.Flickr;
import com.aetrion.flickr.FlickrException;
import com.aetrion.flickr.REST;
import com.aetrion.flickr.photos.Photo;

public class FlickrApiService {
    
    static Logger log = Logger.getLogger(FlickrApiService.class);
    final String REST_HOST = "www.flickr.com";

    
    private String apiKey;
    
    public FlickrApiService() {      
    }
    
    
    public int getPoolPhotoCountForTag(Tag tag, String poolGroupId) {

        boolean groupIdSet = poolGroupId != null && !poolGroupId.trim().equals("");
        if (groupIdSet) {
            log.info("Checking count for tag " + tag.getName() + " in pool group id " + poolGroupId);
            try {                
                Flickr f;
                REST rest;

                rest = new REST();
                rest.setHost(REST_HOST);
                f = new Flickr(apiKey, rest);

                String[] tags = new String[1];
                tags[0] = tag.getName();
                
                Collection<Photo> poolPhotos = f.getPoolsInterface().getPhotos(poolGroupId, tags, 0, 0);
                return poolPhotos.size();
                
            } catch (IOException e) {
               log.error(e);
            } catch (SAXException e) {
                log.error(e);
            } catch (FlickrException e) {
                log.error(e);
            } catch (ParserConfigurationException e) {
                log.error(e);
            }
        } else {
            log.warn("Not checking for Flickr counts; pool group id is not set.");
        }
        return 0;

    }
    
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
}
