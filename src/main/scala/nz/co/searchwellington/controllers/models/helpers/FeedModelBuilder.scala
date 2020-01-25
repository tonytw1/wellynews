package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.LoggedInUserFilter
import nz.co.searchwellington.controllers.models.{GeotaggedNewsitemExtractor, ModelBuilder}
import nz.co.searchwellington.feeds.reading.WhakaokoService
import nz.co.searchwellington.feeds.reading.whakaoko.model.{FeedItem, Subscription}
import nz.co.searchwellington.feeds.{FeedItemLocalCopyDecorator, FeeditemToNewsitemService, RssfeedNewsitemService}
import nz.co.searchwellington.model.Feed
import nz.co.searchwellington.model.frontend.{FeedNewsitemForAcceptance, FrontendNewsitem}
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

@Component class FeedModelBuilder @Autowired()(rssfeedNewsitemService: RssfeedNewsitemService, contentRetrievalService: ContentRetrievalService,
                                               geotaggedNewsitemExtractor: GeotaggedNewsitemExtractor, feedNewsItemLocalCopyDecorator: FeedItemLocalCopyDecorator,
                                               frontendResourceMapper: FrontendResourceMapper,
                                               commonAttributesModelBuilder: CommonAttributesModelBuilder,
                                               feeditemToNewsitemService: FeeditemToNewsitemService,
                                               loggedInUserFilter: LoggedInUserFilter,
                                               whakaokoService: WhakaokoService) extends ModelBuilder with ReasonableWaits {

  private val log = Logger.getLogger(classOf[FeedModelBuilder])
  private val FEED_ATTRIBUTE = "feedAttribute"

  def isValid(request: HttpServletRequest): Boolean = {
    request.getAttribute(FEED_ATTRIBUTE) != null
  }

  def populateContentModel(request: HttpServletRequest): Option[ModelAndView] = {

    def populateGeotaggedFeedItems(mv: ModelAndView, feedNewsitems: Seq[FrontendNewsitem]) {
      val geotaggedItems = geotaggedNewsitemExtractor.extractGeotaggedItems(feedNewsitems)
      if (geotaggedItems.nonEmpty) {
        log.info("Adding " + geotaggedItems.size + " geotagged feed items")

        import scala.collection.JavaConverters._

        mv.addObject("geocoded", geotaggedItems.asJava) // TODO deduplicate overlapping
      }
    }

    def populateFeedItems(mv: ModelAndView, feed: Feed) {

      val z: Future[Either[String, Seq[FeedNewsitemForAcceptance]]] = rssfeedNewsitemService.getFeedItemsAndDetailsFor(feed).flatMap {
        feedItemsForFeed =>
          val x = feedItemsForFeed.fold({
            l =>
              Future.successful(Left(l))

          }, {
            result =>
              val a: (Seq[FeedItem], Subscription) = result
              val feedItems = result._1
              val feedNewsitems = feedItems.map(i => feeditemToNewsitemService.makeNewsitemFromFeedItem(i, feed))
              val eventualWithSuppressionAndLocalCopyInformation = feedNewsItemLocalCopyDecorator.addSupressionAndLocalCopyInformation(feedNewsitems)

              eventualWithSuppressionAndLocalCopyInformation.map {
                i =>
                  Right(i)
              }
          })
          x
      }

      val x = z.map {
        y =>
          y.fold({
            l =>
              mv.addObject("feed_error", l)
              mv

          }, {
            result =>
              val a = result

              import scala.collection.JavaConverters._

              mv.addObject(MAIN_CONTENT, result.asJava)
              populateGeotaggedFeedItems(mv, result.map(_.newsitem))

              //mv.addObject("whakaoko_subscription", result._2)
              mv
          })
      }

      Await.result(x, TenSeconds)
    }

    if (isValid(request)) {
      val feedOnRequest = Option(request.getAttribute(FEED_ATTRIBUTE).asInstanceOf[Feed])
      feedOnRequest.flatMap {
        feed =>
          feed.page.map { p =>
            val mv = new ModelAndView
            mv.addObject("feed", frontendResourceMapper.createFrontendResourceFrom(feed))
            Await.result(whakaokoService.getWhakaokoSubscriptionByUrl(p), TenSeconds).map { s =>
              mv.addObject("subscription", s)
            }
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
    Await.result(contentRetrievalService.getAllFeedsOrderedByLatestItemDate(Option(loggedInUserFilter.getLoggedInUser)).map {
      feeds =>
        commonAttributesModelBuilder.populateSecondaryFeeds(mv, feeds)
    }, TenSeconds)
  }

  def getViewName(mv: ModelAndView) = "viewfeed"

}
