package nz.co.searchwellington.controllers.models;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.controllers.RelatedTagsService;
import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.TagContentCount;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.urls.UrlBuilder;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

public class PublisherTagCombinerModelBuilder extends AbstractModelBuilder implements ModelBuilder {
	
	static Logger logger = Logger.getLogger(PublisherTagCombinerModelBuilder.class);
	
	private ContentRetrievalService contentRetrievalService;
	private RssUrlBuilder rssUrlBuilder;
	private UrlBuilder urlBuilder;
	private RelatedTagsService relatedTagsService;
	
	
	public PublisherTagCombinerModelBuilder(
			ContentRetrievalService contentRetrievalService,
			RssUrlBuilder rssUrlBuilder, UrlBuilder urlBuilder,
			RelatedTagsService relatedTagsService) {
		this.contentRetrievalService = contentRetrievalService;
		this.rssUrlBuilder = rssUrlBuilder;
		this.urlBuilder = urlBuilder;
		this.relatedTagsService = relatedTagsService;
	}

	
	@Override
	public boolean isValid(HttpServletRequest request) {
		Tag tag = (Tag) request.getAttribute("tag");
		Website publisher = (Website) request.getAttribute("publisher"); 
		boolean isPublisherTagCombiner = publisher != null && tag != null;
		return isPublisherTagCombiner;
	}
	
	
	@Override
	public ModelAndView populateContentModel(HttpServletRequest request, boolean showBroken) {
		if (isValid(request)) {
			logger.info("Building publisher tag combiner page model");
			Tag tag = (Tag) request.getAttribute("tag");
			Website publisher = (Website) request.getAttribute("publisher"); 
			ModelAndView mv = new ModelAndView();		
			mv.addObject("publisher", publisher);
			mv.addObject("heading", publisher.getName() + " and " + tag.getDisplayName());
			mv.addObject("description", "");
			mv.addObject("link", urlBuilder.getPublisherCombinerUrl(publisher, tag));			
			populatePublisherTagCombinerNewsitems(mv, publisher, tag, showBroken);			
			return mv;
		}
		return null;
	}
	
	
	@Override
	public void populateExtraModelConent(HttpServletRequest request, boolean showBroken, ModelAndView mv) {
		Website publisher = (Website) request.getAttribute("publisher"); 
		List<TagContentCount> relatedTagLinks = relatedTagsService.getRelatedLinksForPublisher(publisher, showBroken);
		if (relatedTagLinks.size() > 0) {
			mv.addObject("related_tags", relatedTagLinks);
		}		
	}

	
	@Override
	public String getViewName(ModelAndView mv) {
		return "publisherCombiner";
	}
	

	// TODO needs pagination
	private void populatePublisherTagCombinerNewsitems(ModelAndView mv, Website publisher, Tag tag, boolean showBroken) {		
		final List<FrontendResource> publisherNewsitems = contentRetrievalService.getPublisherTagCombinerNewsitems(publisher, tag, MAX_NEWSITEMS);
		mv.addObject("main_content", publisherNewsitems);		
		if (publisherNewsitems.size() > 0) {            
			setRss(mv, rssUrlBuilder.getRssTitleForPublisherCombiner(publisher, tag), rssUrlBuilder.getRssUrlForPublisherCombiner(publisher, tag));
		}
	}
	
}
