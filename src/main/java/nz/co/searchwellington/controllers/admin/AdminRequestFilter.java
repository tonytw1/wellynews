package nz.co.searchwellington.controllers.admin;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.filters.ResourceParameterFilter;
import nz.co.searchwellington.filters.TagsParameterFilter;
import nz.co.searchwellington.model.Image;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.HibernateResourceDAO;
import nz.co.searchwellington.repositories.TagDAO;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import com.clutch.dates.StringToTime;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@Component
@Scope(value="request", proxyMode=ScopedProxyMode.TARGET_CLASS)
public class AdminRequestFilter {
	
	private static Logger log = Logger.getLogger(AdminRequestFilter.class);

	private static final String DATE_FIELD = "date";
	private static final String EMBARGO_DATE_FIELD = "embargo_date";
	
	private HibernateResourceDAO resourceDAO;
	private TagDAO tagDAO;	
	private ResourceParameterFilter resourceParameterFilter;
	private TagsParameterFilter tagsParameterFilter;
	private List<SimpleDateFormat> supportedEmbargoDateFormats;
	
	public AdminRequestFilter() {
	}
	
	@Autowired
	public AdminRequestFilter(HibernateResourceDAO resourceDAO, TagDAO tagDAO,
			ResourceParameterFilter resourceParameterFilter,
			TagsParameterFilter tagsParameterFilter) {
		this.resourceDAO = resourceDAO;
		this.tagDAO = tagDAO;
		this.resourceParameterFilter = resourceParameterFilter;
		this.tagsParameterFilter = tagsParameterFilter;
		supportedEmbargoDateFormats = Lists.newArrayList();
		supportedEmbargoDateFormats.add(new SimpleDateFormat("dd MMM yyyy HH:mm"));
		supportedEmbargoDateFormats.add(new SimpleDateFormat("HH:mm"));
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
    	
    	final String image = (String) request.getParameter("image");
		if (!Strings.isNullOrEmpty(image)) {
    		request.setAttribute("image", new Image(image, null));
    	}
		
		log.info("Looking for date field");
		if (request.getParameter(DATE_FIELD) != null && !request.getParameter(DATE_FIELD).isEmpty()) {
			final String dateString = (String) request.getParameter(DATE_FIELD);
			SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy");              
			try {            	
				Date date = df.parse(dateString);
				if (date != null) {
					request.setAttribute(DATE_FIELD, new DateTime(date).toDate());   	
				}              
			} catch (ParseException e) {
				log.warn("Invalid date string supplied: " + dateString);
			}        	
		}		
		
		log.info("Looking for embargoed field");
		if (request.getParameter(EMBARGO_DATE_FIELD) != null && !request.getParameter(EMBARGO_DATE_FIELD).isEmpty()) {
			request.setAttribute(EMBARGO_DATE_FIELD, parseEmbargoDate((String) request.getParameter(EMBARGO_DATE_FIELD)));
		}
		
		// TODO Test coverage	Deprecated?	
        if (request.getParameter("publisher") != null && !request.getParameter("publisher").equals("")) {
            final String publisherUrlWords = request.getParameter("publisher");
            Resource publisher = resourceDAO.getPublisherByUrlWords(publisherUrlWords);
            if (publisher != null) {            
                request.setAttribute("publisher", publisher);
            }
        }
        
    	tagsParameterFilter.filter(request);		    	
    	resourceParameterFilter.filter(request);
    	
		if (request.getParameter("feed") != null) {
			final String feedParameter = request.getParameter("feed");
			log.info("Loading feed by url words: " + feedParameter);		
    		final Resource feed = resourceDAO.loadFeedByUrlWords(feedParameter);
    		if (feed != null) {           	
    			log.info("Found feed: " + feed.getName());
    			request.setAttribute("feedAttribute", feed);        				
    		} else {
    			log.info("Could not find feed: " + feed);
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

	private Date parseEmbargoDate(String dateString) {
		for (SimpleDateFormat dateFormat : supportedEmbargoDateFormats) {
			try {
				Date date = dateFormat.parse(dateString);
				if (date != null) {
					return date;
				}		
			} catch (ParseException e) {
				log.info("Supplied embargo date '" + dateString + "' did not match date format: " + dateFormat.toPattern());
			}
		}
		
		Date date = new StringToTime(dateString);
		if (date != null) {
			return date;
		}
		
		log.warn("User supplied embargo date '" + dateString + "' could not be parsed");		
		return null;
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
