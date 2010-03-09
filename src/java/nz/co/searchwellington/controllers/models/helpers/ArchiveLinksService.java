package nz.co.searchwellington.controllers.models.helpers;

import java.util.Date;
import java.util.List;
import java.util.Map;

import nz.co.searchwellington.model.ArchiveLink;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.urls.UrlBuilder;

import org.springframework.web.servlet.ModelAndView;

public class ArchiveLinksService {
	
		
	private ContentRetrievalService contentRetrievalService;
	private UrlBuilder urlBuilder;
	
	
	public ArchiveLinksService(ContentRetrievalService contentRetrievalService,
			UrlBuilder urlBuilder) {
		this.contentRetrievalService = contentRetrievalService;
		this.urlBuilder = urlBuilder;
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
	
	
	public String makeArchiveUrl(Date dateOfLastNewsitem, List<ArchiveLink> archiveMonths) {    // TODO url builder should be able todo this with just the date!
    	ArchiveLink archiveLink = getArchiveLinkForDate(dateOfLastNewsitem, archiveMonths);
    	if (archiveLink != null) {
    		return urlBuilder.getArchiveLinkUrl(archiveLink);
    	}
    	return null;
	}
	
	
    private void populateArchiveStatistics(ModelAndView mv) {
		Map<String, Integer> archiveStatistics = contentRetrievalService.getArchiveStatistics();
		if (archiveStatistics != null) {
			mv.addObject("site_count",  archiveStatistics.get("W"));
			mv.addObject("newsitem_count",  archiveStatistics.get("N"));
			mv.addObject("feed_count", archiveStatistics.get("F"));
		}
	}
	
    
    private ArchiveLink getArchiveLinkForDate(Date dateOfLastNewsitem, List<ArchiveLink> archiveMonths) {	// TODO can't the url builder just to this?
		for (ArchiveLink monthLink : archiveMonths) {
			boolean monthMatches = monthLink.getMonth().getMonth() == dateOfLastNewsitem
					.getMonth() && monthLink.getMonth().getYear() == dateOfLastNewsitem.getYear();
			if (monthMatches) {
				return monthLink;
			}
		}
		return null;
	}
    
}
