package nz.co.searchwellington.filters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.model.CalendarFeed;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.ResourceRepository;

import org.apache.log4j.Logger;

public class RequestFilter {

    Logger log = Logger.getLogger(RequestFilter.class);
    
    protected ResourceRepository resourceDAO;

    public RequestFilter(ResourceRepository resourceDAO) {
        this.resourceDAO = resourceDAO;     
    }
    
    public void loadAttributesOntoRequest(HttpServletRequest request) {
    	
    	if (request.getParameter("page") != null) {
    		String pageString = request.getParameter("page");
    		try {
    			Integer page = Integer.parseInt(pageString);
    			request.setAttribute("page", page);
    		} catch (NumberFormatException e) {
    		}    		
    	}
    	
    	
        // TODO depricate be using a url tagname instead of a form parameter - move to adminFilter?
        if (request.getParameter("tag") != null) {
            String tagName = request.getParameter("tag");        
            Tag tag = resourceDAO.loadTagByName(tagName);             
               request.setAttribute("tag", tag);            
        }
        
        
        if (request.getParameter("publisher") != null && !request.getParameter("publisher").equals("")) {
            String publisherUrlWords = request.getParameter("publisher");
            Website publisher = resourceDAO.getPublisherByUrlWords(publisherUrlWords);          
            request.setAttribute("publisher", publisher);
        }
        
        
        if (request.getPathInfo().matches("^/archive/.*/.*$")) {            
            Date monthFromPath = getArchiveDateFromPath(request.getPathInfo());
            if (monthFromPath != null) {
            	request.setAttribute("month", monthFromPath);
            	log.info("Setting archive month to: " + monthFromPath);
            }
            return;
        }
                
        
		if (request.getPathInfo().matches("^/feed/.*$")) {			
			String feedUrlWords = request.getPathInfo().split("/")[2];			
        	Resource feed = resourceDAO.loadFeedByUrlWords(feedUrlWords);
        	if (feed != null) {
        		request.setAttribute("feedAttribute", feed);
        		return;
        	}
        }
        
                
        log.info("Looking for publiser watchlist and feeds urls");
        final String publisherUrlWords = getPublisherUrlWordsFromPath(request.getPathInfo());
        if (publisherUrlWords != null) {
        	Website publisher = resourceDAO.getPublisherByUrlWords(publisherUrlWords);
        	request.setAttribute("publisher", publisher);
        	return;
        }
        
        log.info("Looking for combiner urls");        
        Pattern pattern = Pattern.compile("^/(.*)\\+(.*?)(/rss|/json)?$");
        Matcher matcher = pattern.matcher(request.getPathInfo());
        if (matcher.matches()) {
        	final String left = matcher.group(1);
        	final String right = matcher.group(2);        	
        	log.info("Path matches combiner pattern for '" + left + "', '" + right + "'");        	
        	// righthand side is always a tag;
        	// Left could be a publisher or a tag.
        	Tag rightHandTag = resourceDAO.loadTagByName(right);        	
        	if (rightHandTag != null) {
	        	Website publisher = resourceDAO.getPublisherByUrlWords(left);
	        	log.info("Right matches tag: " + rightHandTag.getName());
	        	if (publisher != null) {
	        		log.info("Left matches publisher: " + publisher.getName());
	        		request.setAttribute("publisher", publisher);
	        		request.setAttribute("tag", rightHandTag);
	        		return;
	        		
	        	} else {
	        		Tag leftHandTag = resourceDAO.loadTagByName(left);
	        		if (leftHandTag != null) {
	        			log.info("Left matches tag: " + leftHandTag.getName());
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
        
             
        log.info("Looking for single publisher and tag urls");
        Pattern contentPattern = Pattern.compile("^/(.*?)(/.*)?(/(rss|json|comment|geotagged))?$");
        Matcher contentMatcher = contentPattern.matcher(request.getPathInfo());
        if (contentMatcher.matches()) {
        	final String match = contentMatcher.group(1);
        	log.debug("'" + match + "' matches content");
	        	
        	log.info("Looking for tag '" + match + "'");
        	Tag tag = resourceDAO.loadTagByName(match);
	        if (tag != null) {
	        	log.info("Setting tag: " + tag.getName());
	        	request.setAttribute("tag", tag);
	        	List<Tag> tags = new ArrayList<Tag>();
	        	tags.add(tag);
	        	request.setAttribute("tags", tags);
	        	return;
	        } else {
	        	log.info("Looking for publisher '" + match + "'");
	        	Website publisher = (Website) resourceDAO.getPublisherByUrlWords(match);
	        	if (publisher != null) {
	        		log.info("Setting publisher: " + publisher.getName());
	        		request.setAttribute("publisher", publisher);
	        		return;
	       		}
	       	}
	        return;
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

  

    protected Integer parseResourceIDFromRequestParameter(HttpServletRequest request) {
        Integer requestResourceID = null;        
        if (request.getParameter("resource") != null) {        
            // TODO does not fail gracefully of an invalid int is given.
            // Should just give a null resource and keep going in this case.
            requestResourceID= Integer.parseInt(request.getParameter("resource"));           
        }
        return requestResourceID;
    }
    
    
    public Date getArchiveDateFromPath(String path) {
        // TODO this method can probably be written in alot less lines, with regexs and a matches check.
        if (path.startsWith("/archive/")) {
            String[] fields = path.split("/");
            if (fields.length == 4) {
                String archiveMonthString = fields[2] + " " + fields[3];
                SimpleDateFormat df = new SimpleDateFormat("yyyy MMM");              
                try {
                    Date month = df.parse(archiveMonthString);
                    return month;
                } catch (ParseException e) {
                    throw (new IllegalArgumentException(e.getMessage()));
                }
            }
        }
        return null;
    }
    

    protected String getPublisherUrlWordsFromPath(String pathInfo) {     
        Pattern pattern = Pattern.compile("^/(.*)/(calendars|feeds|watchlist)$");
        Matcher matcher = pattern.matcher(pathInfo);
        if (matcher.matches()) {
        	return matcher.group(1);
        }
        return null;
    }
    
    
    
}
