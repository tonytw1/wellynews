package nz.co.searchwellington.controllers.models.helpers

import java.util.List

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.models.{GeotaggedNewsitemExtractor, ModelBuilder}
import nz.co.searchwellington.controllers.{LoggedInUserFilter, RelatedTagsService, RssUrlBuilder}
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

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Component class PublisherModelBuilder @Autowired()(rssUrlBuilder: RssUrlBuilder,
                                                    relatedTagsService: RelatedTagsService,
                                                    contentRetrievalService: ContentRetrievalService,
                                                    urlBuilder: UrlBuilder,
                                                    geotaggedNewsitemExtractor: GeotaggedNewsitemExtractor,
                                                    geocodeToPlaceMapper: GeocodeToPlaceMapper,
                                                    commonAttributesModelBuilder: CommonAttributesModelBuilder,
                                                    frontendResourceMapper: FrontendResourceMapper,
                                                    loggedInUserFilter: LoggedInUserFilter) extends ModelBuilder with CommonSizes with Pagination with ReasonableWaits {

  private val logger = Logger.getLogger(classOf[PublisherModelBuilder])

  def isValid(request: HttpServletRequest): Boolean = {
    val tag = request.getAttribute("tag").asInstanceOf[Tag]
    val publisher = request.getAttribute("publisher").asInstanceOf[Website]
    val isPublisherPage = publisher != null && tag == null
    isPublisherPage
  }

  def populateContentModel(request: HttpServletRequest): Option[ModelAndView] = {
    val loggedInUser = Option(loggedInUserFilter.getLoggedInUser)

    def populatePublisherPageModelAndView(publisher: Website, page: Int): ModelAndView = {
      val frontendPublisher = frontendResourceMapper.mapFrontendWebsite(publisher)

      val startIndex = getStartIndex(page)

      val eventualPublisherNewsitemsCount = contentRetrievalService.getPublisherNewsitemsCount(publisher, loggedInUser)
      val eventualPublisherNewsitems = contentRetrievalService.getPublisherNewsitems(publisher, MAX_NEWSITEMS, startIndex, loggedInUser)
      val eventualPublisherFeeds = contentRetrievalService.getPublisherFeeds(publisher, loggedInUser)

      val eventualModelAndView = for {
        mainContentTotal <- eventualPublisherNewsitemsCount
        publisherNewsitems <- eventualPublisherNewsitems
        publisherFeeds <- eventualPublisherFeeds

      } yield {
        val mv = new ModelAndView
        mv.addObject("heading", publisher.title.getOrElse(""))
        mv.addObject("description", publisher.title.getOrElse("") + " newsitems")
        publisher.title.map(t => mv.addObject("link", urlBuilder.getPublisherUrl(t)))
        mv.addObject("publisher", frontendPublisher)
        mv.addObject("location", frontendPublisher.getPlace)

        if (mainContentTotal > 0) {
          import scala.collection.JavaConverters._
          mv.addObject(MAIN_CONTENT, publisherNewsitems.asJava)

          mv.addObject("feeds", publisherFeeds.asJava)
          commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForPublisher(publisher), rssUrlBuilder.getRssUrlForPublisher(publisher))
          populatePagination(mv, startIndex, mainContentTotal)
        }
        mv
      }

      Await.result(eventualModelAndView, TenSeconds)
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
    val loggedInUser = Option(loggedInUserFilter.getLoggedInUser)

    val publisher = request.getAttribute("publisher").asInstanceOf[Website]
    import scala.collection.JavaConverters._

    mv.addObject("watchlist", contentRetrievalService.getPublisherWatchlist(publisher, loggedInUser).asJava)
    populateGeotaggedItems(mv)
    val relatedTagLinks = relatedTagsService.getRelatedLinksForPublisher(publisher)
    if (relatedTagLinks.nonEmpty) {
      mv.addObject("related_tags", relatedTagLinks.asJava)
    }
    mv.addObject("latest_newsitems", Await.result(contentRetrievalService.getLatestNewsitems(5, loggedInUser = loggedInUser), TenSeconds).asJava)
  }

  def getViewName(mv: ModelAndView): String = {
    "publisher"
  }

  private def populateGeotaggedItems(mv: ModelAndView) {
    val mainContent = mv.getModel.get("main_content").asInstanceOf[List[FrontendNewsitem]]
    if (mainContent != null) {
      import scala.collection.JavaConversions._
      val geotaggedNewsitems = geotaggedNewsitemExtractor.extractGeotaggedItems(mainContent)
      if (geotaggedNewsitems.nonEmpty) {
        import scala.collection.JavaConverters._
        mv.addObject("geocoded", geotaggedNewsitems.asJava)
      }
    }
  }

}
