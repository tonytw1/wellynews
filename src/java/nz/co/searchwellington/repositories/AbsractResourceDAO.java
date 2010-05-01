package nz.co.searchwellington.repositories;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;

import nz.co.searchwellington.model.CalendarFeed;
import nz.co.searchwellington.model.Comment;
import nz.co.searchwellington.model.CommentFeed;
import nz.co.searchwellington.model.DiscoveredFeed;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedImpl;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.NewsitemImpl;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Twit;
import nz.co.searchwellington.model.TwitteredNewsitem;
import nz.co.searchwellington.model.Watchlist;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.model.WebsiteImpl;

import org.apache.log4j.Logger;


public abstract class AbsractResourceDAO implements ResourceRepository {
    
    Logger log = Logger.getLogger(AbsractResourceDAO.class);
    
      
    public Newsitem createNewNewsitem() {         
        return new NewsitemImpl(0, "", "", "", Calendar.getInstance().getTime(), null,
                            new HashSet<DiscoveredFeed>(), null, new HashSet<Twit>());
    }
    
    public TwitteredNewsitem createNewTwitteredNewsitem(Twit twit) {         
        return new TwitteredNewsitem(0, "", "", "", Calendar.getInstance().getTime(), null,
                            new HashSet<DiscoveredFeed>(), twit);
    }
        
    public Website createNewWebsite() {
        return new WebsiteImpl( 0, "", "", Calendar.getInstance().getTime(), 
                                "", 
                                new HashSet <Feed>(), new HashSet<Watchlist>(),
                                new HashSet <DiscoveredFeed>(),
                                new HashSet <CalendarFeed>());
    }

    public Feed createNewFeed() {
        return new FeedImpl(0, "", "", "", null, null);
    }
    
    
    public Watchlist createNewWatchlist() {
        log.info("Creating new Watchlist.");
        return new Watchlist(0, "", "", "", null, new HashSet <DiscoveredFeed>());
      
    }

    
    public CommentFeed createNewCommentFeed(String commentFeedUrl) {
        CommentFeed commentFeed = new CommentFeed(0, commentFeedUrl, new ArrayList<Comment>(), null, null, new HashSet<Resource>());
        return commentFeed;
    }
    
    
    public CalendarFeed createNewCalendarFeed(String url) {
        CalendarFeed calendar = new CalendarFeed(0, url, "", "");
        return calendar;
    }
    
    public DiscoveredFeed createNewDiscoveredFeed(String discoveredUrl) {
        DiscoveredFeed discoveredFeed = new DiscoveredFeed();
        discoveredFeed.setUrl(discoveredUrl);
        discoveredFeed.setReferences(new HashSet<Resource>());
        return discoveredFeed;
    }
    
}
