package nz.co.searchwellington.model;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.sun.syndication.feed.module.georss.GeoRSSModule;
import com.sun.syndication.feed.module.georss.W3CGeoModuleImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;


public class RomeRssFeed {

    Logger log = Logger.getLogger(RomeRssFeed.class);

    private String title;
    private String linkUrl;
    private String description;
    private List<Newsitem> contents;

    private String clickThroughTrackingUrl;

    
    
    public RomeRssFeed(String title, String linkUrl, String description, List<Newsitem> contents, String clickThroughTrackingUrl) {
        this.title = title;
        this.contents = contents;
        this.description = description;
        this.linkUrl = linkUrl;
        this.clickThroughTrackingUrl = clickThroughTrackingUrl;
    }

    
    
    public String outputAsXml() {
        SyndFeed feed = new SyndFeedImpl();

        feed.setTitle(title);
        feed.setFeedType("rss_2.0");
        feed.setLink(linkUrl);
        feed.setDescription(description);
        feed.setEncoding("UTF-8");

        List<SyndEntry> entries = new ArrayList<SyndEntry>();

        if (contents.size() > 0) {
            Iterator iterator = contents.iterator();
            while (iterator.hasNext()) {
                Resource selectedResource = (Resource) iterator.next();

                SyndEntry entry = selectedResource.getRssItem();
                if (clickThroughTrackingUrl != null) {
                    entry.setLink(clickThroughTrackingUrl + "?resource=" + selectedResource.getId());
                }
                               
                addGeoRSSModule(selectedResource, entry);                
                entries.add(entry);
            }
            
        } else {
            log.warn("RSS Collection is empty");
        }

        feed.setEntries(entries);

        StringWriter writer = new StringWriter();
        SyndFeedOutput output = new SyndFeedOutput();

        try {
            output.output(feed, writer);
            return writer.toString();

        } catch (IOException e) {
            log.error(e);
        } catch (FeedException e) {
            log.error(e);
        }

        return null;
    }



    private void addGeoRSSModule(Resource selectedResource, SyndEntry entry) {
        final Geocode geocode = selectedResource.getGeocode();
        if (geocode != null && geocode.isValid()) {            
            GeoRSSModule geoRSSModule = new W3CGeoModuleImpl();     
            geoRSSModule.setLatitude(geocode.getLatitude());
            geoRSSModule.setLongitude(geocode.getLongitude());
            entry.getModules().add(geoRSSModule);            
        }
    }
    
}
