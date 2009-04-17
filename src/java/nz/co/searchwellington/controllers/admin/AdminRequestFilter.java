package nz.co.searchwellington.controllers.admin;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.ResourceRepository;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

public class AdminRequestFilter {
	

    Logger log = Logger.getLogger(AdminRequestFilter.class);
    

	private ResourceRepository resourceDAO;
	
	
	public AdminRequestFilter(ResourceRepository resourceDAO) {		
		this.resourceDAO = resourceDAO;
	}


	public void loadAttributesOntoRequest(HttpServletRequest request) {
		
		log.info("Looking for tag parameter");
		if (request.getParameter("tag") != null) {
			String tagName = request.getParameter("tag");
			Tag tag = resourceDAO.loadTagByName(tagName);
			if (tag != null) {
	           	request.setAttribute("tag", tag);
			}
		}
		
		log.info("Looking for resource parameter");
		if (request.getParameter("resource") != null) {
			String resourceParametere = request.getParameter("resource");			
			try {
        		final int resourceId = Integer.parseInt(resourceParametere);
        		if (resourceId > 0) {
        			Resource resource = resourceDAO.loadResourceById(resourceId);
        			if (resource != null) {        				
        				request.setAttribute("resource", resource);
        				return;
        			}
        		}
        	} catch (NumberFormatException e) {
        		log.debug("Invalid resource id given: " + resourceParametere);
        	}
		}
		
		
		log.info("Looking for feed parameter");
		if (request.getParameter("feed") != null) {
			String feedParameter = request.getParameter("feed");			
			try {
        		final int feedID = Integer.parseInt(feedParameter);
        		if (feedID > 0) {
        			Resource feed = resourceDAO.loadResourceById(feedID);
        			if (feed != null) {                	
        				request.setAttribute("feedAttribute", feed);
        				return;
        			}
        		}
        	} catch (NumberFormatException e) {
        		log.debug("Invalid feed id given: " + feedParameter);
        	}
		}
		
        log.info("Looking for parent tag");
        if (request.getParameter("parent") != null) {
            String tagName = request.getParameter("parent");
            Tag tag = resourceDAO.loadTagByName(tagName);
            if (tag != null) {
            	request.setAttribute("parent_tag", tag);
            }
        }
                
        log.info("Looking for date field");
        if (request.getParameter("date") != null) {
        	final String dateString = (String) request.getParameter("date");
            SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy");              
            try {            	
                Date date = df.parse(dateString);
                if (date != null) {
                	request.setAttribute("date", new DateTime(date));   	
                }              
            } catch (ParseException e) {
                log.warn("Invalid date string supplied: " + dateString);
            }        	
        }
                
	    log.info("Looking for edit tags");
        Pattern pattern = Pattern.compile("^/edit/tag/(.*)$");
        Matcher matcher = pattern.matcher(request.getPathInfo());
        if (matcher.matches()) {
        	final String tagname = matcher.group(1);
	        Tag tag = resourceDAO.loadTagByName(tagname);
	        if (tag != null) {
	        	request.setAttribute("tag", tag);
	        }	        
        }
	}
	
}
