package nz.co.searchwellington.controllers.models;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.controllers.LoggedInUserFilter;

import org.apache.log4j.Logger;
import org.apache.lucene.index.CorruptIndexException;
import org.springframework.web.servlet.ModelAndView;

import com.sun.syndication.io.FeedException;

public class ContentModelBuilderService {

	Logger logger = Logger.getLogger(ContentModelBuilderService.class);
		
	private ModelBuilder[] modelBuilders;
	private LoggedInUserFilter loggedInUserFilter;
	
	public ContentModelBuilderService(LoggedInUserFilter loggedInUserFilter, ModelBuilder... modelBuilders) {
		this.loggedInUserFilter = loggedInUserFilter;
		this.modelBuilders = modelBuilders;
	}

	public ModelAndView populateContentModel(HttpServletRequest request) throws IOException, CorruptIndexException, FeedException {
		logger.info("Building content model");
		boolean showBroken = false;	
		if (loggedInUserFilter.getLoggedInUser() != null) {
			showBroken = true;
		}
		for (int i = 0; i < modelBuilders.length; i++) {
			ModelBuilder modelBuilder = modelBuilders[i];
			logger.info("Checking " + modelBuilder);
			if (modelBuilder.isValid(request)) {
				logger.info("Using " + modelBuilder);
				ModelAndView mv = modelBuilder.populateContentModel(request, showBroken);
				modelBuilder.populateExtraModelConent(request, showBroken, mv);
				return mv;
			}
		}		
        return null;
	}
	
}
