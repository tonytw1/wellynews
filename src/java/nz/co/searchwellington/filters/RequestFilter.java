package nz.co.searchwellington.filters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.model.CalendarFeed;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.TagDAO;

import org.apache.log4j.Logger;

public class RequestFilter {

    public static final String SEARCH_TERM = "searchterm";
        
    static Pattern contentPattern = Pattern.compile("^/(.*?)(/.*)?(/(rss|json|comment|geotagged))?$");
    static Pattern combinerPattern = Pattern.compile("^/(.*)\\+(.*?)(/rss|/json)?$");
    static Pattern autotagPattern = Pattern.compile("^/autotag/(.*)$");
       
	static Logger log = Logger.getLogger(RequestFilter.class);
    
    private ResourceRepository resourceDAO;
    private TagDAO tagDAO;
    
    private GoogleSearchTermExtractor searchTermExtractor;
    
    public RequestFilter() {         
    }
    
    public RequestFilter(ResourceRepository resourceDAO, TagDAO tagDAO, GoogleSearchTermExtractor searchTermExtractor) {
        this.resourceDAO = resourceDAO;
        this.tagDAO = tagDAO;
        this.searchTermExtractor = searchTermExtractor;
    }
    
          
	public void loadAttributesOntoRequest(HttpServletRequest request) {
		log.debug("Looking for google search referrer");
		extractGoogleReferrer(request);
		
		
		log.debug("Loading attributes onto request");		
    	if (request.getParameter("page") != null) {
    		String pageString = request.getParameter("page");
    		try {
    			Integer page = Integer.parseInt(pageString);
    			request.setAttribute("page", page);
    		} catch (NumberFormatException e) {
    		}    		
    	}
    	
    	
    	// TODO duplicate from admin request filter
		if (request.getParameter("tags") != null) {
			String[] tagNames = request.getParameterValues("tags");
			
			List <Tag> tags = new ArrayList <Tag>();
			for (int i = 0; i < tagNames.length; i++) {             
				String tagName = tagNames[i];
				if (tagName != null) {  	// TODO cleaning        
					Tag tag = tagDAO.loadTagByName(tagName);
					if (tag != null) {
						tags.add(tag);
					} else {
						log.warn("Could not find tag with name: " + tagName);
					}
				} 
			}           
			request.setAttribute("tags", tags);
		}
    	
    	
    	// TODO this is duplication from the admin filter.
    	if (request.getParameter("resource") != null) {
			String resourceParametere = request.getParameter("resource");			
			try {
        		final int resourceId = Integer.parseInt(resourceParametere);
        		if (resourceId > 0) {
        			Resource resource = resourceDAO.loadResourceById(resourceId);
        			if (resource != null) {
        				log.info("Found resource: " + resource.getName());
        				request.setAttribute("resource", resource);
        				return;
        			}
        		}
        	} catch (NumberFormatException e) {
        		log.warn("Invalid resource id given: " + resourceParametere);
        	}
		}
    	
		
    	
    	
        // TODO depricate be using a url tagname instead of a form parameter - move to adminFilter?
    	// Used by the rssfeeds index page?
        if (request.getParameter("tag") != null) {
            String tagName = request.getParameter("tag");            
            Tag tag = tagDAO.loadTagByName(tagName);             
               request.setAttribute("tag", tag);            
        }
        
        
        if (request.getParameter("publisher") != null && !request.getParameter("publisher").equals("")) {
            String publisherUrlWords = request.getParameter("publisher");
            Website publisher = resourceDAO.getPublisherByUrlWords(publisherUrlWords);          
            request.setAttribute("publisher", publisher);
        }
        
                
		if (request.getPathInfo().matches("^/feed/.*$")) {			
			String feedUrlWords = request.getPathInfo().split("/")[2];			
        	Resource feed = resourceDAO.loadFeedByUrlWords(feedUrlWords);
        	if (feed != null) {
        		request.setAttribute("feedAttribute", feed);
        		return;
        	}
        }
        
		
        log.debug("Looking for combiner urls");        
        Matcher matcher = combinerPattern.matcher(request.getPathInfo());
        if (matcher.matches()) {
        	final String left = matcher.group(1);
        	final String right = matcher.group(2);        	
        	log.debug("Path matches combiner pattern for '" + left + "', '" + right + "'");        	
        	// righthand side is always a tag;
        	// Left could be a publisher or a tag.
        	Tag rightHandTag = tagDAO.loadTagByName(right);        	
        	if (rightHandTag != null) {
	        	Website publisher = resourceDAO.getPublisherByUrlWords(left);
	        	log.debug("Right matches tag: " + rightHandTag.getName());
	        	if (publisher != null) {
	        		log.debug("Left matches publisher: " + publisher.getName());
	        		request.setAttribute("publisher", publisher);
	        		request.setAttribute("tag", rightHandTag);
	        		return;
	        		
	        	} else {
	        		Tag leftHandTag = tagDAO.loadTagByName(left);
	        		if (leftHandTag != null) {
	        			log.debug("Left matches tag: " + leftHandTag.getName());
	        			log.info("Setting tags '" + leftHandTag.getName() + "', '" + rightHandTag.getName() + "'");
	        			List<Tag> tags = new ArrayList<Tag>();
	        			tags.add(leftHandTag);
	        			tags.add(rightHandTag);
	        			request.setAttribute("tags", tags);
	        			return;
	        		}
	        	}
        	}
        	return;
        } 
        
 
        log.debug("Looking for single publisher and tag urls");
        Matcher contentMatcher = contentPattern.matcher(request.getPathInfo());
        if (contentMatcher.matches()) {
        	final String match = contentMatcher.group(1);
        	
        	if (!isReservedUrlWord(match)) {
	        	log.debug("'" + match + "' matches content");
		        	
	        	log.debug("Looking for tag '" + match + "'");	        	
	        	Tag tag = tagDAO.loadTagByName(match);
		        if (tag != null) {
		        	log.info("Setting tag: " + tag.getName());
		        	request.setAttribute("tag", tag);	// TODO deprecate
		        	List<Tag> tags = new ArrayList<Tag>();
		        	tags.add(tag);
		        	log.info("Setting tags: " + tags);
		        	request.setAttribute("tags", tags);
		        	return;
		        } else {
		        	log.debug("Looking for publisher '" + match + "'");
		        	Website publisher = (Website) resourceDAO.getPublisherByUrlWords(match);
		        	if (publisher != null) {
		        		log.info("Setting publisher: " + publisher.getName());
		        		request.setAttribute("publisher", publisher);
		        		return;
		       		}
		       	}
		    
		        return;
        	}
        }
        
        log.debug("Looking for autotag urls");
        Matcher autotagMatcher = autotagPattern.matcher(request.getPathInfo());
        if (autotagMatcher.matches()) {        	
        	final String match = autotagMatcher.group(1);        	
        	if (!isReservedUrlWord(match)) {
	        	log.debug("'" + match + "' matches content");
		        	
	        	log.debug("Looking for tag '" + match + "'");	        	
	        	Tag tag = tagDAO.loadTagByName(match);
		        if (tag != null) {
		        	log.info("Setting tag: " + tag.getName());
		        	request.setAttribute("tag", tag);	// TODO deprecate
		        	List<Tag> tags = new ArrayList<Tag>();
		        	tags.add(tag);
		        	log.info("Setting tags: " + tags);
		        	request.setAttribute("tags", tags);
		        	return;
		        }
		        return;
        	}
        }
        
        
        if (request.getParameter("calendarfeed") != null) {
            final int feedID = Integer.parseInt(request.getParameter("calendarfeed"));
            if (feedID > 0) {
                CalendarFeed calendarFeed = (CalendarFeed) resourceDAO.loadResourceById(feedID);
                log.debug("Loaded calendar feed object of id: " + calendarFeed.getId() + ", type: " + calendarFeed.getType());
                request.setAttribute("calendarfeed", calendarFeed);
            }
        }
        
    }

    
    private void extractGoogleReferrer(HttpServletRequest request) {
		final String referer = request.getHeader("Referer");
		if (referer != null) {
			log.info("Referer is: " + referer);
			
			final String searchTerm = searchTermExtractor.extractSearchTerm(referer);
			if (searchTerm != null) {
				log.info("Referrer search term is: " + searchTerm);
				request.setAttribute(SEARCH_TERM, searchTerm);
			}			
		}		
	}
    

    // TODO this wants to be in the spring config
	private boolean isReservedUrlWord(String urlWord) {
    	Set<String> reservedUrlWords = new HashSet<String>();
    	reservedUrlWords.add("about");
    	reservedUrlWords.add("api");
    	reservedUrlWords.add("autotag");
       	reservedUrlWords.add("index");
    	reservedUrlWords.add("feeds");
    	reservedUrlWords.add("comment");
    	reservedUrlWords.add("geotagged");
    	reservedUrlWords.add("tags");
    	return reservedUrlWords.contains(urlWord);
	}
	
}
