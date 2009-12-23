package nz.co.searchwellington.controllers.models;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.controllers.RelatedTagsService;
import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.TagContentCount;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.urls.UrlBuilder;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

public class PublisherModelBuilder extends AbstractModelBuilder implements ModelBuilder {
	
	Logger logger = Logger.getLogger(PublisherModelBuilder.class);
	
	private ResourceRepository resourceDAO;
	private RssUrlBuilder rssUrlBuilder;
	private UrlBuilder urlBuilder;
	private RelatedTagsService relatedTagsService;

	
	public PublisherModelBuilder(ResourceRepository resourceDAO, RssUrlBuilder rssUrlBuilder, UrlBuilder urlBuilder, RelatedTagsService relatedTagsService) {		
		this.resourceDAO = resourceDAO;
		this.rssUrlBuilder = rssUrlBuilder;
		this.urlBuilder = urlBuilder;
		this.relatedTagsService = relatedTagsService;
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
		Website publisher = (Website) request.getAttribute("publisher"); 
		List<TagContentCount> relatedTagLinks = relatedTagsService.getRelatedLinksForPublisher(publisher, showBroken);
		if (relatedTagLinks.size() > 0) {
			mv.addObject("related_tags", relatedTagLinks);
		}
	}

		
	private ModelAndView populatePublisherPageModelAndView(Website publisher, boolean showBroken, int page) {
		ModelAndView mv = new ModelAndView();
		mv.addObject("heading", publisher.getName());
		mv.addObject("description", publisher.getName() + " newsitems");
		mv.addObject("link", urlBuilder.getPublisherUrl(publisher));
		
		int startIndex = getStartIndex(page);
		final int mainContentTotal = resourceDAO.getPublisherNewsitemsCount(publisher, showBroken);
		if (mainContentTotal > 0) {			
			final List<Resource> publisherNewsitems = resourceDAO.getPublisherNewsitems(publisher, MAX_NEWSITEMS, showBroken, startIndex);		
			mv.addObject("main_content", publisherNewsitems);
			setRss(mv, rssUrlBuilder.getRssTitleForPublisher(publisher), rssUrlBuilder.getRssUrlForPublisher(publisher));
			mv.addObject("publisher", publisher);
			
			populatePagination(mv, startIndex, mainContentTotal);
			
		}		
		mv.setViewName("browse");
		return mv;
	}
	
}
