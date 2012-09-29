package nz.co.searchwellington.controllers;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.controllers.submission.SubmissionProcessor;
import nz.co.searchwellington.controllers.submission.UrlProcessor;
import nz.co.searchwellington.geocoding.NominatimGeocodingService;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.Image;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.PublishedResource;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.HandTaggingDAO;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.TagDAO;
import nz.co.searchwellington.utils.UrlCleaner;
import nz.co.searchwellington.utils.UrlFilters;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.ecs.xhtml.s;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysql.jdbc.log.Slf4JLogger;

public class SubmissionProcessingService {

	private static final String REQUEST_TITLE_NAME = "title";
	private static final String REQUEST_DATE_NAME = "date";
    private static final String REQUEST_DESCRIPTION_NAME = "description";
    private static final String REQUEST_SELECTED_GEOCODE = "selectedGeocode";
	private static final String REQUEST_EMBARGO_DATE_NAME = "embargo_date";
    
    private Logger log = Logger.getLogger(SubmissionProcessingService.class);
        
    private UrlCleaner urlCleaner;
    private NominatimGeocodingService nominatimGeocodeService;
    private TagDAO tagDAO;
    private HandTaggingDAO tagVoteDAO;
	private ResourceRepository resourceDAO;
	
	@Autowired
	public SubmissionProcessingService(UrlCleaner urlCleaner, NominatimGeocodingService NominatimGeocodingService, TagDAO tagDAO, HandTaggingDAO tagVoteDAO, ResourceRepository resourceDAO) {
		this.urlCleaner = urlCleaner;
		this.nominatimGeocodeService = NominatimGeocodingService;
		this.tagDAO = tagDAO;
		this.tagVoteDAO = tagVoteDAO;
		this.resourceDAO = resourceDAO;
	}
	
	public void processTitle(HttpServletRequest req, Resource editResource) {           
        if (req.getParameter(REQUEST_TITLE_NAME) != null) {
            String title = new String(req.getParameter(REQUEST_TITLE_NAME));
            title = UrlFilters.trimWhiteSpace(title);
            title = UrlFilters.stripHtml(title);            
            final String flattenedTitle = UrlFilters.lowerCappedSentence(title);           
            if (!flattenedTitle.equals(title)) {
                title = flattenedTitle;   
                log.info("Flatten capitalised sentence to '" + title + "'");
            }
            editResource.setName(title);
        }
    }
	
	public void processUrl(HttpServletRequest request, Resource editResource) {		
		SubmissionProcessor urlProcessor = new UrlProcessor(urlCleaner);	// TODO inject
		urlProcessor.process(request, editResource);
	}
		
	public void processImage(HttpServletRequest request, Newsitem editResource, User loggedInUser) {
		Image image = (Image) request.getAttribute("image");
    	editResource.setImage(image);
    }
	
	public Geocode processGeocode(HttpServletRequest req) {      
		log.info("Starting processing of geocode.");
		if (req.getParameter(REQUEST_SELECTED_GEOCODE) != null) {
	    	final String selectedGeocode = new String(req.getParameter(REQUEST_SELECTED_GEOCODE));
	        log.info("Found selected geocode: " + selectedGeocode);
	        if (selectedGeocode != null && !selectedGeocode.trim().equals("")) {
	        	
	        	final long osmId = Long.parseLong(selectedGeocode.split("/")[0]);
	            final String osmType = selectedGeocode.split("/")[1];
	            
	            final Geocode resolvedGeocode = nominatimGeocodeService.resolveAddress(osmType, osmId);
	            log.info("Selected geocode " + selectedGeocode + " resolved to: " + resolvedGeocode);
	            return resolvedGeocode;
	        }
		}
		return null;
	}
	
	public void processDate(HttpServletRequest request, Resource editResource) {
        editResource.setDate((Date) request.getAttribute(REQUEST_DATE_NAME));
        if (editResource.getDate() == null && editResource.getId() == 0) {
            editResource.setDate(Calendar.getInstance().getTime());
        }
    }
	
	public void processEmbargoDate(HttpServletRequest request, Resource editResource) {
		editResource.setEmbargoedUntil((Date) request.getAttribute(REQUEST_EMBARGO_DATE_NAME));
	}
	
    public void processDescription(HttpServletRequest request, Resource editResource) {
        String description = request.getParameter(REQUEST_DESCRIPTION_NAME);
        if (description != null) {
        	description = StringEscapeUtils.unescapeHtml(description);
        	description = UrlFilters.stripHtml(description);
        }
        editResource.setDescription(description);
    }
    
    public void processHeld (HttpServletRequest request, Resource editResource) {
    	if (request.getParameter("has_held") != null) {
    		if (request.getParameter("held") != null) {
    			editResource.setHeld(true);
    			return;
    		}
    		editResource.setHeld(false);
    	}
    	return;
    }
        
