package nz.co.searchwellington.controllers.models;

import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.controllers.RelatedTagsService;
import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.controllers.models.helpers.CommonAttributesModelBuilder;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.urls.UrlBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

@Component
public class TagCombinerModelBuilder implements ModelBuilder {
	
	private ContentRetrievalService contentRetrievalService;
	private RssUrlBuilder rssUrlBuilder;
	private UrlBuilder urlBuilder;
	private RelatedTagsService relatedTagsService;
    private CommonAttributesModelBuilder commonAttributesModelBuilder;
	
	@Autowired
	public TagCombinerModelBuilder(ContentRetrievalService contentRetrievalService,
			RssUrlBuilder rssUrlBuilder, UrlBuilder urlBuilder,
			RelatedTagsService relatedTagsService, CommonAttributesModelBuilder commonAttributesModelBuilder) {
		this.contentRetrievalService = contentRetrievalService;
		this.rssUrlBuilder = rssUrlBuilder;
		this.urlBuilder = urlBuilder;
		this.relatedTagsService = relatedTagsService;
        this.commonAttributesModelBuilder = commonAttributesModelBuilder;
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
	public ModelAndView populateContentModel(HttpServletRequest request) {
		if (isValid(request)) {
			List<Tag> tags = (List<Tag>) request.getAttribute("tags");
			int page = commonAttributesModelBuilder.getPage(request);
			return populateTagCombinerModelAndView(tags, page);
		}
		return null;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void populateExtraModelContent(HttpServletRequest request, ModelAndView mv) {
		List<Tag> tags = (List<Tag>) request.getAttribute("tags");
		Tag tag = tags.get(0);
		mv.addObject("related_tags", relatedTagsService.getRelatedLinksForTag(tag, 8));
		mv.addObject("latest_news", contentRetrievalService.getLatestWebsites(5));

		final List<FrontendResource> taggedWebsites = contentRetrievalService.getTaggedWebsites(new HashSet<Tag>(tags), CommonAttributesModelBuilder.MAX_WEBSITES);
		mv.addObject("websites", taggedWebsites);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public String getViewName(ModelAndView mv) {
		final long taggedNewsitemsCount = (Long) mv.getModel().get("main_content_total");
		final List<Resource> taggedWebsites = (List<Resource>) mv.getModel().get("websites");
		final boolean isOneContentType = taggedNewsitemsCount == 0 || taggedWebsites.size() == 0;
		if (isOneContentType) {
			return "tagCombinedOneContentType";
		}
		return "tag";
	}
	
	private ModelAndView populateTagCombinerModelAndView(List<Tag> tags, int page) {
		final int startIndex = commonAttributesModelBuilder.getStartIndex(page);
		final long totalNewsitemCount = contentRetrievalService.getTaggedNewsitemsCount(tags);
		if (startIndex > totalNewsitemCount) {
			return null;
		}
		
		final Tag firstTag = tags.get(0);
		final Tag secondTag = tags.get(1);
		
		final ModelAndView mv = new ModelAndView();		
		mv.addObject("tag", firstTag);
		mv.addObject("tags", tags);
		
		mv.addObject("heading", firstTag.getDisplayName() +  " and " + secondTag.getDisplayName());
		mv.addObject("description", "Items tagged with " + firstTag.getDisplayName() +  " and " + secondTag.getDisplayName() + ".");
		mv.addObject("link", urlBuilder.getTagCombinerUrl(firstTag, secondTag));
		
		if (totalNewsitemCount > 0) {			
			commonAttributesModelBuilder.populatePagination(mv, startIndex, totalNewsitemCount);
			final List<FrontendResource> taggedNewsitems = contentRetrievalService.getTaggedNewsitems(tags, startIndex, CommonAttributesModelBuilder.MAX_NEWSITEMS);
			mv.addObject("main_content", taggedNewsitems);
			 commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForTagCombiner(tags.get(0), tags.get(1)), rssUrlBuilder.getRssUrlForTagCombiner(tags.get(0), tags.get(1)));
			 return mv;
		}
		return null;
	}
	
}