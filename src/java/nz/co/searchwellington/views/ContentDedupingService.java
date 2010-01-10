package nz.co.searchwellington.views;

import java.util.ArrayList;
import java.util.List;

import nz.co.searchwellington.model.Resource;

import org.apache.log4j.Logger;

public class ContentDedupingService {
	
	Logger log = Logger.getLogger(ContentDedupingService.class);

    public List<Resource> dedupeNewsitems(List<Resource> latestNewsitems, List<Resource> commentedNewsitems) { 
    	List <Resource> depuded  = new ArrayList<Resource>(latestNewsitems);
    	depuded.removeAll(commentedNewsitems);
    	if (depuded.size() < latestNewsitems.size()) {
    		log.debug("Removed " + (latestNewsitems.size() - depuded.size()) + " duplicates");
    	}
    	return depuded;
    }

}
