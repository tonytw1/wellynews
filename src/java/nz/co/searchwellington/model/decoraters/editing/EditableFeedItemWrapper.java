package nz.co.searchwellington.model.decoraters.editing;

import nz.co.searchwellington.model.PublishedResource;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.utils.UrlFilters;

// TODO pull interface on FeedItem.
public class EditableFeedItemWrapper extends EditablePublishedResourceWrapper {

	private Resource localCopy;
	private boolean isSupressed;
	private int feedId;
	private int itemNumber;

	
	public EditableFeedItemWrapper(PublishedResource resource, Resource localCopy, boolean isSupressed, int feedId, int itemNumber) {
		super(resource);
		this.resource = resource;
		this.localCopy = localCopy;
		this.isSupressed = isSupressed;
		this.feedId = feedId;
		this.itemNumber = itemNumber;
	}
	

	public String getEditUrl() {
		if (localCopy != null) {
		    return "edit/edit?resource=" + localCopy.getId(); 
		} else {
		    return "edit/accept?feed=" + feedId + "&item=" + itemNumber;
		}
	}
	
	public String getEditLabel() {
		if (localCopy != null) {
		    return "Edit local copy";
		} else {
		    return "Accept feed item";
		}
	}
	
    
	public boolean isSuppressed() {
		return isSupressed;
	}
	
	public String getSupressUrl() {
		if (!isSupressed) {
			return "supress/supress?url=" + UrlFilters.encode(getUrl());		
		}
		return null;
	}
	
	
	public String getUnsupressUrl() {
		if (isSupressed) {
			return "supress/unsupress?url=" + UrlFilters.encode(getUrl());	
		}
		return null;
	}
    
    
    
    public String getDeleteAndSupressUrl() {
        if (getUrl() != null && !isSupressed) {
            return "edit/deleteandsupress?resource=" + getId();            
        }
        return null;
    }
    
    
     // TODO Move this to an edit decorator.
    /*
    if (loggedInUser != null) {
        int itemCounter = 1;
        for (HashMap item : items) {
            
            String url = (String) item.get("url");
            boolean urlIsSupressed = supressionDAO.isSupressed(url);
            Resource localCopy = resourceDAO.loadResourceByUrl(url);
            if (localCopy == null) {
               
            } else {
                            
                             
            }
            
            if (url != null) {
                if (!urlIsSupressed) {
                    item.put("supressUrl", "supress/supress?url=" + url);
                } else {
                    item.put("supressed", 1);
                    item.put("unsupressUrl", "supress/unsupress?url=" + url);
                }
            }
            itemCounter++;
        }
        populateAcceptanceDecisions(resources, items, feed);
    }
    */
    
    
    
//     @SuppressWarnings("unchecked")
//        private void populateAcceptanceDecisions(List<Resource> resources, List<HashMap> items, Feed feed) {   
//            Iterator<HashMap> itemsIterator = items.iterator();
//            for (Resource resource : resources) {
//                HashMap item = itemsIterator.next();                           
//                
//                List<String> acceptanceProblems = feedAcceptanceDecider.getAcceptanceErrors(resource, feed.getAcceptancePolicy());
//                boolean accepting = acceptanceProblems.size() == 0;
//                if (!accepting) {
//                    item.put("acceptanceErrors", acceptanceProblems);
//                }
//            }
//        }
//    
    
    
    
	
	 
}
