package nz.co.searchwellington.controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import nz.co.searchwellington.commentfeeds.CommentFeedDetectorService;
import nz.co.searchwellington.commentfeeds.CommentFeedGuesserService;
import nz.co.searchwellington.commentfeeds.guessers.EyeOfTheFishCommentFeedGuesser;
import nz.co.searchwellington.commentfeeds.guessers.WordpressCommentFeedGuesser;
import nz.co.searchwellington.feeds.CommentFeedReader;
import nz.co.searchwellington.htmlparsing.Extractor;
import nz.co.searchwellington.htmlparsing.LinkExtractor;
import nz.co.searchwellington.model.CommentFeed;
import nz.co.searchwellington.model.DiscoveredFeed;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Snapshot;
import nz.co.searchwellington.repositories.FeedRepository;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.SnapshotDAO;
import nz.co.searchwellington.repositories.TechnoratiDAO;
import nz.co.searchwellington.utils.HttpFetcher;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.sun.syndication.io.FeedException;

public class LinkChecker {
    
    Logger log = Logger.getLogger(LinkChecker.class);
       
    private ResourceRepository resourceDAO;
    private FeedRepository feedDAO;
    private CommentFeedReader commentFeedReader;
	private CommentFeedDetectorService commentFeedDetector;
	private SnapshotDAO snapshotDAO;
    private TechnoratiDAO technoratiDAO;

    
    // Used by Spring transactional proxy.
    public LinkChecker() {
    }
    
    
     
    



    public LinkChecker(ResourceRepository resourceDAO, FeedRepository feedDAO, CommentFeedReader commentFeedReader, CommentFeedDetectorService commentFeedDetector, SnapshotDAO snapshotDAO, TechnoratiDAO technoratiDAO) {    
        this.resourceDAO = resourceDAO;
        this.feedDAO = feedDAO;
        this.commentFeedReader = commentFeedReader;
        this.commentFeedDetector = commentFeedDetector;
        this.snapshotDAO = snapshotDAO;
        this.technoratiDAO = technoratiDAO;
    }







    @Transactional()
    public void scanResource(int checkResourceId) {

        Resource checkResource = resourceDAO.loadResourceById(checkResourceId);
         
        log.info("Checking: " + checkResource.getName() + "(" + checkResource.getUrl() + ")");        
        log.debug("Before status: " + checkResource.getHttpStatus());      
        
        String before = null;
        Snapshot snapshot = snapshotDAO.loadSnapshot(checkResource.getUrl());
        if (snapshot != null) {
        	before = snapshot.getBody();
        } else {
        	snapshot = new Snapshot(checkResource.getUrl());
        }
        
        Calendar currentTime = Calendar.getInstance();
        
        // TODO move exludes to a field on Publisher.
        // Cricket Wellington's site does something weird when spidered.
        boolean excludeFromCheck = checkResource.getUrl() != null && checkResource.getUrl().contains("www.cricketwellington.co.nz");
        if (!excludeFromCheck) {
            httpCheck(checkResource, snapshot);
                       
            checkForChangeUsingSnapshots(checkResource, before, currentTime, snapshot.getBody());
            
            if (checkResource.getType().equals("F")) {
                updateLatestFeedItem((Feed) checkResource);
            } else {
                // For non feeds, parse for rss auto discovery links.                      
                discoverFeeds(checkResource);
            }
            
        } else {
            // Assume resources excluded from check are ok.
            checkResource.setHttpStatus(200);
        }
               
        checkResource.setLastScanned(currentTime.getTime());
        
        
        // Check if the resource if now viewable but not previously launched.
        boolean goneLive = checkResource.getHttpStatus() == 200 && checkResource.getLiveTime() == null;
        if (goneLive) {
            checkResource.setLiveTime(currentTime.getTime());                                 
        }
        
        // TODO get exclude url from config.
        int technoratiCount = technoratiDAO.getTechnoratiLinkCount(checkResource.getUrl(), "http://www.wellington.gen.nz");
        log.info("Technorati count is: " + technoratiCount);
        checkResource.setTechnoratiCount(technoratiCount);
        
        log.debug("Saving resource.");
        resourceDAO.saveResource(checkResource);
        
        // If the item is a newsitem, then load it's comments.
        if (checkResource.getType().equals("N") && ((Newsitem) checkResource).getCommentFeed() != null) {            
            commentFeedReader.loadCommentsFromCommentFeed(((Newsitem) checkResource).getCommentFeed());           
        }
        
    }

