package nz.co.searchwellington.controllers.models.helpers;

import java.util.List;
import java.util.Map;

import nz.co.searchwellington.model.ArchiveLink;
import nz.co.searchwellington.repositories.ContentRetrievalService;

import org.springframework.web.servlet.ModelAndView;

public class ArchiveLinksService {
	
		
	private ContentRetrievalService contentRetrievalService;
	
	
	public ArchiveLinksService(ContentRetrievalService contentRetrievalService) {
		this.contentRetrievalService = contentRetrievalService;
	}


	public void populateArchiveLinks(ModelAndView mv, List<ArchiveLink> archiveMonths) {                        
        final int MAX_BACK_ISSUES = 6;
        if (archiveMonths.size() <= MAX_BACK_ISSUES) {
            mv.addObject("archive_links", archiveMonths);
        } else {
        	
            mv.addObject("archive_links", archiveMonths.subList(0, MAX_BACK_ISSUES));           
        }        
        populateArchiveStatistics(mv);
    }
	
	
    private void populateArchiveStatistics(ModelAndView mv) {
		Map<String, Integer> archiveStatistics = contentRetrievalService.getArchiveStatistics();
		if (archiveStatistics != null) {
			mv.addObject("site_count",  archiveStatistics.get("W"));
			mv.addObject("newsitem_count",  archiveStatistics.get("N"));
			mv.addObject("feed_count", archiveStatistics.get("F"));
		}
	}
	
}
