package nz.co.searchwellington.controllers.models;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.controllers.RelatedTagsService;
import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.controllers.UrlBuilder;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.TagContentCount;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.ResourceRepository;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

public class PublisherTagCombinerModelBuilder extends AbstractModelBuilder implements ModelBuilder {

Logger logger = Logger.getLogger(PublisherTagCombinerModelBuilder.class);
	
	private ResourceRepository resourceDAO;
	private RssUrlBuilder rssUrlBuilder;
	private UrlBuilder urlBuilder;
	private RelatedTagsService relatedTagsService;
	
	public PublisherTagCombinerModelBuilder(ResourceRepository resourceDAO, RssUrlBuilder rssUrlBuilder, UrlBuilder urlBuilder, RelatedTagsService relatedTagsService) {		
		this.resourceDAO = resourceDAO;
		this.rssUrlBuilder = rssUrlBuilder;
		this.urlBuilder = urlBuilder;
		this.relatedTagsService = relatedTagsService;
	}
	
	
	public boolean isValid(HttpServletRequest request) {
		Tag tag = (Tag) request.getAttribute("tag");
		Website publisher = (Website) request.getAttribute("publisher"); 
		boolean isPublisherTagCombiner = publisher != null && tag != null;
		return isPublisherTagCombiner;
	}
	
	
	public ModelAndView populateContentModel(HttpServletRequest request, boolean showBroken) {
		if (isValid(request)) {
			logger.info("Building publisher tag combiner page model");
			Tag tag = (Tag) request.getAttribute("tag");
			Website publisher = (Website) request.getAttribute("publisher"); 
			ModelAndView mv = new ModelAndView();		
			mv.addObject("publisher", publisher);
			mv.addObject("heading", publisher.getName() + " + " + tag.getDisplayName());
			mv.addObject("description", "");
			mv.addObject("link", urlBuilder.getPublisherCombinerUrl(publisher, tag));			
			populatePublisherTagCombinerNewsitems(mv, publisher, tag, showBroken);		
			mv.setViewName("publisherCombiner");
			return mv;
		}
		return null;
	}
	
	
	public void populateExtraModelConent(HttpServletRequest request, boolean showBroken, ModelAndView mv) {
		Website publisher = (Website) request.getAttribute("publisher"); 
		List<TagContentCount> relatedTagLinks = relatedTagsService.getRelatedTagLinks(publisher, showBroken);
		if (relatedTagLinks.size() > 0) {
			mv.addObject("related_tags", relatedTagLinks);
		}		
	}


	
	private void populatePublisherTagCombinerNewsitems(ModelAndView mv, Website publisher, Tag tag, boolean showBroken) {		
		final List<Resource> publisherNewsitems = resourceDAO.getPublisherTagCombinerNewsitems(publisher, tag, showBroken);
		mv.addObject("main_content", publisherNewsitems);
	        
		if (publisherNewsitems.size() > 0) {            
			setRss(mv, rssUrlBuilder.getRssTitleForPublisherCombiner(publisher, tag), rssUrlBuilder.getRssUrlForPublisherCombiner(publisher, tag));
		}
	}
		
}
