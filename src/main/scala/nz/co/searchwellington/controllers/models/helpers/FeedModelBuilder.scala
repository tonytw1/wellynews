package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.LoggedInUserFilter
import nz.co.searchwellington.controllers.models.{GeotaggedNewsitemExtractor, ModelBuilder}
import nz.co.searchwellington.feeds.reading.WhakaokoService
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

  def populateContentModel(request: HttpServletRequest): Future[Option[ModelAndView]] = {

    def populateGeotaggedFeedItems(mv: ModelAndView, feedNewsitems: Seq[FrontendNewsitem]) {
      val geotaggedItems = geotaggedNewsitemExtractor.extractGeotaggedItems(feedNewsitems)
      if (geotaggedItems.nonEmpty) {
        log.info("Adding " + geotaggedItems.size + " geotagged feed items")

        import scala.collection.JavaConverters._

        mv.addObject("geocoded", geotaggedItems.asJava) // TODO deduplicate overlapping
      }
    }

    def populateFeedItems(mv: ModelAndView, feed: Feed) {

      val z: Future[Either[String, Seq[FeedNewsitemForAcceptance]]] = rssfeedNewsitemService.getFeedItemsAndDetailsFor(feed).flatMap { feedItemsForFeed =>
        val x = feedItemsForFeed.fold({ l =>
          Future.successful(Left(l))
        }, {
          result =>
            val feedItems = result._1
            val feedNewsitems = feedItems.map(i => feeditemToNewsitemService.makeNewsitemFromFeedItem(i, feed))
            val eventualWithSuppressionAndLocalCopyInformation = feedNewsItemLocalCopyDecorator.addSupressionAndLocalCopyInformation(feedNewsitems)

            eventualWithSuppressionAndLocalCopyInformation.map { i =>
              Right(i)
            }
        })
        x
      }

      val x = z.map { y =>
        y.fold({
          l =>
            mv.addObject("feed_error", l)
            mv

        }, { result =>
          import scala.collection.JavaConverters._
          mv.addObject(MAIN_CONTENT, result.asJava)
          populateGeotaggedFeedItems(mv, result.map(_.newsitem))
          mv
        })
      }

      Await.result(x, TenSeconds)
    }

    if (isValid(request)) {
      val feedOnRequest = Option(request.getAttribute(FEED_ATTRIBUTE).asInstanceOf[Feed])

      feedOnRequest.map { feed =>
        for {
          maybeSubscription <- feed.page.map { p =>
            whakaokoService.getWhakaokoSubscriptionByUrl(p)
          }.getOrElse {
            Future.successful(None)
          }
        } yield {
          val mv = new ModelAndView().
            addObject("feed", frontendResourceMapper.createFrontendResourceFrom(feed)).
            addObject("subscription", maybeSubscription.orNull)

          commonAttributesModelBuilder.setRss(mv, feed.title.getOrElse(""), feed.page.orNull)
          populateFeedItems(mv, feed) // TODO inline
          Some(mv)
        }

      }.getOrElse {
        Future.successful(None)
      }

    } else {
      Future.successful(None)
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
