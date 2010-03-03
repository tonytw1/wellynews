package nz.co.searchwellington.controllers;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.model.ArchiveLink;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.ConfigRepository;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.urls.UrlBuilder;

import org.springframework.web.servlet.ModelAndView;


public class BrowseController extends BaseMultiActionController {

    private UrlBuilder urlBuilder;
    private ShowBrokenDecisionService showBrokenDecisionService;
 
	public BrowseController(UrlStack urlStack, ConfigRepository configDAO, UrlBuilder urlBuilder, ShowBrokenDecisionService showBrokenDecisionService, ContentRetrievalService contentRetrievalService) {       
        this.urlStack = urlStack;
        this.configDAO = configDAO;
        this.urlBuilder = urlBuilder;
        this.showBrokenDecisionService = showBrokenDecisionService;
        this.contentRetrievalService = contentRetrievalService;
	}
	
		
    public ModelAndView archive(HttpServletRequest request, HttpServletResponse response) throws IOException {       
        ModelAndView mv = new ModelAndView();           
        populateLocalCommon(request, mv);
        
        if (request.getAttribute("month") != null) {
            Date month = (Date) request.getAttribute("month");
            mv.addObject("archive_month", new ArchiveLink(month, 0));
            final List<Resource> newsitemsForMonth = contentRetrievalService.getNewsitemsForMonth(month);            
            mv.addObject("main_content", newsitemsForMonth);
			populateSecondaryLatestNewsitems(mv);
            mv.addObject("used_tags_description", "Most used tags during this month.");
            
            List<ArchiveLink> archiveLinks = contentRetrievalService.getArchiveMonths();
            
            populateSecondaryLatestNewsitems(mv);
            mv.setViewName("archivePage");
            return mv;
        }
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return null;        
    }



    @SuppressWarnings("unchecked")
    private void populateLocalCommon(HttpServletRequest request, ModelAndView mv) {
        urlStack.setUrlStack(request);
        mv.getModel().put("top_level_tags", contentRetrievalService.getTopLevelTags());
        populateArchiveLinks(mv, showBrokenDecisionService.shouldShowBroken(), contentRetrievalService.getArchiveMonths());
    }
	
    
    private void populateArchiveLinks(ModelAndView mv, boolean showBroken, List<ArchiveLink> archiveMonths) {                        
        final int MAX_BACK_ISSUES = 6;
        if (archiveMonths.size() <= MAX_BACK_ISSUES) {
            mv.addObject("archive_links", archiveMonths);
        } else {
            mv.addObject("archive_links", archiveMonths.subList(0, MAX_BACK_ISSUES));           
        }        
        populateArchiveStatistics(mv);
    }
    
    // TODO duplicated with index model builder
    private void populateArchiveStatistics(ModelAndView mv) {
		Map<String, Integer> archiveStatistics = contentRetrievalService.getArchiveStatistics();
		if (archiveStatistics != null) {
			mv.addObject("site_count",  archiveStatistics.get("W"));
			mv.addObject("newsitem_count",  archiveStatistics.get("N"));
			mv.addObject("feed_count", archiveStatistics.get("F"));
		}
	}

    
    
}
