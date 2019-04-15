package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.models.{GeotaggedNewsitemExtractor, ModelBuilder}
import nz.co.searchwellington.feeds.{FeedItemLocalCopyDecorator, FeeditemToNewsitemService, RssfeedNewsitemService}
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.{Feed, Newsitem}
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem

import scala.concurrent.Await

@Component class FeedModelBuilder @Autowired()(rssfeedNewsitemService: RssfeedNewsitemService, contentRetrievalService: ContentRetrievalService,
                                               geotaggedNewsitemExtractor: GeotaggedNewsitemExtractor, feedNewsItemLocalCopyDecorator: FeedItemLocalCopyDecorator,
                                               frontendResourceMapper: FrontendResourceMapper,
                                               commonAttributesModelBuilder: CommonAttributesModelBuilder,
                                               feeditemToNewsitemService: FeeditemToNewsitemService) extends ModelBuilder
  with ReasonableWaits {

  private val log = Logger.getLogger(classOf[FeedModelBuilder])
  private val FEED_ATTRIBUTE = "feedAttribute"

  def isValid(request: HttpServletRequest): Boolean = {
    return request.getAttribute(FEED_ATTRIBUTE) != null
  }

  def populateContentModel(request: HttpServletRequest): Option[ModelAndView] = {

    def populateFeedItems(mv: ModelAndView, feed: Feed) {
      val feedItemsForFeed = Await.result(rssfeedNewsitemService.getFeedItemsAndDetailsFor(feed), TenSeconds)
      feedItemsForFeed.fold({ l =>
        mv.addObject("feed_error", l)

      }, { result =>
        val feedItems = result._1
        val feedNewsitems = feedItems.map(i => feeditemToNewsitemService.makeNewsitemFromFeedItem(i, feed))
        val feedItemsWithAcceptanceInformation = feedNewsItemLocalCopyDecorator.addSupressionAndLocalCopyInformation(feedNewsitems)
        import scala.collection.JavaConverters._
        mv.addObject(MAIN_CONTENT, feedItemsWithAcceptanceInformation.asJava)
        populateGeotaggedFeedItems(mv, feedItems)
        mv.addObject("whakaoko_subscription", result._2)
      })
    }

    if (isValid(request)) {
      val feedOnRequest = Option(request.getAttribute(FEED_ATTRIBUTE).asInstanceOf[Feed])
      feedOnRequest.flatMap { feed =>
        feed.page.map { p =>
          val mv = new ModelAndView
          mv.addObject("feed", frontendResourceMapper.createFrontendResourceFrom(feed))
          commonAttributesModelBuilder.setRss(mv, feed.title.getOrElse(""), p)
          populateFeedItems(mv, feed)
          mv
        }
      }

    } else {
      None
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView) {
    commonAttributesModelBuilder.populateSecondaryFeeds(mv)
  }

  def getViewName(mv: ModelAndView): String = {
    return "viewfeed"
  }

  private def populateGeotaggedFeedItems(mv: ModelAndView, feedNewsitems: Seq[FeedItem]) {
    val geotaggedItems = geotaggedNewsitemExtractor.extractGeotaggedItemsFromFeedNewsitems(feedNewsitems)
    if (geotaggedItems.nonEmpty) {
      log.info("Adding " + geotaggedItems.size + " geotagged feed items")
      import scala.collection.JavaConverters._
      mv.addObject("geocoded", geotaggedItems.asJava) // TODO deduplicate overlapping
    }
  }

}
