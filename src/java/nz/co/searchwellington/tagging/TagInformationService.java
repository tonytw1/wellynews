package nz.co.searchwellington.tagging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.TagContentCount;
import nz.co.searchwellington.model.Tag;

import org.apache.log4j.Logger;

public class TagInformationService {
    
    
    Logger log = Logger.getLogger(TagInformationService.class);
    
    
    public List<TagContentCount> getNewsitemsMostUsedTags(List<Newsitem> newsitems, int maxtags) {                
        Map<Tag, Integer> usedTags = new HashMap<Tag, Integer>();        
        for (Newsitem newsitem : newsitems) { 
            Set<Tag> itemTags = newsitem.getTags();   
            registerTags(usedTags, itemTags);                  
            if (newsitem.getPublisher() != null) {
            	registerTags(usedTags, newsitem.getPublisher().getTags());
            }
        }        
        log.debug("Found " + usedTags.size() + " unique tags in newsitems.");       
        return sortAndLimitResults(maxtags, usedTags);        
    }

    
    public int getPercentageUntagged(List<Newsitem> recentNewsitems) {
    	int untagged = 0;
		if (recentNewsitems.size() > 0) {
			for (Newsitem newsitem : recentNewsitems) {
				if (newsitem.getTags().size() == 0) {
					untagged = untagged + 1;
				}            
			}
			log.info("Untagged = " + untagged + " / " + recentNewsitems.size());
			int percentageUntagged = calculatePercentage(recentNewsitems.size(), untagged);
			return percentageUntagged;
		}
		return 0;
    }


	protected int calculatePercentage(int total, int untagged) {
		float percentageTagged = (new Float(untagged) / new Float(total) * 100);	
		return Math.round(percentageTagged);
	}
    
    
    private void registerTags(Map<Tag, Integer> usedTags, Set<Tag> itemTags) {
        for (Tag tag : itemTags) {
            Integer tagCount = usedTags.get(tag);
            if (tagCount != null) {
                tagCount = tagCount + 1;
                usedTags.put(tag, tagCount);
            } else {
                tagCount = new Integer(1);
                usedTags.put(tag, tagCount);
            }
            usedTags.put(tag, tagCount);
        }
    }
    
    
    private List<TagContentCount> sortAndLimitResults(int maxtags, Map<Tag, Integer> usedTags) {               
        List<TagContentCount> links = new ArrayList<TagContentCount>();
        for (Tag tag : usedTags.keySet()) {
            links.add(new TagContentCount(tag, usedTags.get(tag)));
        }
        
        Collections.sort(links);               
        if (links.size() <= maxtags) {
            return links;
        } else {
            return links.subList(0, maxtags);
        }                   
    }

    
}
