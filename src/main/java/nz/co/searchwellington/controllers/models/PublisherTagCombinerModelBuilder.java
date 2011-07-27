package nz.co.searchwellington.controllers.models;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.controllers.RelatedTagsService;
import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.TagContentCount;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.model.frontend.FrontendWebsiteImpl;
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
		return publisher != null && tag != null;
	}
	
	@Override
	public ModelAndView populateContentModel(HttpServletRequest request) {
		if (isValid(request)) {
			logger.info("Building publisher tag combiner page model");
			Tag tag = (Tag) request.getAttribute("tag");
			Website publisher = (Website) request.getAttribute("publisher");	// TODO probably needs to be a frontendwebsite
			ModelAndView mv = new ModelAndView();	
			
			// TODO hack - fronendpublisher / publisher mismatch
			FrontendWebsiteImpl frontendPublisher = new FrontendWebsiteImpl();
			frontendPublisher.setName(publisher.getName());
			frontendPublisher.setUrlWords(publisher.getUrlWords());			
			mv.addObject("publisher", frontendPublisher);
			
			mv.addObject("heading", publisher.getName() + " and " + tag.getDisplayName());
			mv.addObject("description", "");
			mv.addObject("link", urlBuilder.getPublisherCombinerUrl(frontendPublisher, tag));
			populatePublisherTagCombinerNewsitems(mv, publisher, tag);			
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
	private void populatePublisherTagCombinerNewsitems(ModelAndView mv, Website publisher, Tag tag) {		
		final List<FrontendResource> publisherNewsitems = contentRetrievalService.getPublisherTagCombinerNewsitems(publisher, tag, MAX_NEWSITEMS);
		mv.addObject("main_content", publisherNewsitems);		
		if (publisherNewsitems.size() > 0) {            
			setRss(mv, rssUrlBuilder.getRssTitleForPublisherCombiner(publisher, tag), rssUrlBuilder.getRssUrlForPublisherCombiner(publisher, tag));
		}
	}
	
}
