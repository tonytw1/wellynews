package nz.co.searchwellington.controllers.models;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.controllers.RelatedTagsService;
import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.TagContentCount;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.model.frontend.FrontendNewsitem;
import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.model.frontend.FrontendWebsiteImpl;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.urls.UrlBuilder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Lists;

@Component
public class PublisherModelBuilder extends AbstractModelBuilder implements ModelBuilder {
	
	private static Logger logger = Logger.getLogger(PublisherModelBuilder.class);
	
	private RssUrlBuilder rssUrlBuilder;
	private RelatedTagsService relatedTagsService;
	private ContentRetrievalService contentRetrievalService;
	private UrlBuilder urlBuilder;
	
	@Autowired
	public PublisherModelBuilder(RssUrlBuilder rssUrlBuilder, RelatedTagsService relatedTagsService, ContentRetrievalService contentRetrievalService, UrlBuilder urlBuilder) {
		this.rssUrlBuilder = rssUrlBuilder;
		this.relatedTagsService = relatedTagsService;
		this.contentRetrievalService = contentRetrievalService;
		this.urlBuilder = urlBuilder;
	}
	
	@Override
	public boolean isValid(HttpServletRequest request) {
        Tag tag = (Tag) request.getAttribute("tag");
        Website publisher = (Website) request.getAttribute("publisher");   
        boolean isPublisherPage = publisher != null && tag == null;
        return isPublisherPage;
	}
		
	@Override
	public ModelAndView populateContentModel(HttpServletRequest request) {				
		if (isValid(request)) {
			logger.info("Building publisher page model");
			Website publisher = (Website) request.getAttribute("publisher");	// TODO needs to be a frontend Website
			int page = getPage(request);
			return populatePublisherPageModelAndView(publisher, page);			
		}
		return null;
	}
		
	@Override
	public void populateExtraModelConent(HttpServletRequest request, ModelAndView mv) {
		Website publisher = (Website) request.getAttribute("publisher");
		
		mv.addObject("feeds", contentRetrievalService.getPublisherFeeds(publisher));
		mv.addObject("watchlist", contentRetrievalService.getPublisherWatchlist(publisher));
	
		populateGeotaggedItems(mv);
		
		List<TagContentCount> relatedTagLinks = relatedTagsService.getRelatedLinksForPublisher(publisher);
		if (relatedTagLinks.size() > 0) {
			mv.addObject("related_tags", relatedTagLinks);
		}
		
		mv.addObject("latest_newsitems", contentRetrievalService.getLatestNewsitems(5));
	}
	
	@Override
	public String getViewName(ModelAndView mv) {
		return "publisher";
	}
	
	private ModelAndView populatePublisherPageModelAndView(Website publisher, int page) {
		ModelAndView mv = new ModelAndView();
		mv.addObject("heading", publisher.getName());
		mv.addObject("description", publisher.getName() + " newsitems");
		mv.addObject("link", urlBuilder.getPublisherUrl(publisher.getName()));
		
		// TODO hack - fronendpublisher / publisher mismatch
		FrontendWebsiteImpl frontendPublisher = new FrontendWebsiteImpl();
		frontendPublisher.setName(publisher.getName());
		frontendPublisher.setUrlWords(publisher.getUrlWords());			
		mv.addObject("publisher", frontendPublisher);
		
		int startIndex = getStartIndex(page);
		final long mainContentTotal = contentRetrievalService.getPublisherNewsitemsCount(publisher);
		if (mainContentTotal > 0) {			
			final List<FrontendResource> publisherNewsitems = contentRetrievalService.getPublisherNewsitems(publisher, MAX_NEWSITEMS, startIndex);		
			mv.addObject("main_content", publisherNewsitems);
			setRss(mv, rssUrlBuilder.getRssTitleForPublisher(publisher), rssUrlBuilder.getRssUrlForPublisher(publisher));			
			populatePagination(mv, startIndex, mainContentTotal);
		}
		return mv;
	}
	
	@SuppressWarnings("unchecked")
	// TODO duplication with feed model builder
	private void populateGeotaggedItems(ModelAndView mv) {
		List<FrontendNewsitem> mainContent = (List<FrontendNewsitem>) mv.getModel().get("main_content");
		if (mainContent != null) {
			final List<FrontendNewsitem> geotaggedNewsitems = Lists.newArrayList();
			for (FrontendNewsitem feedNewsitem : mainContent) {
				if (feedNewsitem.getPlace() != null) {
					geotaggedNewsitems.add(feedNewsitem);
				}
			}
			if (!geotaggedNewsitems.isEmpty()) {
				mv.addObject("geocoded", geotaggedNewsitems);
			}
		}
	}
	
}
