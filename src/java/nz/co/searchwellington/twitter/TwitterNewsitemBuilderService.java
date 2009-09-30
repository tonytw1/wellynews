package nz.co.searchwellington.twitter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.unto.twitter.TwitterProtos.Status;
import nz.co.searchwellington.model.DiscoveredFeed;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.NewsitemImpl;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.Twit;
import nz.co.searchwellington.model.TwitterMention;
import nz.co.searchwellington.model.TwitterSubmittable;
import nz.co.searchwellington.model.TwitteredNewsitem;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.PublisherGuessingService;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.utils.UrlCleaner;

import org.apache.log4j.Logger;

public class TwitterNewsitemBuilderService {

    Logger log = Logger.getLogger(TwitterNewsitemBuilderService.class);
    
    private UrlCleaner urlCleaner;
	private PublisherGuessingService publisherGuessingService;
	private ResourceRepository resourceDAO;
	private TwitterService twitterService;
    
	
    public TwitterNewsitemBuilderService(UrlCleaner urlCleaner, PublisherGuessingService publisherGuessingService, 
    		ResourceRepository resourceDAO, TwitterService twitterService) {     
        this.urlCleaner = urlCleaner;
        this.publisherGuessingService = publisherGuessingService;
        this.resourceDAO = resourceDAO;
        this.twitterService = twitterService;
    }
    
    
	public List<TwitteredNewsitem> getPossibleSubmissions() {		
		List<TwitteredNewsitem> unacceptedSubmissions = new ArrayList<TwitteredNewsitem>();
		for (TwitteredNewsitem twitteredNewsitem : extractPossibleSubmissionsFromTwitterReplies(twitterService.getReplies())) {
			if (resourceDAO.loadNewsitemBySubmittingTwitterId(twitteredNewsitem.getTwit().getId()) == null) {
				unacceptedSubmissions.add(twitteredNewsitem);
			}	
		}		
		return unacceptedSubmissions;		
	}
	
	
	public TwitteredNewsitem getPossibleSubmissionByTwitterId(Long twitterId) {
		for (TwitteredNewsitem possibleSubmission : getPossibleSubmissions()) {
			if (possibleSubmission.getTwit().getTwitterid().equals(twitterId)) {
				return possibleSubmission;
			}
		}
		return null;
	}
    
	
	public List<TwitterMention> getNewsitemMentions() {
		List<TwitterMention> RTs = new ArrayList<TwitterMention>();
		
		List<Status> replies = twitterService.getReplies();	
		for (Status status : replies) {
	
			
			String message = status.getText();			
			if (status.getInReplyToStatusId() > 0) {
							
				long inReplyTo = status.getInReplyToStatusId();
				log.info("Twit '" + status.getText() + "' is in reply to twit #: " + inReplyTo);
				
				Twit referencedTwit = resourceDAO.loadTweetByTwitterId(inReplyTo);
				if (referencedTwit == null) {
					Status referencedStatus = twitterService.getTwitById(inReplyTo);
					if (status != null) {
						referencedTwit = new Twit(referencedStatus);
						resourceDAO.saveTweet(referencedTwit);						
					}
				}
				
				if (referencedTwit != null) {
					message = referencedTwit.getText();
					Resource referencedNewsitem = extractReferencedResourceFromMessage(message);
					if (referencedNewsitem != null && referencedNewsitem.getType().equals("N")) {
						Twit replyTwit = loadOrCreateTwit(status);
						
						boolean isSubmittingTwit = isSubmittingTwit((Newsitem) referencedNewsitem, replyTwit);
						if (!isSubmittingTwit) {
							RTs.add(new TwitterMention((Newsitem) referencedNewsitem, replyTwit));
							log.info("Twit '" + replyTwit + "' is a reply to: " + referencedNewsitem);
						}
						
					}
					
				} else {
					log.warn("Could not find replied to tweet: " + inReplyTo);
				}
			
			} else if (message != null) {				
				Resource referencedNewsitem = extractReferencedResourceFromMessage(message);					
				if (referencedNewsitem != null && referencedNewsitem.getType().equals("N")) {
					log.info("Found RT: " + referencedNewsitem.getName() + ", " + message);
					Twit tweet = loadOrCreateTwit(status);
					
					boolean isSubmittingTwit = ((TwitterSubmittable) referencedNewsitem).getSubmittingTwit() != null &&
						((TwitterSubmittable) referencedNewsitem).getSubmittingTwit().getTwitterid().equals(tweet.getTwitterid());
										
					log.info("isSubmittedTwit: " + isSubmittingTwit);
					if (!isSubmittingTwit) {					
						RTs.add(new TwitterMention((Newsitem) referencedNewsitem, tweet));
						
					} else {
						log.info("Not adding to mentions as looks like submitting tweet: " + tweet.getText());
					}
				}				
			}			
		}
		return RTs;
	}


