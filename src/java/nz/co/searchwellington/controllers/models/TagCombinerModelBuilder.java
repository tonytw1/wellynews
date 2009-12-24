package nz.co.searchwellington.controllers.models;

import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.controllers.RelatedTagsService;
import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.urls.UrlBuilder;

import org.springframework.web.servlet.ModelAndView;

public class TagCombinerModelBuilder extends AbstractModelBuilder implements ModelBuilder {
	
	private ResourceRepository resourceDAO;	
	private RssUrlBuilder rssUrlBuilder;
	private UrlBuilder urlBuilder;
	private RelatedTagsService relatedTagsService;
	
	
	public TagCombinerModelBuilder(ResourceRepository resourceDAO, RssUrlBuilder rssUrlBuilder, UrlBuilder urlBuilder, RelatedTagsService relatedTagsService) {		
		this.resourceDAO = resourceDAO;		
		this.rssUrlBuilder = rssUrlBuilder;
		this.urlBuilder = urlBuilder;
		this.relatedTagsService = relatedTagsService;		
	}
	
	
	@SuppressWarnings("unchecked")
	public boolean isValid(HttpServletRequest request) {		
		List<Tag> tags = (List<Tag>) request.getAttribute("tags");
		boolean isTagCombinerPage = tags != null && tags.size() == 2;
		return isTagCombinerPage;		
	}

	
	@SuppressWarnings("unchecked")
	public ModelAndView populateContentModel(HttpServletRequest request, boolean showBroken) {
		if (isValid(request)) {
			List<Tag> tags = (List<Tag>) request.getAttribute("tags");
			return populateTagCombinerModelAndView(tags, showBroken);
		}
		return null;
	}
	
	
	@SuppressWarnings("unchecked")
	public void populateExtraModelConent(HttpServletRequest request, boolean showBroken, ModelAndView mv) {
		List<Tag> tags = (List<Tag>) request.getAttribute("tags");
		Tag tag = tags.get(0);		
		mv.addObject("related_tags", relatedTagsService.getRelatedLinksForTag(tag, showBroken, 1000));
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
		
		final List<Resource> taggedWebsites = resourceDAO.getTaggedWebsites(new HashSet<Tag>(tags), showBroken, MAX_WEBSITES);  
		final List<Resource> taggedNewsitems = resourceDAO.getTaggedNewsitems(new HashSet<Tag>(tags), showBroken, MAX_WEBSITES);
		
		mv.addObject("main_content", taggedNewsitems);	
		mv.addObject("websites", taggedWebsites);
		     
		if (taggedNewsitems.size() > 0) { 
			 setRss(mv, rssUrlBuilder.getRssTitleForTagCombiner(tags.get(0), tags.get(1)), rssUrlBuilder.getRssUrlForTagCombiner(tags.get(0), tags.get(1)));
		}

		boolean isOneContentType = taggedNewsitems.size() == 0 || taggedWebsites.size() == 0;
		if (isOneContentType) {
			mv.setViewName("tagCombinedOneContentType");
		} else {
			mv.setViewName("tag");			
		}
		return mv;
	}
	
}
