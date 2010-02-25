package nz.co.searchwellington.repositories;

import java.util.Date;
import java.util.List;

import nz.co.searchwellington.model.CalendarFeed;
import nz.co.searchwellington.model.CommentFeed;
import nz.co.searchwellington.model.DiscoveredFeed;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.Twit;
import nz.co.searchwellington.model.TwitteredNewsitem;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.Watchlist;
import nz.co.searchwellington.model.Website;


public interface ResourceRepository {

    public Resource loadResourceById(int resourceID);
    public void saveResource(Resource resource);
     
    public boolean isResourceWithUrl(String url);
    public Resource loadResourceByUrl(String url);    
    public void deleteResource(Resource resource);
        
    public Tag createNewTag();
    public Website createNewWebsite();
    public Newsitem createNewNewsitem();
    public Feed createNewFeed();
    public Watchlist createNewWatchlist();

    public List<Feed> getAllFeeds();
    public List<Feed> getFeedsToRead();
    
    public List<Resource> getAllCalendarFeeds();
    
    // TODO move to tagDAO - probably already a delegate
    public Tag loadTagById(int tagID);
    public Tag loadTagByName(String tagname);
    public void saveTag(Tag editTag);   

    public List<Tag> getTopLevelTags();
    public void deleteTag(Tag tag);
    
    public List<Resource> getNotCheckedSince(Date date, int maxItems);
    public List<Resource> getNotCheckedSince(Date launchedDate, Date lastScanned, int maxItems);
    
    public List<CommentFeed> getCommentFeedsToCheck(int maxItems);
    
    public List<Tag> getTagsMatchingKeywords(String keywords);
    public List<Resource> getAllPublishersMatchingStem(String stem, boolean showBroken);
    public List<Resource> getRecentlyChangedWatchlistItems();
    public List<DiscoveredFeed> getAllDiscoveredFeeds();
    public DiscoveredFeed loadDiscoveredFeedByUrl(String discoveredUrl);
    public void saveDiscoveredFeed(DiscoveredFeed discoveredFeed);
    public DiscoveredFeed createNewDiscoveredFeed(String discoveredUrl);
    public CommentFeed loadCommentFeedByUrl(String feedLink);
    public CommentFeed createNewCommentFeed(String discoveredUrl);
	public void saveCommentFeed(CommentFeed commentFeed);
	
    public List<Newsitem> getRecentUntaggedNewsitems();   
    public Date getNewslogLastChanged();
        
    public List<Integer> getAllResourceIds();
    public CalendarFeed createNewCalendarFeed(String url);
    public List<Newsitem> getLatestTwitteredNewsitems(int numberOfItems, boolean showBroken);
    
	public Website getPublisherByUrlWords(String publisherUrlWords);
	public Feed loadFeedByUrlWords(String string);
	
	public Newsitem loadNewsitemBySubmittingTwitterId(long twitterId);
	
	public Resource loadResourceByUniqueUrl(String url);
	public List<String> getPublisherNamesByStartingLetters(String q);
	public Resource getPublisherByName(String publisherName);
	public List<Resource> getOwnedBy(User loggedInUser, int maxItems);
	public TwitteredNewsitem createNewTwitteredNewsitem(Twit twit);	
	public List<Resource> getNewsitemsMatchingStem(String stem);
	
	public List<Twit> getAllTweets();
	public Twit loadTweetByTwitterId(Long id);
	public void saveTweet(Twit twit);
	
	public List<Resource> getResourcesWithTag(Tag tag);
	public List<Newsitem> getNewsitemsForFeed(Feed feed);	
	public List<Resource> getAllWatchlists();
    
}