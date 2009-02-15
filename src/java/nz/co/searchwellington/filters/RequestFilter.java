package nz.co.searchwellington.filters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.model.CalendarFeed;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.ResourceRepository;

import org.apache.log4j.Logger;

public class RequestFilter {

    Logger log = Logger.getLogger(RequestFilter.class);
    
    
    
    protected ResourceRepository resourceDAO;


    public RequestFilter(ResourceRepository resourceDAO) {
        this.resourceDAO = resourceDAO;     
    }

    public void loadAttributesOntoRequest(HttpServletRequest request) {
        
        // old redirects support.
        if (request.getParameter("category") != null) {
            try {
                int categoryId = Integer.parseInt(request.getParameter("category"));
                if (categoryId > 0) {
                    Tag tag = resourceDAO.loadTagById(categoryId);            
                    request.setAttribute("tag", tag);
                }
            } catch (NumberFormatException e) {
                log.warn("category parmeter was not a valid number: " + request.getParameter("category"));
            }
        }
        
        
        
        if (request.getParameter("date") != null) {

            SimpleDateFormat sdfInput = new SimpleDateFormat("d MMM yyyy");
            try {
                Date date = sdfInput.parse(request.getParameter("date"));
                request.setAttribute("date", date);
                
            } catch (ParseException e) {
                log.warn("Invalid date input; defaulting to null.");
            }
   
        }
        
  
        loadResourceFromRequestParameter(request);
             
        
        List<String> tagNames = getTagNamesFromPath(request.getPathInfo());
        log.debug("Found " + tagNames.size() + " tag names on request.");
        if (tagNames.size() > 0) {
            List<Tag> tags = new ArrayList<Tag>();
            for (String tagname : tagNames) {
                if (tagname != null) {
                    Tag tag = resourceDAO.loadTagByName(tagname);
                    if (tag != null) {
                        tags.add(tag);
                    } else {
                        log.warn("Failed to load tagname: " + tagname);
                    }
                }
            }
            
            if (tags.size() == 1) {
                Tag mainTag = tags.get(0);
                request.setAttribute("tag", mainTag);
            }
            
            request.setAttribute("tags", tags);
        }
        
        //      TODO why is this still using ints?
        // TODO duplication.
        if (request.getParameter("tags") != null) {
           String[] tagIds = request.getParameterValues("tags");
           List <Tag> tags = new ArrayList <Tag>();
           for (int i = 0; i < tagIds.length; i++) {             
               String tagIdString = tagIds[i];
               int tagID = Integer.parseInt(tagIdString);
               if (tagID > 0) {          
                   Tag tag = resourceDAO.loadTagById(tagID);
                   tags.add(tag);
               }
           }           
           request.setAttribute("tags", tags);
        }
        
        
       // TODO depricate be using a url tagname instead of a form parameter.
       if (request.getParameter("tag") != null) {
           String tagName = request.getParameter("tag");        
           Tag tag = resourceDAO.loadTagByName(tagName);             
              request.setAttribute("tag", tag);            
       }
        
        
        
        if (request.getParameter("parent") != null) {
            String tagName = request.getParameter("parent");
            Tag tag = resourceDAO.loadTagByName(tagName);
            request.setAttribute("parent_tag", tag); 
        }
        
        
        
        
        
        
        
        if (request.getParameter("publisher") != null) {
            final int publisherID = Integer.parseInt(request.getParameter("publisher"));
            if (publisherID > 0) {
                Resource publisher = resourceDAO.loadResourceById(publisherID);
                request.setAttribute("publisher", publisher);
            }
        }
        
        if (request.getParameter("feed") != null) {
            final int feedID = Integer.parseInt(request.getParameter("feed"));
            if (feedID > 0) {
                Resource feed = resourceDAO.loadResourceById(feedID);
                log.debug("Loaded feed object of id: " + feed.getId() + ", type: " + feed.getType());
                request.setAttribute("feedAttribute", feed);
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
             
        if (request.getPathInfo().startsWith("/archive/")) {            
            request.setAttribute("month", getArchiveDateFromPath(request.getPathInfo()));
        }
        
        
        if (request.getParameter("item") != null) {
            Integer item = Integer.parseInt(request.getParameter("item"));
            request.setAttribute("item", item);            
        }

    }

    private void loadResourceFromRequestParameter(HttpServletRequest request) {        
        Integer requestResourceID = parseResourceIDFromRequestParameter(request);        
        if (requestResourceID != null && requestResourceID > 0) {
            Resource resource = resourceDAO.loadResourceById(requestResourceID);               
            request.setAttribute("resource", resource);
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
    
    
   
    protected List<String> getTagNamesFromPath(String path) {

        List<String> tagNames = new ArrayList<String>();
        String[] fields = path.split("/");

        int tagIndex = -1;
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].equals("tag")) {
                tagIndex = i;
            }
        }

        boolean urlContainsTag = tagIndex > -1;
        if (urlContainsTag && fields.length > tagIndex + 1) {
            String tagsString = fields[tagIndex + 1];

            String[] tagnames = tagsString.split("\\+");
            for (int i = 0; i < tagnames.length; i++) {
                tagNames.add(tagnames[i]);
            }
        }
        return tagNames;
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

    
    
    public String getRssTypeFromRequest(String url) {
        if (url.equals("/rss/watchlist")) {
            return "L";
        } else if (url.equals("/rss/justin")) {
            return "W";
        }
        return null;
    }
    
    
    
}
