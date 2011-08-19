 package nz.co.searchwellington.controllers.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.controllers.models.helpers.ArchiveLinksService;
import nz.co.searchwellington.dates.DateFormatter;
import nz.co.searchwellington.model.ArchiveLink;
import nz.co.searchwellington.repositories.ContentRetrievalService;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

public class ArchiveModelBuilder extends AbstractModelBuilder implements ModelBuilder {
	
	static Logger log = Logger.getLogger(ArchiveModelBuilder.class);
    	
	private ContentRetrievalService contentRetrievalService;
	private ArchiveLinksService archiveLinksService;

	
	public ArchiveModelBuilder(ContentRetrievalService contentRetrievalService,
			ArchiveLinksService archiveLinksService) {
		this.contentRetrievalService = contentRetrievalService;
		this.archiveLinksService = archiveLinksService;
	}

	
	@Override
	public boolean isValid(HttpServletRequest request) {
		return request.getPathInfo().matches("^/archive/.*?/.*?$");
	}

	
	@Override
	public ModelAndView populateContentModel(HttpServletRequest request) {
		if (isValid(request)) {
			log.info("Building archive page model");			
			Date month = getArchiveDateFromPath(request.getPathInfo());
			if (month != null) {
	            log.info("Archive month is: " + month);
	            final String monthLabel = new DateFormatter().formatDate(month, DateFormatter.MONTH_YEAR_FORMAT);
	            
				ModelAndView mv = new ModelAndView();				
				mv.addObject("heading", monthLabel);				
				mv.addObject("description", "Archived newsitems for the month of " + monthLabel);				
				mv.addObject("main_content", contentRetrievalService.getNewsitemsForMonth(month));				
				return mv;
			}
		}
		return null;
	}

	
	@Override
	public void populateExtraModelConent(HttpServletRequest request, ModelAndView mv) {
		Date month = getArchiveDateFromPath(request.getPathInfo());
        List<ArchiveLink> archiveLinks = contentRetrievalService.getArchiveMonths();
		populateNextAndPreviousLinks(mv, month, archiveLinks);
		archiveLinksService.populateArchiveLinks(mv, archiveLinks);
	}
	
	
	@Override
	public String getViewName(ModelAndView mv) {
		return "archivePage";
	}
	
	
    private void populateNextAndPreviousLinks(ModelAndView mv, Date month, List<ArchiveLink> archiveLinks) {
        ArchiveLink selected = null;
        for (ArchiveLink link : archiveLinks) {            
            if (link.getMonth().equals(month)) {
                selected = link;
            }                
        }
        
        if (selected != null) {
            final int indexOf = archiveLinks.indexOf(selected);                
            if (indexOf < archiveLinks.size()-1) {
                ArchiveLink previous = archiveLinks.get(indexOf+1);
                mv.addObject("next_page", previous);                
            }
            if (indexOf > 0) {
                ArchiveLink next = archiveLinks.get(indexOf-1);
                mv.addObject("previous_page", next);
            }                
        }
    }
    

    private Date getArchiveDateFromPath(String path) {
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
		
}
