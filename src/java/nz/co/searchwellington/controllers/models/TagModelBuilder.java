package nz.co.searchwellington.controllers.models;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

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
	
	Logger log = Logger.getLogger(TagModelBuilder.class);
    	
	private ResourceRepository resourceDAO;
	private RssUrlBuilder rssUrlBuilder;
	private UrlBuilder urlBuilder;

		
	
	public TagModelBuilder(ResourceRepository resourceDAO, RssUrlBuilder rssUrlBuilder, UrlBuilder urlBuilder) {
		this.resourceDAO = resourceDAO;
		this.rssUrlBuilder = rssUrlBuilder;
		this.urlBuilder = urlBuilder;
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
	
	
	public ModelAndView populateTagPageModelAndView(Tag tag, boolean showBroken) throws IOException, CorruptIndexException, FeedException {		
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
	
	
	private void setRss(ModelAndView mv, String title, String url) {
		mv.addObject("rss_title", title);
		mv.addObject("rss_url", url);
	}  
	

}
