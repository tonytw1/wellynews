package nz.co.searchwellington.views;

import java.util.List;

import nz.co.searchwellington.model.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
public class ContentDedupingService {
	
	private static Logger log = Logger.getLogger(ContentDedupingService.class);

    public List<Resource> dedupeNewsitems(List<Resource> latestNewsitems, List<Resource> commentedNewsitems) {
    	log.debug("Called with " + latestNewsitems.size() + " main content items and " + commentedNewsitems.size() + " commented news items");
    	final List <Resource> depuded  = Lists.newArrayList(latestNewsitems);
    	depuded.removeAll(commentedNewsitems);
    	if (depuded.size() < latestNewsitems.size()) {
    		log.debug("Removed " + (latestNewsitems.size() - depuded.size()) + " duplicates");
    	}
    	log.debug("Returning " + depuded.size() + " main content items");
    	return depuded;
    }

}