	public void processTags(HttpServletRequest request, Resource editResource, User user) {
		log.info("Processing tags");
    	if (request.getParameter("has_tag_select") != null) {
    		processTagSelect(request, editResource, user);    		
    	}
        if (request.getParameter("additional_tags") != null) {
            processAdditionalTags(request, editResource, user);                   
        } else {
        	log.debug("No additional tag string found.");
        }    
        trimTags(editResource, 4);               
    }
	
	@SuppressWarnings("unchecked")
	private void processTagSelect(HttpServletRequest request, Resource editResource, User user) {
		log.info("Processing tag select");
		if (request.getAttribute("tags") != null) {        	
			List<Tag> requestTagsList = (List <Tag>) request.getAttribute("tags");
			log.debug("Found tags on request: " + requestTagsList);
			Set<Tag> tags = new HashSet<Tag>(requestTagsList);
			log.info("Found " + tags.size() + " tags on the request");			
			tagVoteDAO.setUsersTagVotesForResource(editResource, user, tags);			
			
		} else {
			log.info("No tags request attribute seen; clearing users tag votes");
			tagVoteDAO.setUsersTagVotesForResource(editResource, user, new HashSet<Tag>());
		}
	}
    
	public void processPublisher(HttpServletRequest request, Resource editResource) {
    	boolean isPublishedResource = editResource instanceof PublishedResource;
    	if (isPublishedResource) {
    		if (request.getParameter("publisherName") != null && !request.getParameter("publisherName").equals("")) {
    			final String publisherName = request.getParameter("publisherName");
    			Website publisher = (Website) resourceDAO.getPublisherByName(publisherName);
    			if (publisher != null) {
    				log.info("Found publisher: " + publisher.getName());
    				((PublishedResource) editResource).setPublisher(publisher);
    			}
    		}
    	}
    }
	
	
	public void processAcceptance(HttpServletRequest request, Resource editResource, User loggedInUser) {
		if (editResource instanceof Newsitem) {
			if (request.getParameter("acceptedFromFeed") != null && !request.getParameter("acceptedFromFeed").equals("")) {			
				final String acceptedFromFeedUrlWords = request.getParameter("acceptedFromFeed");
				log.info("Item was accepted from a feed with url words: " + acceptedFromFeedUrlWords);
				Feed feed = resourceDAO.loadFeedByUrlWords(acceptedFromFeedUrlWords);
				if (feed != null) {
					log.info("Setting accepted from feed to: " + feed.getName());
					((Newsitem) editResource).setFeed(feed);
					((Newsitem) editResource).setAcceptedBy(loggedInUser);
					((Newsitem) editResource).setAccepted(new DateTime().toDate());	// TODO not test covered
				}
			}
		}
	}
	
    
    private void processAdditionalTags(HttpServletRequest request, Resource editResource, User user) {
        String additionalTagString = request.getParameter("additional_tags").trim();
        log.debug("Found additional tag string: " + additionalTagString);
        String[] fields = additionalTagString.split(",");
        if (fields.length > 0) {
            
            for (int i = 0; i < fields.length; i++) {
                String field = fields[i].trim();
                    
                String displayName = field;
                field = cleanTagName(field);
                log.debug("Wants additional tag: " + field);
                                
                if (isValidTagName(field)) {
                    Tag existingTag = tagDAO.loadTagByName(field);
                    if (existingTag == null) {
                        log.debug("Tag '" + field + "' is a new tag. Needs to be created.");                                
                      
                        Tag newTag = tagDAO.createNewTag();                                                
                        newTag.setName(field);                       
                        newTag.setDisplayName(displayName);                        
                        tagDAO.saveTag(newTag);
                        tagVoteDAO.addTag(user, newTag, editResource);
                        
                    } else {
                        log.debug("Found an existing tag in the additional list: " + existingTag.getName() + "; adding.");
                        tagVoteDAO.addTag(user, existingTag, editResource);
                                                     
                    }
                } else {
                    log.debug("Ignoring invalid tag name: " + field);
                }
            }                    
        }
    }
    
    private String cleanTagName(String field) {
    	field = StringUtils.strip(field);
        field = StringUtils.remove(field, " ");    
        return field.toLowerCase().trim();
    }
    
    protected void trimTags(Resource editResource, int maxTags) { // TODO reimplement
   /*     if (editResource.getTags().size() > maxTags) {
            Set <Tag> tagsToKeep = new HashSet<Tag>();
            int counter = 0;
            for (Iterator<Tag> iter = editResource.getTags().iterator(); iter.hasNext();) {
                Tag toKeep= iter.next();
                counter++;
                if (counter <= 4) {
                    tagsToKeep.add(toKeep);
                }
            }
            editResource.setTags(tagsToKeep);
    	}
    */ 
    }

    protected boolean isValidTagName(String field) {
        return field != null && field.length() > 0 && field.matches("[a-zA-Z0-9]*");
    }
    
}
