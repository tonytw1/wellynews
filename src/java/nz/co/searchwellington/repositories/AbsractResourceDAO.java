package nz.co.searchwellington.repositories;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;

import nz.co.searchwellington.model.CalendarFeed;
import nz.co.searchwellington.model.CalendarFeedImpl;
import nz.co.searchwellington.model.Comment;
import nz.co.searchwellington.model.CommentFeed;
import nz.co.searchwellington.model.CommentFeedImpl;
import nz.co.searchwellington.model.DiscoveredFeed;
import nz.co.searchwellington.model.DiscoveredFeedImpl;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedImpl;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.NewsitemImpl;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.TagImpl;
import nz.co.searchwellington.model.Watchlist;
import nz.co.searchwellington.model.WatchlistImpl;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.model.WebsiteImpl;

import org.apache.log4j.Logger;


public abstract class AbsractResourceDAO implements ResourceRepository {
    
    Logger log = Logger.getLogger(AbsractResourceDAO.class);
    
      
    public Newsitem createNewNewsitem() {         
        return new NewsitemImpl(0, "", "", "", Calendar.getInstance().getTime(), null, new HashSet<Tag>(),
                            new HashSet<DiscoveredFeed>());
    }
    
    public Website createNewWebsite() {
        return new WebsiteImpl( 0, "", "", Calendar.getInstance().getTime(), 
                                "", 
                                new HashSet <Resource> (), 
                                new HashSet <Feed>(), new HashSet<Watchlist>(), 
                                new HashSet<Tag>(),
                                new HashSet <DiscoveredFeed>(),
                                new HashSet <CalendarFeed>());
    }

    public Feed createNewFeed() {
        return new FeedImpl(0, "", "", "", null, null, new HashSet<Tag>());
    }
    
    
    public Watchlist createNewWatchlist() {
        log.info("Creating new Watchlist.");
        return new WatchlistImpl(0, "", "", "", null, new HashSet<Tag>(),
                new HashSet <DiscoveredFeed>());
    }

    public Tag createNewTag() {
        Tag newTag = new TagImpl(0, "", "", null, new HashSet<Tag>(), 0);
        log.info("Created tag: " + newTag.getName() + " (#" + newTag.getId() + ")");
		return newTag;
    }
    
    
    
    public CommentFeed createNewCommentFeed(String commentFeedUrl) {
        CommentFeed commentFeed = new CommentFeedImpl(0, commentFeedUrl, new ArrayList<Comment>(), null, null, new HashSet<Resource>());     
        return commentFeed;
    }
    
    
    public CalendarFeed createNewCalendarFeed(String url) {
        CalendarFeed calendar = new CalendarFeedImpl(0, url, "", "", new HashSet<Tag>());
        return calendar;
    }
    
    public DiscoveredFeed createNewDiscoveredFeed(String discoveredUrl) {
        DiscoveredFeed discoveredFeed = new DiscoveredFeedImpl();
        discoveredFeed.setUrl(discoveredUrl);
        discoveredFeed.setReferences(new HashSet<Resource>());
        return discoveredFeed;
    }

    
}
