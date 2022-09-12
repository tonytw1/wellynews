package nz.co.searchwellington.controllers.models.helpers

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.models.GeotaggedNewsitemExtractor
import nz.co.searchwellington.feeds.whakaoko.{WhakaokoFeedReader, WhakaokoService}
import nz.co.searchwellington.feeds.{FeedItemActionDecorator, FeeditemToNewsitemService}
import nz.co.searchwellington.model.frontend.{FrontendNewsitem, FrontendResource}
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.{Feed, User}
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.ui.ModelMap

import javax.servlet.http.HttpServletRequest
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

@Component class FeedModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService,
                                               geotaggedNewsitemExtractor: GeotaggedNewsitemExtractor,
                                               feedNewsItemLocalCopyDecorator: FeedItemActionDecorator,
                                               frontendResourceMapper: FrontendResourceMapper,
                                               commonAttributesModelBuilder: CommonAttributesModelBuilder,
                                               feeditemToNewsitemService: FeeditemToNewsitemService,
                                               whakaokoFeedReader: WhakaokoFeedReader,
                                               whakaokoService: WhakaokoService) extends ModelBuilder with ReasonableWaits {

  private val log = LogFactory.getLog(classOf[FeedModelBuilder])
  private val FEED_ATTRIBUTE = "feedAttribute"

  def isValid(request: HttpServletRequest): Boolean = {
    request.getAttribute(FEED_ATTRIBUTE) != null
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
        }, { feedItems =>
            val feedNewsitems = feedItems._1.flatMap(i => feeditemToNewsitemService.makeNewsitemFromFeedItem(i, feed))
            val totalCount = feedItems._2

            val eventualFrontendFeedNewitems = Future.sequence {
              feedNewsitems.map { r =>
                frontendResourceMapper.mapFrontendResource(r, r.geocode, Seq.empty, Seq.empty, loggedInUser)
              }
            }

            val eventualWithSuppressionAndLocalCopyInformation: Future[Seq[FrontendResource]] = eventualFrontendFeedNewitems.flatMap { feedItems =>
              feedNewsItemLocalCopyDecorator.withFeedItemSpecificActions(feedItems, loggedInUser).map{ feedItems =>
                // Remove tags
                feedItems.map { feedItem: FrontendResource =>
                  feedItem match {
                    case n: FrontendNewsitem => n.copy(tags = None, handTags = None)
                  }
                }
              }
            }

            eventualWithSuppressionAndLocalCopyInformation.map { i =>
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

    val feedOnRequest = Option(request.getAttribute(FEED_ATTRIBUTE).asInstanceOf[Feed])

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
