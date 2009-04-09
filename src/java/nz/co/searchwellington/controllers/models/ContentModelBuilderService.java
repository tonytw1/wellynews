package nz.co.searchwellington.controllers.models;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.controllers.RelatedTagsService;
import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.controllers.UrlBuilder;
import nz.co.searchwellington.repositories.ResourceRepository;

import org.apache.log4j.Logger;
import org.apache.lucene.index.CorruptIndexException;
import org.springframework.web.servlet.ModelAndView;

import com.sun.syndication.io.FeedException;

public class ContentModelBuilderService {




	Logger logger = Logger.getLogger(ContentModelBuilderService.class);
	
	private ResourceRepository resourceDAO;
	private RssUrlBuilder rssUrlBuilder;
	private UrlBuilder urlBuilder;
	
	private ModelBuilder[] modelBuilders;
	
	
	
	public ContentModelBuilderService(ResourceRepository resourceDAO, RssUrlBuilder rssUrlBuilder, UrlBuilder urlBuilder) {		
		this.resourceDAO = resourceDAO;
		this.rssUrlBuilder = rssUrlBuilder;	
		this.urlBuilder = urlBuilder;
		
		// TODO inject
		modelBuilders = new ModelBuilder[4];
		modelBuilders[0] = new PublisherModelBuilder(resourceDAO, rssUrlBuilder, urlBuilder);
		modelBuilders[1] = new PublisherTagCombinerModelBuilder(resourceDAO, rssUrlBuilder, urlBuilder);
		modelBuilders[2] = new TagModelBuilder(resourceDAO, rssUrlBuilder, urlBuilder, new RelatedTagsService(resourceDAO));
		modelBuilders[3] = new TagCombinerModelBuilder(resourceDAO, rssUrlBuilder, urlBuilder,  new RelatedTagsService(resourceDAO));			
	}


	public ModelAndView populateContentModel(HttpServletRequest request) throws IOException, CorruptIndexException, FeedException {
		logger.info("Building content model");
		boolean showBroken = false;	
				
		for (int i = 0; i < modelBuilders.length; i++) {
			ModelBuilder modelBuilder = modelBuilders[i];
			if (modelBuilder.isValid(request)) {
				ModelAndView mv = modelBuilder.populateContentModel(request, showBroken);
				modelBuilder.populateExtraModelConent(request, showBroken, mv);
				return mv;
			}
		}
		
        return null;
	}
	
}
