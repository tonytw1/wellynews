package nz.co.searchwellington.controllers.models.helpers

import java.util.List
import javax.servlet.http.HttpServletRequest

import nz.co.searchwellington.controllers.models.{GeotaggedNewsitemExtractor, ModelBuilder}
import nz.co.searchwellington.controllers.{RelatedTagsService, RssUrlBuilder}
import nz.co.searchwellington.model.frontend.FrontendNewsitem
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.{Tag, Website}
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
                                                     commonAttributesModelBuilder: CommonAttributesModelBuilder,
                                                     frontendResourceMapper: FrontendResourceMapper) extends ModelBuilder with CommonSizes with Pagination {

  private val logger = Logger.getLogger(classOf[PublisherModelBuilder])

  def isValid(request: HttpServletRequest): Boolean = {
    val tag = request.getAttribute("tag").asInstanceOf[Tag]
    val publisher = request.getAttribute("publisher").asInstanceOf[Website]
    val isPublisherPage = publisher != null && tag == null
    isPublisherPage
  }

  def populateContentModel(request: HttpServletRequest): Option[ModelAndView] = {

    def populatePublisherPageModelAndView(publisher: Website, page: Int): ModelAndView = {
      val frontendPublisher = frontendResourceMapper.mapFrontendWebsite(publisher)

      val mv = new ModelAndView
      mv.addObject("heading", publisher.title.getOrElse(""))
      mv.addObject("description", publisher.title.getOrElse("") + " newsitems")
      publisher.title.map(t => mv.addObject("link", urlBuilder.getPublisherUrl(t)))
      mv.addObject("publisher", frontendPublisher)
      mv.addObject("location", frontendPublisher.getPlace)

      val startIndex = getStartIndex(page)
      val mainContentTotal = contentRetrievalService.getPublisherNewsitemsCount(publisher)
      if (mainContentTotal > 0) {
        val publisherNewsitems = contentRetrievalService.getPublisherNewsitems(publisher, MAX_NEWSITEMS, startIndex)
        import scala.collection.JavaConverters._
        mv.addObject(MAIN_CONTENT, publisherNewsitems.asJava)

        commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForPublisher(publisher), rssUrlBuilder.getRssUrlForPublisher(publisher))
        populatePagination(mv, startIndex, mainContentTotal)
      }
      mv
    }

    if (isValid(request)) {
      logger.info("Building publisher page model")
      val publisher = request.getAttribute("publisher").asInstanceOf[Website]
      val page = getPage(request)
      Some(populatePublisherPageModelAndView(publisher, page))

    } else {
      None
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView) {
    val publisher = request.getAttribute("publisher").asInstanceOf[Website]
    import scala.collection.JavaConverters._

    val publisherFeeds = contentRetrievalService.getPublisherFeeds(publisher)
    logger.info("Publisher feeds: " + publisherFeeds.size)
    mv.addObject("feeds", publisherFeeds.asJava)

    mv.addObject("watchlist", contentRetrievalService.getPublisherWatchlist(publisher).asJava)
    populateGeotaggedItems(mv)
    val relatedTagLinks = relatedTagsService.getRelatedLinksForPublisher(publisher)
    if (relatedTagLinks.size > 0) {
      mv.addObject("related_tags", relatedTagLinks.asJava)
    }
    mv.addObject("latest_newsitems", contentRetrievalService.getLatestNewsitems(5, 1).asJava)
  }

  def getViewName(mv: ModelAndView):String = {
    "publisher"
  }

  private def populateGeotaggedItems(mv: ModelAndView) {
    val mainContent = mv.getModel.get("main_content").asInstanceOf[List[FrontendNewsitem]]
    if (mainContent != null) {
      import scala.collection.JavaConversions._
      val geotaggedNewsitems = geotaggedNewsitemExtractor.extractGeotaggedItems(mainContent)
      if (!geotaggedNewsitems.isEmpty) {
        import scala.collection.JavaConverters._
        mv.addObject("geocoded", geotaggedNewsitems.asJava)
      }
    }
  }

}
