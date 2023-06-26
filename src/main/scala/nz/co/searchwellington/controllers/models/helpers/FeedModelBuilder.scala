package nz.co.searchwellington.controllers.models.helpers

import io.opentelemetry.api.trace.Span
import jakarta.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.models.GeotaggedNewsitemExtractor
import nz.co.searchwellington.feeds.FeedItemActionDecorator
import nz.co.searchwellington.feeds.whakaoko.model.FeedItem
import nz.co.searchwellington.feeds.whakaoko.{WhakaokoFeedReader, WhakaokoService}
import nz.co.searchwellington.filters.attributesetters.FeedAttributeSetter
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.{Feed, User}
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.ui.ModelMap

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

@Component class FeedModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService,
                                               geotaggedNewsitemExtractor: GeotaggedNewsitemExtractor,
                                               feedNewsItemLocalCopyDecorator: FeedItemActionDecorator,
                                               frontendResourceMapper: FrontendResourceMapper,
                                               commonAttributesModelBuilder: CommonAttributesModelBuilder,
                                               whakaokoFeedReader: WhakaokoFeedReader,
                                               whakaokoService: WhakaokoService) extends ModelBuilder with ReasonableWaits {

  private val log = LogFactory.getLog(classOf[FeedModelBuilder])

  def isValid(request: HttpServletRequest): Boolean = {
    request.getAttribute(FeedAttributeSetter.FEED_ATTRIBUTE) != null
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[Option[ModelMap]] = {

    def populateGeotaggedFeedItems(mv: ModelMap, feedNewsitems: Seq[FrontendResource]): Unit = {
      val geotaggedItems = geotaggedNewsitemExtractor.extractGeotaggedItems(feedNewsitems)
      if (geotaggedItems.nonEmpty) {
        log.info("Adding " + geotaggedItems.size + " geotagged feed items")
        mv.addAttribute("geocoded", geotaggedItems.asJava) // TODO deduplicate overlapping
      }
    }

    def feedItemsFor(feed: Feed): Future[Either[String, (Seq[FrontendResource], Long)]] = {
      whakaokoFeedReader.fetchFeedItems(feed).flatMap { feedItemsForFeed =>
        feedItemsForFeed.fold({ l =>
          Future.successful(Left(l))
        }, { feedItems: (Seq[FeedItem], Long) =>
          val totalCount = feedItems._2

          val eventualFrontendFeedItems = Future.sequence {
            feedItems._1.map { feedItem =>
              frontendResourceMapper.mapFeedItem(feed, feedItem)
            }
          }
          val eventualWithActions = eventualFrontendFeedItems.flatMap { frontendFeedItems =>
            Future.sequence {
              frontendFeedItems.map { feedItem =>
                feedNewsItemLocalCopyDecorator.withFeedItemSpecificActions(feed, feedItem, loggedInUser)
              }
            }
          }

          eventualWithActions.map { i =>
            Right((i, totalCount))
          }
        })
      }
    }

    def populateFeedItems(mv: ModelMap, feedItems: Either[String, (Seq[FrontendResource], Long)]): ModelMap = {
      feedItems.fold({
        l =>
          mv.addAttribute("feed_error", l)
          mv
      }, { result =>
        mv.addAttribute(MAIN_CONTENT, result._1.asJava)
        populateGeotaggedFeedItems(mv, result._1)
        mv.addAttribute("feed_total_count", result._2)
        mv
      })
    }

    val feedOnRequest = Option(request.getAttribute(FeedAttributeSetter.FEED_ATTRIBUTE).asInstanceOf[Feed])

    feedOnRequest.map { feed =>
      val eventualFrontendFeed = frontendResourceMapper.createFrontendResourceFrom(feed, loggedInUser)
      val eventualFeedItems = feedItemsFor(feed)
      val eventualGetSubscriptionResult = feed.whakaokoSubscription.map(subscriptionId => whakaokoService.getSubscription(subscriptionId)).getOrElse(Future.successful(Right(None)))

      for {
        frontendFeed <- eventualFrontendFeed
        feedItems <- eventualFeedItems
        getSubscriptionResult <- eventualGetSubscriptionResult

      } yield {
        val maybeMaybeSubscription = getSubscriptionResult.toOption.flatten

        val mv = new ModelMap().
          addAttribute("feed", frontendFeed).
          addAttribute("subscription", maybeMaybeSubscription.orNull)

        commonAttributesModelBuilder.setRss(mv, feed.title, feed.page)
        populateFeedItems(mv, feedItems) // TODO inline
        Some(mv)
      }

    }.getOrElse {
      Future.successful(None)
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[ModelMap] = {
    for {
      feedsOrderedByLatestItemDate <- contentRetrievalService.getAllFeedsOrderedByLatestItemDate(loggedInUser)
    } yield {
      new ModelMap().
        addAllAttributes(commonAttributesModelBuilder.secondaryFeeds(feedsOrderedByLatestItemDate))
    }
  }

  def getViewName(mv: ModelMap, loggedInUser: Option[User]): String = "viewfeed"

}
