 package nz.co.searchwellington.controllers.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.repositories.ContentRetrievalService;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

public class ArchiveModelBuilder extends AbstractModelBuilder implements ModelBuilder {
	
	Logger log = Logger.getLogger(ArchiveModelBuilder.class);
    	
	private ContentRetrievalService contentRetrievalService;	

	public ArchiveModelBuilder(ContentRetrievalService contentRetrievalService) {		
		this.contentRetrievalService = contentRetrievalService;
	}

	
	public boolean isValid(HttpServletRequest request) {
		return request.getPathInfo().matches("^/archive/*/*");
	}

	
	public ModelAndView populateContentModel(HttpServletRequest request, boolean showBroken) {
		if (isValid(request)) {
			log.info("Building archive page model");			
			Date month = getArchiveDateFromPath(request.getPathInfo());
			if (month != null) {
	            log.info("Archive month is: " + month);
	            
				ModelAndView mv = new ModelAndView();				
				mv.addObject("heading", "ARCHIVE");	// TODO month name        		
				mv.addObject("description", "The most recently submitted website listings.");
				
				mv.addObject("main_content", contentRetrievalService.getNewsitemsForMonth(month));
				mv.setViewName("archivePage");
				return mv;
			}
		}
		return null;
	}

	
	public void populateExtraModelConent(HttpServletRequest request, boolean showBroken, ModelAndView mv) {		
	}
	
	
	// TODO duplicated from RequestFilter
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
