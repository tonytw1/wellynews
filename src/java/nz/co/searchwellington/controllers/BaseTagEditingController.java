package nz.co.searchwellington.controllers;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.User;

import org.apache.commons.lang.StringUtils;

public class BaseTagEditingController extends BaseMultiActionController {
    
    
    
    protected void processAdditionalTags(HttpServletRequest request, Resource editResource) {
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
                    Tag existingTag = resourceDAO.loadTagByName(field);
                    if (existingTag == null) {
                        log.debug("Tag '" + field + "' is a new tag. Needs to be created.");                                
                      
                        Tag newTag = resourceDAO.createNewTag();                                                
                        newTag.setName(field);                       
                        newTag.setDisplayName(displayName);                        
                        resourceDAO.saveTag(newTag);                                      
                        editResource.addTag(newTag);
                        
                    } else {
                        log.debug("Found an existing tag in the additional list: " + existingTag.getName() + "; adding.");
                        editResource.addTag(existingTag);                                
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

    protected void trimTags(Resource editResource, int maxTags) {
        if (editResource.getTags().size() > maxTags) {
            Set <Tag> tagsToKeep = new HashSet<Tag>();
            int counter = 0;
            for (Iterator iter = editResource.getTags().iterator(); iter.hasNext();) {
                Tag toKeep= (Tag) iter.next();
                counter++;
                if (counter <= 4) {
                    tagsToKeep.add(toKeep);
                }
            }
            editResource.setTags(tagsToKeep);
        }
    }

    protected boolean isValidTagName(String field) {
        return field != null && field.length() > 0 && field.matches("[a-zA-Z0-9]*");
    }

    protected void processTags(HttpServletRequest request, Resource editResource, User loggedInUser) {
        if (request.getAttribute("tags") != null) {
            List<Tag> requestTagsList = (List <Tag>) request.getAttribute("tags");
            Set<Tag> tags = new HashSet<Tag>(requestTagsList);
            editResource.setTags(tags);
        }
                   
        // Process additional tags
        if (request.getParameter("additional_tags") != null) {
            processAdditionalTags(request, editResource);                   
        } else {
            //log.info("No additional tag string found.");
        }
        // Don't let the greate unwashed drop stuff into the featured slot.
        Tag featuredTag = resourceDAO.loadTagByName("featured");
        boolean containsFeaturedTag = featuredTag != null && editResource.getTags().contains(featuredTag);
        if (containsFeaturedTag && loggedInUser == null) {
            //log.info("Removing featured tag from public submission.");
            editResource.getTags().remove(featuredTag);            
        }
                
        trimTags(editResource, 4);               
    }

}