    private void updateLatestFeedItem(Feed checkResource) {
        log.debug("Resource is a feed; checking for latest item publication date.");
        try {                          
            List <Resource> feeditems = feedDAO.getFeedNewsitems(checkResource);
            log.debug("Feed has " + feeditems.size() + " items.");
            
            Date latestPublicationDate = feedDAO.getLatestPublicationDate(checkResource);                              
            // TODO would be nice if the hibernate mapping preserved this field as a datetime rather than just a date.
            checkResource.setLatestItemDate(latestPublicationDate);
            log.debug("Latest item publication date for this feed was: " + checkResource.getLatestItemDate());
            
        } catch (IllegalArgumentException e) {
            log.error(e);
        } catch (FeedException e) {
            log.error(e);
        } catch (IOException e) {
            log.error(e);
        }
    }



    protected void discoverFeeds(Resource checkResource) {
        Snapshot snapshot = snapshotDAO.loadSnapshot(checkResource.getUrl());
        for (Iterator iter = getAutoDiscoveredUrlsFromResource(snapshot).iterator(); iter.hasNext();) {
            String discoveredUrl = (String) iter.next();
            
            if (!discoveredUrl.startsWith("http://")) {
                log.info("url is not fully qualified: " + discoveredUrl);
				try {
					final String sitePrefix = new URL(checkResource.getUrl()).getHost();
					discoveredUrl = "http://" + sitePrefix + discoveredUrl;
                    log.info("url expanded to: " + discoveredUrl);
				} catch (MalformedURLException e) {
					log.error("Invalid url", e);
				}
            }
            
            boolean isCommentFeedUrl = commentFeedDetector.isCommentFeedUrl(discoveredUrl);            
            if (isCommentFeedUrl) {
                log.debug("Discovered url is a comment feed: " + discoveredUrl);
                if (checkResource.getType().equals("N")) {        
                    recordCommentFeed(checkResource, discoveredUrl);
                }
            } else {
                recordDiscoveredFeedUrl(checkResource, discoveredUrl);
            }
        }
        
        if (checkResource.getType().equals("N")) {
            addGuessedCommentFeeds(checkResource);
        }
    }



    private void addGuessedCommentFeeds(Resource checkResource) {
        // TODO Inject with spring.       
        EyeOfTheFishCommentFeedGuesser eyeOfTheFishCommentFeedGuesser = new EyeOfTheFishCommentFeedGuesser();       
        WordpressCommentFeedGuesser wordpressCommentFeedGuesser = new WordpressCommentFeedGuesser();
    	
        CommentFeedGuesserService commentFeedGuesser = new CommentFeedGuesserService(eyeOfTheFishCommentFeedGuesser, wordpressCommentFeedGuesser); 
        String commentFeedUrl = commentFeedGuesser.guessCommentFeedUrl(checkResource.getUrl());             
        if (commentFeedUrl != null) {
            recordCommentFeed(checkResource, commentFeedUrl);         
        }
    }

    
    
    protected Set<String> getAutoDiscoveredUrlsFromResource(Snapshot snapshot) {
        Set<String> feedUrls = new HashSet<String>();       
        if (snapshot != null) {
            feedUrls.addAll(locateAutodiscoveredFeedsFromContent(snapshot.getBody()));
        } 
        return feedUrls;
    }



