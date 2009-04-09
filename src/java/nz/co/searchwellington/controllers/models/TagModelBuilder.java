package nz.co.searchwellington.controllers.models;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.controllers.RelatedTagsService;
import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.controllers.UrlBuilder;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.ResourceRepository;

import org.apache.log4j.Logger;
import org.apache.lucene.index.CorruptIndexException;
import org.springframework.web.servlet.ModelAndView;

import com.sun.syndication.io.FeedException;

public class TagModelBuilder implements ModelBuilder {

	private static final int MAX_WEBSITES = 500;
	private static final int MAX_NEWSITEMS = 30;
	private static final int MAX_NUMBER_OF_COMMENTED_TO_SHOW = 3;
	
	Logger log = Logger.getLogger(TagModelBuilder.class);
    	
	private ResourceRepository resourceDAO;
	private RssUrlBuilder rssUrlBuilder;
	private UrlBuilder urlBuilder;
	private RelatedTagsService relatedTagsService;

		
	
	public TagModelBuilder(ResourceRepository resourceDAO, RssUrlBuilder rssUrlBuilder, UrlBuilder urlBuilder, RelatedTagsService relatedTagsService) {
		this.resourceDAO = resourceDAO;
		this.rssUrlBuilder = rssUrlBuilder;
		this.urlBuilder = urlBuilder;
		this.relatedTagsService = relatedTagsService;
	}

	
	@SuppressWarnings("unchecked")
	public boolean isValid(HttpServletRequest request) {
		List<Tag> tags = (List<Tag>) request.getAttribute("tags");
		boolean isSingleTagPage = tags != null && tags.size() == 1;
		return isSingleTagPage;
	}

	@SuppressWarnings("unchecked")
	public ModelAndView populateContentModel(HttpServletRequest request, boolean showBroken) throws IOException, CorruptIndexException, FeedException {
		if (isValid(request)) {
			log.info("Building tag page model");
			List<Tag> tags = (List<Tag>) request.getAttribute("tags");
			Tag tag = tags.get(0);
			return populateTagPageModelAndView(tag, showBroken);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public void populateExtraModelConent(HttpServletRequest request, boolean showBroken, ModelAndView mv) {
		List<Tag> tags = (List<Tag>) request.getAttribute("tags");
		Tag tag = tags.get(0);
		mv.addObject("related_tags", relatedTagsService.getRelatedTagLinks(tag, showBroken));
		try {
			populateCommentedTaggedNewsitems(mv, tag, showBroken);
			mv.addObject("last_changed", resourceDAO.getLastLiveTimeForTag(tag));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
 	
	
	private ModelAndView populateTagPageModelAndView(Tag tag, boolean showBroken) throws IOException, CorruptIndexException, FeedException {		
		ModelAndView mv = new ModelAndView();				
		mv.addObject("tag", tag);
		mv.addObject("heading", tag.getDisplayName());        		
		mv.addObject("description", tag.getDisplayName());
		mv.addObject("link", urlBuilder.getTagUrl(tag));	
		
		final List<Website> taggedWebsites = resourceDAO.getTaggedWebsites(tag, showBroken, MAX_WEBSITES);
		final List<Resource> taggedNewsitems = resourceDAO.getTaggedNewitems(tag, showBroken, MAX_NEWSITEMS);         
		
		mv.addObject("main_content", taggedNewsitems);
		mv.addObject("websites", taggedWebsites);
	
		if (taggedNewsitems.size() > 0) {
			 setRss(mv, rssUrlBuilder.getRssTitleForTag(tag), rssUrlBuilder.getRssUrlForTag(tag));
		}
				
		mv.setViewName("tag");
		return mv;
	}
	
	

    private void populateCommentedTaggedNewsitems(ModelAndView mv, Tag tag, boolean showBroken) throws IOException {
        List<Resource> allCommentedNewsitems = resourceDAO.getCommentedNewsitemsForTag(tag, showBroken, MAX_NUMBER_OF_COMMENTED_TO_SHOW + 1);        
        List<Resource>commentedToShow;
        if (allCommentedNewsitems.size() <= MAX_NUMBER_OF_COMMENTED_TO_SHOW) {
            commentedToShow = allCommentedNewsitems;            
        } else {
            commentedToShow = allCommentedNewsitems.subList(0, MAX_NUMBER_OF_COMMENTED_TO_SHOW);
            final String moreCommentsUrl = urlBuilder.getTagCommentUrl(tag);
            mv.addObject("commented_newsitems_moreurl", moreCommentsUrl);
            // TODO count
        }        
        mv.addObject("commented_newsitems", commentedToShow);        
    }
	
	
	private void setRss(ModelAndView mv, String title, String url) {
		mv.addObject("rss_title", title);
		mv.addObject("rss_url", url);
	}  
	

}
