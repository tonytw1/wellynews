package nz.co.searchwellington.repositories;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nz.co.searchwellington.model.ArchiveLink;
import nz.co.searchwellington.model.CalendarFeed;
import nz.co.searchwellington.model.CommentFeed;
import nz.co.searchwellington.model.DiscoveredFeed;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.PublisherContentCount;
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
        
    public Tag createNewTag();
    public Website createNewWebsite();
    public Newsitem createNewNewsitem();
    public Feed createNewFeed();
    public Watchlist createNewWatchlist();

    public List<Feed> getAllFeeds();
	public List<Feed> getAllFeedsByName();
    public List<Feed> getFeedsToRead();
    
    public List<Resource> getAllCalendarFeeds();
    public List<Resource> getCalendarFeedsForTag(Tag tag, boolean showBroken);
    public List<Resource> getAllWatchlists(boolean showBroken);
    
    // TODO move to tagDAO - probably already a delegate
    public Tag loadTagById(int tagID);
    public Tag loadTagByName(String tagname);
    public void saveTag(Tag editTag);   
    public List <Tag> getAllTags();
    public List<Tag> getTopLevelTags();
    public void deleteTag(Tag tag);
     
    public List<Resource> getTaggedResources(Tag tag, int max_newsitems);
    public List<Resource> getTaggedWebsites(Set<Tag> tags, boolean showBroken, int max_websites);        
    public List<Resource> getTaggedNewsitems(Tag tag, boolean showBroken, int startIndex, int maxItems);
    public List<Resource> getTaggedNewsitems(Set<Tag> name, boolean showBroken, int max_secondary_items);
    
    public void deleteResource(Resource resource);
    
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
    public int getWebsiteCount(boolean showBroken);
    public int getNewsitemCount(boolean showBroken);
    
    public List<Tag> getCommentedTags(boolean showBroken); 
    public List<Tag> getGeotaggedTags(boolean showBroken);	
    
    public List<ArchiveLink> getArchiveMonths(boolean showBroken);
    public List<Integer> getAllResourceIds();
    public CalendarFeed createNewCalendarFeed(String url);
    public List<Newsitem> getLatestTwitteredNewsitems(int numberOfItems, boolean showBroken);
    
    public Date getLastLiveTimeForTag(Tag tag);
	public List<Resource> getAllValidGeocoded(int max_events_to_show_on_front, boolean showBroken);
    public List<Resource> getTaggedGeotaggedNewsitems(Tag tag, int maxNumber, boolean showBroken);
	public List<Resource> getResourcesWithTag(Tag tag);
	public Website getPublisherByUrlWords(String publisherUrlWords);
	public List<Resource> getPublisherTagCombinerNewsitems(Website publisher, Tag tag, boolean showBroken, int maxItems);
	public Feed loadFeedByUrlWords(String string);
	
	public List<Resource> getCommentedNewsitems(int maxItems, boolean showBroken, boolean hasComments, int startIndex);
	public List<Resource> getCommentedNewsitemsForTag(Tag tag, boolean showBroken, int maxItems, int startIndex);
	
	public List<Resource> getTwitterMentionedNewsitems(int maxItems);
	public Newsitem loadNewsitemBySubmittingTwitterId(long twitterId);
	
	public Resource loadResourceByUniqueUrl(String url);
	public List<String> getPublisherNamesByStartingLetters(String q);
	public Resource getPublisherByName(String publisherName);
	public List<Resource> getOwnedBy(User loggedInUser, int maxItems);
	public TwitteredNewsitem createNewTwitteredNewsitem(Twit twit);	
	public List<Resource> getNewsitemsMatchingStem(String stem);
	public List<Resource> getBrokenSites();
	
	public List<Twit> getAllTweets();
	public Twit loadTweetByTwitterId(Long id);
	public void saveTweet(Twit twit);
	
	
	
	
	public int getCommentCount();
	public List<Newsitem> getNewsitemsForFeed(Feed feed);
	public List<Resource> getRecentTwitteredNewsitems(int maxItems, boolean showBroken);
	public List<Resource> getRecentTwitteredNewsitemsForTag(int maxItems, boolean showBroken, Tag tag);
	public List<Resource> getAllFeeds(boolean shouldShowBroken);
	public Map<String, Integer> getArchiveStatistics(boolean shouldShowBroken);
    
}