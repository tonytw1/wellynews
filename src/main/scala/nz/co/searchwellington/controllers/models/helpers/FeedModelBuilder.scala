package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.models.{GeotaggedNewsitemExtractor, ModelBuilder}
import nz.co.searchwellington.feeds.whakaoko.WhakaokoService
import nz.co.searchwellington.feeds.{FeedItemLocalCopyDecorator, FeeditemToNewsitemService, RssfeedNewsitemService}
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.{Feed, User}
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component class FeedModelBuilder @Autowired()(rssfeedNewsitemService: RssfeedNewsitemService,
                                               val contentRetrievalService: ContentRetrievalService,
                                               geotaggedNewsitemExtractor: GeotaggedNewsitemExtractor, feedNewsItemLocalCopyDecorator: FeedItemLocalCopyDecorator,
                                               frontendResourceMapper: FrontendResourceMapper,
                                               commonAttributesModelBuilder: CommonAttributesModelBuilder,
                                               feeditemToNewsitemService: FeeditemToNewsitemService,
                                               whakaokoService: WhakaokoService) extends ModelBuilder with ReasonableWaits {

  private val log = Logger.getLogger(classOf[FeedModelBuilder])
  private val FEED_ATTRIBUTE = "feedAttribute"

  def isValid(request: HttpServletRequest): Boolean = {
    request.getAttribute(FEED_ATTRIBUTE) != null
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User]): Future[Option[ModelAndView]] = {

    def populateGeotaggedFeedItems(mv: ModelAndView, feedNewsitems: Seq[FrontendResource]) {
      val geotaggedItems = geotaggedNewsitemExtractor.extractGeotaggedItems(feedNewsitems)
      if (geotaggedItems.nonEmpty) {
        log.info("Adding " + geotaggedItems.size + " geotagged feed items")

        import scala.collection.JavaConverters._

        mv.addObject("geocoded", geotaggedItems.asJava) // TODO deduplicate overlapping
      }
    }

    def feedItemsFor(feed: Feed): Future[Either[String, (Seq[FrontendResource], Long)]] = {
      rssfeedNewsitemService.getFeedItemsAndDetailsFor(feed).flatMap { feedItemsForFeed =>
        feedItemsForFeed.fold({ l =>
          Future.successful(Left(l))
        }, {
          result =>
            val feedItems = result._1
            val feedNewsitems = feedItems._1.map(i => feeditemToNewsitemService.makeNewsitemFromFeedItem(i, feed))
            val totalCount = feedItems._2

            val eventualFrontendFeedNewitems = Future.sequence {
              feedNewsitems.map { r =>
                frontendResourceMapper.mapFrontendResource(r, r.geocode)
              }
            }

            val eventualWithSuppressionAndLocalCopyInformation: Future[Seq[FrontendResource]] = eventualFrontendFeedNewitems.flatMap { rs =>
              feedNewsItemLocalCopyDecorator.withFeedItemSpecificActions(rs, loggedInUser)
            }

            eventualWithSuppressionAndLocalCopyInformation.map { i =>
              Right((i, totalCount))
            }
        })
      }
    }

    def populateFeedItems(mv: ModelAndView, feedItems: Either[String, (Seq[FrontendResource], Long)]): ModelAndView = {
      feedItems.fold({
        l =>
          mv.addObject("feed_error", l)
          mv
      }, { result =>
        import scala.collection.JavaConverters._
        mv.addObject(MAIN_CONTENT, result._1.asJava)
        populateGeotaggedFeedItems(mv, result._1)
        mv.addObject("feed_total_count", result._2)
        mv
      })
    }

    val feedOnRequest = Option(request.getAttribute(FEED_ATTRIBUTE).asInstanceOf[Feed])

    feedOnRequest.map { feed =>
      val eventualFrontendFeed = frontendResourceMapper.createFrontendResourceFrom(feed, loggedInUser)
      val eventualFeedItems = feedItemsFor(feed)
      for {
        frontendFeed <- eventualFrontendFeed
        feedItems <- eventualFeedItems
        maybeSubscription <- whakaokoService.getWhakaokoSubscriptionByUrl(feed.page)
      } yield {
        val mv = new ModelAndView().
          addObject("feed", frontendFeed).
          addObject("subscription", maybeSubscription.orNull)

        commonAttributesModelBuilder.setRss(mv, feed.title.getOrElse(""), feed.page)
        populateFeedItems(mv, feedItems) // TODO inline
        Some(mv)
      }

    }.getOrElse {
      Future.successful(None)
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView, loggedInUser: Option[User]): Future[ModelAndView] = {
    contentRetrievalService.getAllFeedsOrderedByLatestItemDate(loggedInUser).map { feeds =>
      commonAttributesModelBuilder.withSecondaryFeeds(mv, feeds)
    }
  }

  def getViewName(mv: ModelAndView) = "viewfeed"

}
