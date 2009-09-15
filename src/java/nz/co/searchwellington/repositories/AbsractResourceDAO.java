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
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.Twit;
import nz.co.searchwellington.model.TwitteredNewsitem;
import nz.co.searchwellington.model.Watchlist;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.model.WebsiteImpl;

import org.apache.log4j.Logger;


public abstract class AbsractResourceDAO implements ResourceRepository {
    
    Logger log = Logger.getLogger(AbsractResourceDAO.class);
    
      
    public Newsitem createNewNewsitem() {         
        return new NewsitemImpl(0, "", "", "", Calendar.getInstance().getTime(), null, new HashSet<Tag>(),
                            new HashSet<DiscoveredFeed>(), new HashSet<Twit>());
    }
    
    public TwitteredNewsitem createNewTwitteredNewsitem(long twitterId) {         
        return new TwitteredNewsitem(0, "", "", "", Calendar.getInstance().getTime(), null, new HashSet<Tag>(),
                            new HashSet<DiscoveredFeed>(), twitterId);
    }
        
    public Website createNewWebsite() {
        return new WebsiteImpl( 0, "", "", Calendar.getInstance().getTime(), 
                                "", 
                                new HashSet <Newsitem> (), 
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
        return new Watchlist(0, "", "", "", null, new HashSet<Tag>(),
                new HashSet <DiscoveredFeed>());
    }

    public Tag createNewTag() {
        Tag newTag = new Tag(0, "", "", null, new HashSet<Tag>(), 0);
        log.info("Created tag: " + newTag.getName() + " (#" + newTag.getId() + ")");
		return newTag;
    }
    
    
    
    public CommentFeed createNewCommentFeed(String commentFeedUrl) {
        CommentFeed commentFeed = new CommentFeed(0, commentFeedUrl, new ArrayList<Comment>(), null, null, new HashSet<Resource>());
        return commentFeed;
    }
    
    
    public CalendarFeed createNewCalendarFeed(String url) {
        CalendarFeed calendar = new CalendarFeed(0, url, "", "", new HashSet<Tag>());
        return calendar;
    }
    
    public DiscoveredFeed createNewDiscoveredFeed(String discoveredUrl) {
        DiscoveredFeed discoveredFeed = new DiscoveredFeed();
        discoveredFeed.setUrl(discoveredUrl);
        discoveredFeed.setReferences(new HashSet<Resource>());
        return discoveredFeed;
    }

	public Resource loadResourceByUniqueUrl(String url) {
		return null;
	}

    
}