    // TODO merge this with the discoveredFeedUrl method.
    private void recordCommentFeed(Resource checkResource, String discoveredUrl) {
        // TODO can hibernate take care of this?
        CommentFeed commentFeed = resourceDAO.loadCommentFeedByUrl(discoveredUrl);                        
        if (commentFeed == null) {
            log.debug("Comment feed url was not found in the database. Creating new comment feed: " + discoveredUrl);
            commentFeed = resourceDAO.createNewCommentFeed(discoveredUrl);                         
            resourceDAO.saveCommentFeed(commentFeed);
        }
        ((Newsitem) checkResource).setCommentFeed(commentFeed);      
    }


   
    private void recordDiscoveredFeedUrl(Resource checkResource, String discoveredUrl) {
        DiscoveredFeed discoveredFeed = resourceDAO.loadDiscoveredFeedByUrl(discoveredUrl);
        if (discoveredFeed == null) {
            log.debug("Discovered feed url was not found in the database. Creating new: " + discoveredUrl);
            discoveredFeed = resourceDAO.createNewDiscoveredFeed(discoveredUrl);                  
        }               
        discoveredFeed.getReferences().add(checkResource);
        resourceDAO.saveDiscoveredFeed(discoveredFeed);
    }
    
    
    private void checkForChangeUsingSnapshots(Resource checkResource, String before, Calendar currentTime, String after) {             
        log.info("Comparing content before and after snapshots from content change.");
        boolean contentChanged = contentChanged(before, after);                       
                   
        if (contentChanged) {
            log.info("Change in content checksum detected. Setting last changed.");
            checkResource.setLastChanged(currentTime.getTime());
            
        } else {
            log.info("No change in content detected.");
        }
    }
    
    
    
    protected static boolean contentChanged(String before, String after) {
        boolean contentChanged = false;
        if (before != null && after != null) {
            contentChanged = !after.equals(before);
        } else {
            final boolean bothAreNull = (before == null) && (after == null);           
            if (bothAreNull) {
                contentChanged = false;
            } else {
                contentChanged = true;
            }
        }
        return contentChanged;
    }
   
   
    private boolean crawlAllowed(String url) {
        // TODO implement robots.txt. Needs to check robots.txt file on host.
        return true;
    }
    
 
    
    private int httpCheck(Resource checkResource, Snapshot snapshot) {
        String url = checkResource.getUrl();
        // Check for a robots.txt exclution
        if (crawlAllowed(url)) {

            try {               
                int httpResult = -1;
                
                HttpFetcher client = new HttpFetcher();             
                InputStream inputStream = client.httpFetch(checkResource.getUrl());                              
              
                if (inputStream != null) {
                    httpResult = 200;
                    snapshot.setBody(readEncodedResponse(inputStream, "UTF-8"));
                } else {
                   snapshot.setBody(null);
                }                
                checkResource.setHttpStatus(httpResult);                
                snapshotDAO.saveSnapshot(url, snapshot);
                                
            } catch (IllegalArgumentException e) {
                log.error("Error while checking url: ", e);
                checkResource.setHttpStatus(-1);
            } catch (IOException e) {
                log.error("Error while checking url: ", e);
                checkResource.setHttpStatus(-1);
            }

        } else {
            // TODO does this actually have an effect from here?
            // The return code for not allowed to crawl is -2.
            //log.info("Crawling is disallowed for this resource.");
            return -2;
        }

        return -1;
    }


    private static String readEncodedResponse(InputStream is, String charSet) throws IOException {
        BufferedReader d = new BufferedReader(new InputStreamReader(is, charSet));        
        StringBuffer responseBody = new StringBuffer();
        String input;
        while ((input = d.readLine()) != null) {                
            responseBody.append(input);
            responseBody.append("\n");
        }
        return responseBody.toString();            
     }

    
    
   
    
    
    private Set<String> locateAutodiscoveredFeedsFromContent(String inputHTML) {    
        Set<String> feedLinks = new HashSet<String>();
        
        if (inputHTML != null) {            
            log.info("Parsing html for auto discovered links.");
            // TODO inject this.
            Extractor linkExtractor = new LinkExtractor();
            List<String> extractedLinks = linkExtractor.extractLinks(inputHTML);
            feedLinks.addAll(extractedLinks);                              
        }
        return feedLinks;
    }


    
  
  

    
    
    
}