	private boolean isSubmittingTwit(Newsitem referencedNewsitem, Twit replyTwit) {
		log.info("Newsitems submitting twit: " + referencedNewsitem.getSubmittingTwit());
		if (referencedNewsitem.getSubmittingTwit() != null) {
			log.info("Newsitems submitted twit is: " + referencedNewsitem.getSubmittingTwit().getTwitterid() + ". Reply twit is " + replyTwit.getTwitterid());
			return referencedNewsitem.getSubmittingTwit().getTwitterid().equals(replyTwit.getTwitterid());
		}
		return false;
	}


	private Resource extractReferencedResourceFromMessage(String message) {
		final String url = this.extractUrlFromMessage(message);
		Resource referencedNewsitem = null;
		if (url != null) {
			final String cleanedUrl = urlCleaner.cleanSubmittedItemUrl(url);
			log.debug("Found url '" + cleanedUrl + "' in message: " + message);
			
			referencedNewsitem = resourceDAO.loadResourceByUrl(cleanedUrl); // TOOO load newsitem by url method on DAO instead?
		}
		return referencedNewsitem;
	}


	


	private Twit loadOrCreateTwit(Status status) {
		Twit twit = resourceDAO.loadTweetByTwitterId(status.getId());
		if (twit == null) {
			twit = new Twit(status);
			resourceDAO.saveTweet(twit);
		}
		return twit;
	}
	
	
	
	public Newsitem makeNewsitemFromTwitteredNewsitem(TwitteredNewsitem twitteredNewsitem) {
		// TODO constructor calls should be in the resourceDAO?
    	Newsitem newsitem = new NewsitemImpl(0, twitteredNewsitem.getName(), twitteredNewsitem.getUrl(), twitteredNewsitem.getDescription(), twitteredNewsitem.getDate(), null, 
    			new HashSet<Tag>(),
    			new HashSet<DiscoveredFeed>(),
    			twitteredNewsitem.getTwit(),    	
    			new HashSet<Twit>()); 
    	return newsitem;
	}

	
	 public TwitteredNewsitem createNewsitemFromTwitterReply(Twit twit) {
			if (isValidMessage(twit.getText())) {
				TwitteredNewsitem newsitem = resourceDAO.createNewTwitteredNewsitem(twit);
				
				String message = twit.getText();
				message = message.replaceFirst("@.*? ", "");
				String titleText = message.replaceFirst("http.*", "").trim();
				newsitem.setName(titleText);
				
				String url = message.replace(titleText, "").trim();
				// TODO trim trailing text after url.
				//  ie. @wellynews  RT Hutt City Council - Blind dates with books at your library http://tinyurl.com/n9j77c // Great minds meet! wcl_library too
		        if (url != "") {	        	
		            newsitem.setUrl(urlCleaner.cleanSubmittedItemUrl(url));
		            newsitem.setDate(Calendar.getInstance().getTime());            
		            Website publisher = publisherGuessingService.guessPublisherBasedOnUrl(newsitem.getUrl());
		            newsitem.setPublisher(publisher);
		            newsitem.setSubmittingTwit(twit);
		            newsitem.setDate(twit.getDate());
		            return newsitem;
		            
		        } else {
		            log.warn("Could not resolve url from twit");
		            return null;
		        }
			}
			log.debug("Not a valid submitted newsitem message: " + twit.getText());
			return null;
		}
	
    private List<TwitteredNewsitem> extractPossibleSubmissionsFromTwitterReplies(List<Status> replies) {
    	List<TwitteredNewsitem> potentialTwitterSubmissions = new ArrayList<TwitteredNewsitem>();
    	for (Status status : replies) {
    		Twit twit = new Twit(status);
    		
    		TwitteredNewsitem newsitem = this.createNewsitemFromTwitterReply(twit);    		
    		if (newsitem != null && newsitem.getUrl() != null && !newsitem.getUrl().equals("")) {
    			boolean isRT = newsitem.getName() != null & newsitem.getName().startsWith("RT");
				if (!isRT) {					
					potentialTwitterSubmissions.add(newsitem);
				}
    		}
    	}
    	return potentialTwitterSubmissions;
    }
    
    
	private boolean isValidMessage(String message) {		
		return message.startsWith("@" + twitterService.getUsername() + " ");
	}


	public String extractUrlFromMessage(String message) {
		Pattern pattern = Pattern.compile(".*(http://[\\S]+).*");
		Matcher matcher = pattern.matcher(message);
		if (matcher.matches()) {
			return matcher.group(1);
		}
		return null;
	}

	
}
