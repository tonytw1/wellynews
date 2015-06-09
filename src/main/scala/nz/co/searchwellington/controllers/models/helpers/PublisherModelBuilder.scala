package nz.co.searchwellington.controllers.models.helpers

import java.util.List
import javax.servlet.http.HttpServletRequest

import nz.co.searchwellington.controllers.models.GeotaggedNewsitemExtractor
import nz.co.searchwellington.controllers.{RelatedTagsService, RssUrlBuilder}
import nz.co.searchwellington.model.frontend.{FrontendNewsitem, FrontendResource, FrontendWebsite}
import nz.co.searchwellington.model.{Tag, TagContentCount, Website}
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import nz.co.searchwellington.views.GeocodeToPlaceMapper
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

@Component class PublisherModelBuilder @Autowired() (rssUrlBuilder: RssUrlBuilder,
                                                     relatedTagsService: RelatedTagsService,
                                                     contentRetrievalService: ContentRetrievalService,
                                                     urlBuilder: UrlBuilder,
                                                     geotaggedNewsitemExtractor: GeotaggedNewsitemExtractor,
                                                     geocodeToPlaceMapper: GeocodeToPlaceMapper,
                                                     commonAttributesModelBuilder: CommonAttributesModelBuilder){

  private val logger: Logger = Logger.getLogger(classOf[PublisherModelBuilder])

  def isValid(request: HttpServletRequest): Boolean = {
    val tag: Tag = request.getAttribute("tag").asInstanceOf[Tag]
    val publisher: Website = request.getAttribute("publisher").asInstanceOf[Website]
    publisher != null && tag == null
  }

  def populateContentModel(request: HttpServletRequest): ModelAndView = {
    if (isValid(request)) {
      logger.info("Building publisher page model")
      val publisher: Website = request.getAttribute("publisher").asInstanceOf[Website]
      val page: Int = commonAttributesModelBuilder.getPage(request)
      return populatePublisherPageModelAndView(publisher, page)
    }
    return null
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView) {
    val publisher: Website = request.getAttribute("publisher").asInstanceOf[Website]
    mv.addObject("feeds", contentRetrievalService.getPublisherFeeds(publisher))
    mv.addObject("watchlist", contentRetrievalService.getPublisherWatchlist(publisher))
    populateGeotaggedItems(mv)
    val relatedTagLinks: List[TagContentCount] = relatedTagsService.getRelatedLinksForPublisher(publisher)
    if (relatedTagLinks.size > 0) {
      mv.addObject("related_tags", relatedTagLinks)
    }
    mv.addObject("latest_newsitems", contentRetrievalService.getLatestNewsitems(5))
  }

  def getViewName(mv: ModelAndView) = {
    "publisher"
  }

  private def populatePublisherPageModelAndView(publisher: Website, page: Int): ModelAndView = {
    val mv: ModelAndView = new ModelAndView
    mv.addObject("heading", publisher.getName)
    mv.addObject("description", publisher.getName + " newsitems")
    mv.addObject("link", urlBuilder.getPublisherUrl(publisher.getName))

    val frontendPublisher: FrontendWebsite = new FrontendWebsite
    frontendPublisher.setName(publisher.getName)
    frontendPublisher.setUrlWords(publisher.getUrlWords)
    frontendPublisher.setUrl(publisher.getUrl)

    if (publisher.getGeocode != null) {
      frontendPublisher.setPlace(geocodeToPlaceMapper.mapGeocodeToPlace(publisher.getGeocode))
    }
    mv.addObject("publisher", frontendPublisher)
    mv.addObject("location", frontendPublisher.getPlace)
    val startIndex: Int = commonAttributesModelBuilder.getStartIndex(page)
    val mainContentTotal: Long = contentRetrievalService.getPublisherNewsitemsCount(publisher)
    if (mainContentTotal > 0) {
      val publisherNewsitems: List[FrontendResource] = contentRetrievalService.getPublisherNewsitems(publisher, CommonAttributesModelBuilder.MAX_NEWSITEMS, startIndex)
      mv.addObject("main_content", publisherNewsitems)
      commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForPublisher(publisher), rssUrlBuilder.getRssUrlForPublisher(publisher))
      commonAttributesModelBuilder.populatePagination(mv, startIndex, mainContentTotal)
    }
    return mv
  }

  @SuppressWarnings(Array("unchecked")) private def populateGeotaggedItems(mv: ModelAndView) {
    val mainContent: List[FrontendNewsitem] = mv.getModel.get("main_content").asInstanceOf[List[FrontendNewsitem]]
    if (mainContent != null) {
      val geotaggedNewsitems: List[FrontendNewsitem] = geotaggedNewsitemExtractor.extractGeotaggedItems(mainContent)
      if (!geotaggedNewsitems.isEmpty) {
        mv.addObject("geocoded", geotaggedNewsitems)
      }
    }
  }
}