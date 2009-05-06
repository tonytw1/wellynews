package nz.co.searchwellington.controllers.models;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.controllers.UrlBuilder;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.ResourceRepository;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

public class PublisherModelBuilder extends AbstractModelBuilder implements ModelBuilder {
	
	Logger logger = Logger.getLogger(PublisherModelBuilder.class);
	
	private ResourceRepository resourceDAO;
	private RssUrlBuilder rssUrlBuilder;
	private UrlBuilder urlBuilder;

	
	public PublisherModelBuilder(ResourceRepository resourceDAO, RssUrlBuilder rssUrlBuilder, UrlBuilder urlBuilder) {		
		this.resourceDAO = resourceDAO;
		this.rssUrlBuilder = rssUrlBuilder;
		this.urlBuilder = urlBuilder;
	}


	public boolean isValid(HttpServletRequest request) {
        Tag tag = (Tag) request.getAttribute("tag");
        Website publisher = (Website) request.getAttribute("publisher");   
        boolean isPublisherPage = publisher != null && tag == null;
        return isPublisherPage;
	}
	
	
	public ModelAndView populateContentModel(HttpServletRequest request, boolean showBroken) {				
		if (isValid(request)) {
			logger.info("Building publisher page model");
			Website publisher = (Website) request.getAttribute("publisher");
			int page = getPage(request);
			return populatePublisherPageModelAndView(publisher, showBroken, page);
		}
		return null;
	}
	
	
	public void populateExtraModelConent(HttpServletRequest request, boolean showBroken, ModelAndView mv) {		
	}

		
	private ModelAndView populatePublisherPageModelAndView(Website publisher, boolean showBroken, int page) {
		ModelAndView mv = new ModelAndView();
		mv.addObject("heading", publisher.getName());
		mv.addObject("description", publisher.getName());
		mv.addObject("link", urlBuilder.getPublisherUrl(publisher));
		
		int startIndex = getStartIndex(page);
		final int mainContentTotal = resourceDAO.getPublisherNewsitemsCount(publisher, showBroken);
		if (mainContentTotal > 0) {
			mv.addObject("main_content_total", mainContentTotal);
			final List<Newsitem> publisherNewsitems = resourceDAO.getPublisherNewsitems(publisher, MAX_NEWSITEMS, showBroken, startIndex);		
			mv.addObject("main_content", publisherNewsitems);
			setRss(mv, rssUrlBuilder.getRssTitleForPublisher(publisher), rssUrlBuilder.getRssUrlForPublisher(publisher));
			mv.addObject("publisher", publisher);
		}		
		mv.setViewName("browse");
		return mv;
	}
	
}
