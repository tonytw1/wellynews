package nz.co.searchwellington.controllers.admin;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.filters.ResourceParameterFilter;
import nz.co.searchwellington.filters.TagsParameterFilter;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.TagDAO;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

public class AdminRequestFilter {
	

    Logger log = Logger.getLogger(AdminRequestFilter.class);
    

	private ResourceRepository resourceDAO;
	private TagDAO tagDAO;
	
	
	public AdminRequestFilter(ResourceRepository resourceDAO, TagDAO tagDAO) {		
		this.resourceDAO = resourceDAO;
		this.tagDAO = tagDAO;
	}


	public void loadAttributesOntoRequest(HttpServletRequest request) {		
		log.info("Looking for tag parameter");
		if (request.getParameter("tag") != null) {
			String tagName = request.getParameter("tag");
			Tag tag = tagDAO.loadTagByName(tagName);
			if (tag != null) {
	           	request.setAttribute("tag", tag);
			}
		}
		
		// TODO try catch Test coverage
	    if (request.getParameter("item") != null) {
            Integer item = Integer.parseInt(request.getParameter("item"));
            request.setAttribute("item", item);            
        }
	    
	    Long twitterId = parseTwitterIdfromRequest(request);
    	if (twitterId != null) {
    		request.setAttribute("twitterId", twitterId);
    	}
        
		
		log.info("Looking for date field");
		if (request.getParameter("date") != null) {
			final String dateString = (String) request.getParameter("date");
			SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy");              
			try {            	
				Date date = df.parse(dateString);
				if (date != null) {
					request.setAttribute("date", new DateTime(date).toDate());   	
				}              
			} catch (ParseException e) {
				log.warn("Invalid date string supplied: " + dateString);
			}        	
		}
		
		
		log.info("Looking for embargoed field");
		if (request.getParameter("embargo_date") != null) {
			final String dateString = (String) request.getParameter("embargo_date");
			SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy HH:mm");              
			try {
				Date date = df.parse(dateString);
				if (date != null) {
					request.setAttribute("embargo_date", new DateTime(date).toDate());   	
				}              
			} catch (ParseException e) {
				log.warn("Invalid embargo date string supplied: " + dateString);
			}        	
		}
						
		// TODO Test coverage	Deprecated?	
        if (request.getParameter("publisher") != null && !request.getParameter("publisher").equals("")) {
            final String publisherUrlWords = request.getParameter("publisher");
            Resource publisher = resourceDAO.getPublisherByUrlWords(publisherUrlWords);
            if (publisher != null) {            
                request.setAttribute("publisher", publisher);
            }
        }
        
    	TagsParameterFilter tagsParameterFilter = new TagsParameterFilter(tagDAO);	// TODO up
    	tagsParameterFilter.filter(request); 
		    	
    	ResourceParameterFilter resourceParameterFilter = new ResourceParameterFilter(resourceDAO);	// TODO up
    	resourceParameterFilter.filter(request);
		
    	
		if (request.getParameter("feed") != null) {
			String feedParameter = request.getParameter("feed");			
			try {
        		final int feedID = Integer.parseInt(feedParameter);
        		if (feedID > 0) {
        			Resource feed = resourceDAO.loadResourceById(feedID);
        			if (feed != null) {                	
        				log.info("Found feed: " + feed.getName());
        				request.setAttribute("feedAttribute", feed);        			
        			}
        		}
        	} catch (NumberFormatException e) {
        		log.debug("Invalid feed id given: " + feedParameter);
        	}
		}
		
        if (request.getParameter("parent") != null) {
            String tagName = request.getParameter("parent");
            Tag tag = tagDAO.loadTagByName(tagName);
            if (tag != null) {
            	log.info("Found parent tag: " + tag.getName());
            	request.setAttribute("parent_tag", tag);
            }
        }
                
                
	    log.info("Looking for edit tags");
        Pattern pattern = Pattern.compile("^/edit/tag/(.*)$");
        Matcher matcher = pattern.matcher(request.getPathInfo());
        if (matcher.matches()) {
        	final String tagname = matcher.group(1);
	        Tag tag = tagDAO.loadTagByName(tagname);
	        if (tag != null) {
	        	request.setAttribute("tag", tag);
	        }	        
        }
	}
	
	
	private Long parseTwitterIdfromRequest(HttpServletRequest request) {
		String twitterIdParam = request.getParameter("twitterid");
		log.info("Twitted id parameter: " + twitterIdParam);
		if (twitterIdParam != null) {    
			try {
				Long twitterId = Long.parseLong(twitterIdParam);
				log.info("Twitted id parsed to: " + twitterId);
				return twitterId;
			} catch (Exception e) {
        		return null;
			}
		}
		return null;
	}
	
}
