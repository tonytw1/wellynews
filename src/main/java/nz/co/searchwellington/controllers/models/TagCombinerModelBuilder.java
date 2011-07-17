package nz.co.searchwellington.controllers.models;

import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.controllers.RelatedTagsService;
import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.urls.UrlBuilder;

import org.springframework.web.servlet.ModelAndView;

public class TagCombinerModelBuilder extends AbstractModelBuilder implements ModelBuilder {
	
	private ContentRetrievalService contentRetrievalService;
	private RssUrlBuilder rssUrlBuilder;
	private UrlBuilder urlBuilder;
	private RelatedTagsService relatedTagsService;
	
	public TagCombinerModelBuilder(ContentRetrievalService contentRetrievalService,
			RssUrlBuilder rssUrlBuilder, UrlBuilder urlBuilder,
			RelatedTagsService relatedTagsService) {
		this.contentRetrievalService = contentRetrievalService;
		this.rssUrlBuilder = rssUrlBuilder;
		this.urlBuilder = urlBuilder;
		this.relatedTagsService = relatedTagsService;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean isValid(HttpServletRequest request) {		
		List<Tag> tags = (List<Tag>) request.getAttribute("tags");
		boolean isTagCombinerPage = tags != null && tags.size() == 2;
		return isTagCombinerPage;		
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public ModelAndView populateContentModel(HttpServletRequest request, boolean showBroken) {
		if (isValid(request)) {
			List<Tag> tags = (List<Tag>) request.getAttribute("tags");
			return populateTagCombinerModelAndView(tags, showBroken);
		}
		return null;
	}
		
	@Override
	@SuppressWarnings("unchecked")
	public void populateExtraModelConent(HttpServletRequest request, boolean showBroken, ModelAndView mv) {
		List<Tag> tags = (List<Tag>) request.getAttribute("tags");
		Tag tag = tags.get(0);
		mv.addObject("related_tags", relatedTagsService.getRelatedLinksForTag(tag, showBroken, 8));
		mv.addObject("latest_news", contentRetrievalService.getLatestWebsites(5));

		final List<FrontendResource> taggedWebsites = contentRetrievalService.getTaggedWebsites(new HashSet<Tag>(tags), MAX_WEBSITES);  
		mv.addObject("websites", taggedWebsites);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public String getViewName(ModelAndView mv) {
		List<Resource> taggedNewsitems = (List<Resource>) mv.getModel().get("main_content");
		List<Resource> taggedWebsites = (List<Resource>) mv.getModel().get("websites");
		
		boolean isOneContentType = taggedNewsitems.size() == 0 || taggedWebsites.size() == 0;
		if (isOneContentType) {
			return "tagCombinedOneContentType";
		}		
		return "tag";		
	}
	
	private ModelAndView populateTagCombinerModelAndView(List<Tag> tags, boolean showBroken) {
		ModelAndView mv = new ModelAndView();		
		final Tag firstTag = tags.get(0);
		final Tag secondTag = tags.get(1);
		
		mv.addObject("tag", firstTag);
		mv.addObject("tags", tags);
		
		mv.addObject("heading", firstTag.getDisplayName() +  " and " + secondTag.getDisplayName());
		mv.addObject("description", "Items tagged with " + firstTag.getDisplayName() +  " and " + secondTag.getDisplayName() + ".");
		mv.addObject("link", urlBuilder.getTagCombinerUrl(firstTag, secondTag));
		
		final List<FrontendResource> taggedNewsitems = contentRetrievalService.getTaggedNewsitems(new HashSet<Tag>(tags), MAX_WEBSITES);		
		mv.addObject("main_content", taggedNewsitems);		     
		if (taggedNewsitems.size() > 0) {
			 setRss(mv, rssUrlBuilder.getRssTitleForTagCombiner(tags.get(0), tags.get(1)), rssUrlBuilder.getRssUrlForTagCombiner(tags.get(0), tags.get(1)));
		}		
		return mv;
	}
	
}