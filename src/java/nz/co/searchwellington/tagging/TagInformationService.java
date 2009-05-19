package nz.co.searchwellington.tagging;

import java.util.List;

import nz.co.searchwellington.model.Resource;

import org.apache.log4j.Logger;

public class TagInformationService {
    
    Logger log = Logger.getLogger(TagInformationService.class);
        
    public int getPercentageUntagged(List<Resource> recentNewsitems) {
    	int untagged = 0;
		if (recentNewsitems.size() > 0) {
			for (Resource newsitem : recentNewsitems) {
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
        
}
