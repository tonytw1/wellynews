package nz.co.searchwellington.controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;

import nz.co.searchwellington.commentfeeds.CommentFeedDetectorService;
import nz.co.searchwellington.commentfeeds.CommentFeedGuesserService;
import nz.co.searchwellington.feeds.CommentFeedReader;
import nz.co.searchwellington.feeds.RssfeedNewsitemService;
import nz.co.searchwellington.htmlparsing.LinkExtractor;
import nz.co.searchwellington.model.CommentFeed;
import nz.co.searchwellington.model.DiscoveredFeed;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.SnapshotDAO;
import nz.co.searchwellington.repositories.TechnoratiDAO;
import nz.co.searchwellington.utils.HttpFetchResult;
import nz.co.searchwellington.utils.HttpFetcher;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.transaction.annotation.Transactional;

public class LinkChecker {
    
    Logger log = Logger.getLogger(LinkChecker.class);
       
    private ResourceRepository resourceDAO;
    private RssfeedNewsitemService rssfeedNewsitemService;
    private CommentFeedReader commentFeedReader;
	private CommentFeedDetectorService commentFeedDetector;
	private SnapshotDAO snapshotDAO;
    private TechnoratiDAO technoratiDAO;
	private HttpFetcher httpFetcher;
	private LinkExtractor linkExtractor;
	private CommentFeedGuesserService commentFeedGuesser;
    
    public LinkChecker() {
    }
    
    
    public LinkChecker(ResourceRepository resourceDAO, RssfeedNewsitemService rssfeedNewsitemService, CommentFeedReader commentFeedReader, CommentFeedDetectorService commentFeedDetector, SnapshotDAO snapshotDAO, TechnoratiDAO technoratiDAO, HttpFetcher httpFetcher, LinkExtractor linkExtractor, CommentFeedGuesserService commentFeedGuesser) {    
        this.resourceDAO = resourceDAO;
        this.rssfeedNewsitemService = rssfeedNewsitemService;
        this.commentFeedReader = commentFeedReader;
        this.commentFeedDetector = commentFeedDetector;
        this.snapshotDAO = snapshotDAO;
        this.technoratiDAO = technoratiDAO;
        this.httpFetcher = httpFetcher;
        this.linkExtractor = linkExtractor;
        this.commentFeedGuesser = commentFeedGuesser;
    }


    @Transactional
    public void scanResource(int checkResourceId) {
        Resource checkResource = resourceDAO.loadResourceById(checkResourceId);         
        if (checkResource != null) {
	        scanLoadedResource(checkResource);
        }        
    }


	private void scanLoadedResource(Resource checkResource) {
		log.info("Checking: " + checkResource.getName() + "(" + checkResource.getUrl() + ")");        
		log.debug("Before status: " + checkResource.getHttpStatus());      
						
		final String beforePageContent = snapshotDAO.loadContentForUrl(checkResource.getUrl());									     		
		DateTime currentTime = new DateTime();
		httpCheck(checkResource);
		
		final String pageContent = snapshotDAO.loadContentForUrl(checkResource.getUrl());
		checkForChangeUsingSnapshots(checkResource, beforePageContent, currentTime, pageContent);            
		if (checkResource.getType().equals("F")) {
			updateLatestFeedItem((Feed) checkResource);
		} else {
			// For non feeds, parse for rss auto discovery links.                      
			discoverFeeds(checkResource, pageContent);
		}
		   
		       
		checkResource.setLastScanned(currentTime.toDate());	
		boolean goneLive = checkResource.getHttpStatus() == 200 && checkResource.getLiveTime() == null;
		if (goneLive) {
		    checkResource.setLiveTime(currentTime.toDate());                                 
		}
		
		updateTechnoratiCount(checkResource);           
		readNewsitemsComments(checkResource);
		
		log.debug("Saving resource.");
		resourceDAO.saveResource(checkResource);
	}
	
	private void readNewsitemsComments(Resource checkResource) {
		if (checkResource.getType().equals("N") && ((Newsitem) checkResource).getCommentFeed() != null) {            
			commentFeedReader.loadCommentsFromCommentFeed(((Newsitem) checkResource).getCommentFeed());           
		}
	}


	private void updateTechnoratiCount(Resource checkResource) {
		// TODO get exclude url from config.
		int technoratiCount = technoratiDAO.getTechnoratiLinkCount(checkResource.getUrl(), "http://www.wellington.gen.nz");
		log.info("Technorati count is: " + technoratiCount);
		checkResource.setTechnoratiCount(technoratiCount);
	}

    private void updateLatestFeedItem(Feed checkResource) {            
        Date latestPublicationDate = rssfeedNewsitemService.getLatestPublicationDate(checkResource);     
        checkResource.setLatestItemDate(latestPublicationDate);
        log.debug("Latest item publication date for this feed was: " + checkResource.getLatestItemDate());      
    }



    protected void discoverFeeds(Resource checkResource, String pageContent) {
        for (Iterator iter = linkExtractor.extractLinks(pageContent).iterator(); iter.hasNext();) {
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
    	String commentFeedUrl = commentFeedGuesser.guessCommentFeedUrl(checkResource.getUrl());
        if (commentFeedUrl != null) {
            recordCommentFeed(checkResource, commentFeedUrl);         
        }
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
    
    
    private void checkForChangeUsingSnapshots(Resource checkResource, String before, DateTime currentTime, String after) {             
        log.info("Comparing content before and after snapshots from content change.");
        boolean contentChanged = contentChanged(before, after);                       
                   
        if (contentChanged) {
            log.info("Change in content checksum detected. Setting last changed.");
            checkResource.setLastChanged(currentTime.toDate());
            
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
   
    
    private void httpCheck(Resource checkResource) {
        String url = checkResource.getUrl();
        try {
            	String pageContent = null;
                HttpFetchResult httpResult = httpFetcher.httpFetch(checkResource.getUrl());
                if (httpResult.getStatus() == HttpStatus.SC_OK) {
                    pageContent = readEncodedResponse(httpResult.getInputStream(), "UTF-8");
                }
                checkResource.setHttpStatus(httpResult.getStatus());                
                snapshotDAO.setSnapshotContentForUrl(url, pageContent);
                return;
                
        } catch (IllegalArgumentException e) {
        	log.error("Error while checking url: ", e);        
        } catch (IOException e) {
        	log.error("Error while checking url: ", e);
        }
        checkResource.setHttpStatus(-1);            
    }


    // TODO make method of httpFetcher
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

        
   
 
}
