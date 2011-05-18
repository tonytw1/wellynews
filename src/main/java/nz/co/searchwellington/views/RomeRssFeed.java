package nz.co.searchwellington.views;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import org.apache.log4j.Logger;

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
    private List<SyndEntry> entries;
    
    public RomeRssFeed(String title, String linkUrl, String description, List<SyndEntry> entries) {
        this.title = title;
        this.description = description;
        this.linkUrl = linkUrl;
        this.entries = entries;
    }

       
    public String outputAsXml() {
        SyndFeed feed = new SyndFeedImpl();

        feed.setTitle(title);
        feed.setFeedType("rss_2.0");
        feed.setLink(linkUrl);
        feed.setDescription(description);
        feed.setEncoding("UTF-8");      
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
    
}
