package nz.co.searchwellington.controllers.models.helpers

import java.util.List
import javax.servlet.http.HttpServletRequest

import nz.co.searchwellington.controllers.models.{GeotaggedNewsitemExtractor, ModelBuilder}
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
                                                     commonAttributesModelBuilder: CommonAttributesModelBuilder) extends ModelBuilder with CommonSizes {

  private val logger: Logger = Logger.getLogger(classOf[PublisherModelBuilder])

  def isValid(request: HttpServletRequest): Boolean = {
    val tag: Tag = request.getAttribute("tag").asInstanceOf[Tag]
    val publisher: Website = request.getAttribute("publisher").asInstanceOf[Website]
    val isPublisherPage: Boolean = publisher != null && tag == null
    return isPublisherPage
  }

  def populateContentModel(request: HttpServletRequest): Option[ModelAndView] = {

    def populatePublisherPageModelAndView(publisher: Website, page: Int): ModelAndView = {
      val mv = new ModelAndView
      mv.addObject("heading", publisher.getName)
      mv.addObject("description", publisher.getName + " newsitems")
      mv.addObject("link", urlBuilder.getPublisherUrl(publisher.getName))

      val frontendPublisher = new FrontendWebsite
      frontendPublisher.setName(publisher.getName)
      frontendPublisher.setUrlWords(publisher.getUrlWords)
      frontendPublisher.setUrl(publisher.getUrl)
      if (publisher.getGeocode != null) {
        frontendPublisher.setPlace(geocodeToPlaceMapper.mapGeocodeToPlace(publisher.getGeocode))
      }
      mv.addObject("publisher", frontendPublisher)
      mv.addObject("location", frontendPublisher.getPlace)

      val startIndex = commonAttributesModelBuilder.getStartIndex(page)
      val mainContentTotal = contentRetrievalService.getPublisherNewsitemsCount(publisher)
      if (mainContentTotal > 0) {
        val publisherNewsitems = contentRetrievalService.getPublisherNewsitems(publisher, MAX_NEWSITEMS, startIndex)
        mv.addObject(MAIN_CONTENT, publisherNewsitems)
        commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForPublisher(publisher), rssUrlBuilder.getRssUrlForPublisher(publisher))
        commonAttributesModelBuilder.populatePagination(mv, startIndex, mainContentTotal)
      }
      mv
    }

    if (isValid(request)) {
      logger.info("Building publisher page model")
      val publisher = request.getAttribute("publisher").asInstanceOf[Website]
      val page = commonAttributesModelBuilder.getPage(request)
      Some(populatePublisherPageModelAndView(publisher, page))
    } else {
      None
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView) {
    val publisher: Website = request.getAttribute("publisher").asInstanceOf[Website]
    mv.addObject("feeds", contentRetrievalService.getPublisherFeeds(publisher))
    mv.addObject("watchlist", contentRetrievalService.getPublisherWatchlist(publisher))
    populateGeotaggedItems(mv)
    val relatedTagLinks = relatedTagsService.getRelatedLinksForPublisher(publisher)
    if (relatedTagLinks.size > 0) {
      mv.addObject("related_tags", relatedTagLinks)
    }
    mv.addObject("latest_newsitems", contentRetrievalService.getLatestNewsitems(5, 1))
  }

  def getViewName(mv: ModelAndView):String = {
    "publisher"
  }

  @SuppressWarnings(Array("unchecked")) private def populateGeotaggedItems(mv: ModelAndView) {
    import scala.collection.JavaConversions._
    val mainContent = mv.getModel.get("main_content").asInstanceOf[List[FrontendNewsitem]]
    if (mainContent != null) {
      val geotaggedNewsitems = geotaggedNewsitemExtractor.extractGeotaggedItems(mainContent)
      if (!geotaggedNewsitems.isEmpty) {
        mv.addObject("geocoded", geotaggedNewsitems)
      }
    }
  }
}
