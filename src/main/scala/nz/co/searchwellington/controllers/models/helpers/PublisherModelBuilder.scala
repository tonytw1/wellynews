package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.models.{GeotaggedNewsitemExtractor, ModelBuilder}
import nz.co.searchwellington.controllers.{RelatedTagsService, RssUrlBuilder}
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.{Tag, User, Website}
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

@Component class PublisherModelBuilder @Autowired()(rssUrlBuilder: RssUrlBuilder,
                                                    relatedTagsService: RelatedTagsService,
                                                    contentRetrievalService: ContentRetrievalService,
                                                    urlBuilder: UrlBuilder,
                                                    geotaggedNewsitemExtractor: GeotaggedNewsitemExtractor,
                                                    commonAttributesModelBuilder: CommonAttributesModelBuilder,
                                                    frontendResourceMapper: FrontendResourceMapper) extends ModelBuilder
  with CommonSizes with Pagination with ReasonableWaits {

  private val logger = Logger.getLogger(classOf[PublisherModelBuilder])

  def isValid(request: HttpServletRequest): Boolean = {
    val tag = request.getAttribute("tag").asInstanceOf[Tag]
    val publisher = request.getAttribute("publisher").asInstanceOf[Website]
    val isPublisherPage = publisher != null && tag == null
    isPublisherPage
  }

  def populateContentModel(request: HttpServletRequest, l: User): Future[Option[ModelAndView]] = {
    val loggedInUser = Option(l)

    def populatePublisherPageModelAndView(publisher: Website, page: Int) = {
      val startIndex = getStartIndex(page, MAX_NEWSITEMS)

      val eventualPublisherNewsitems = contentRetrievalService.getPublisherNewsitems(publisher, MAX_NEWSITEMS, startIndex, loggedInUser)
      val eventualPublisherFeeds = contentRetrievalService.getPublisherFeeds(publisher, loggedInUser)
      val eventualFrontendWebsite = frontendResourceMapper.mapFrontendWebsite(publisher)

      for {
        publisherNewsitems <- eventualPublisherNewsitems
        publisherFeeds <- eventualPublisherFeeds
        frontendWebsite <- eventualFrontendWebsite

      } yield {
        val mv = new ModelAndView().addObject("heading", publisher.title.getOrElse("")).
          addObject("description", publisher.title.getOrElse("") + " newsitems").
          addObject("publisher", frontendWebsite).
          addObject("location", frontendWebsite.getPlace)

        publisher.title.map(t => mv.addObject("link", urlBuilder.getPublisherUrl(t)))

        val totalPublisherNewsitems = publisherNewsitems._2
        if (publisherNewsitems._1.nonEmpty) {
          import scala.collection.JavaConverters._
          mv.addObject(MAIN_CONTENT, publisherNewsitems._1.asJava)

          populateGeotaggedItems(mv, publisherNewsitems._1)

          mv.addObject("feeds", publisherFeeds.asJava)
          commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForPublisher(publisher), rssUrlBuilder.getRssUrlForPublisher(publisher))
          populatePagination(mv, startIndex, totalPublisherNewsitems, MAX_NEWSITEMS)
        }
        Some(mv)
      }
    }

    if (isValid(request)) {
      logger.info("Building publisher page model")
      val publisher = request.getAttribute("publisher").asInstanceOf[Website]
      val page = getPage(request)
      populatePublisherPageModelAndView(publisher, page)

    } else {
      Future.successful(None)
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView, l: User): Future[ModelAndView] = {
    val loggedInUser = Option(l)

    val publisher = request.getAttribute("publisher").asInstanceOf[Website]
    import scala.collection.JavaConverters._

    val eventualPublisherWatchlist = contentRetrievalService.getPublisherWatchlist(publisher, loggedInUser)
    val eventualLatestNewsitems = contentRetrievalService.getLatestNewsitems(5, loggedInUser = loggedInUser)

    for {
      publisherWatchlist <- eventualPublisherWatchlist
      latestNewsitems <- eventualLatestNewsitems
    } yield {
      mv.addObject("watchlist", publisherWatchlist.asJava)
      val relatedTagLinks = relatedTagsService.getRelatedLinksForPublisher(publisher)
      if (relatedTagLinks.nonEmpty) {
        mv.addObject("related_tags", relatedTagLinks.asJava)
      }
      mv.addObject("latest_newsitems", latestNewsitems.asJava)
    }
  }

  def getViewName(mv: ModelAndView): String = "publisher"

  private def populateGeotaggedItems(mv: ModelAndView, mainContent: Seq[FrontendResource]) {
    val geotaggedNewsitems = geotaggedNewsitemExtractor.extractGeotaggedItems(mainContent)
    if (geotaggedNewsitems.nonEmpty) {
      import scala.collection.JavaConverters._
      mv.addObject("geocoded", geotaggedNewsitems.asJava)
    }
  }

}
