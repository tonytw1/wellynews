package nz.co.searchwellington.feeds.whakaoko

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.whakaoko.model.FeedItem
import nz.co.searchwellington.model.Feed
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component class WhakaokoFeedReader @Autowired()(whakaokoService: WhakaokoService) extends ReasonableWaits {

  private val log = LogFactory.getLog(classOf[WhakaokoFeedReader])

  def fetchFeedItems(feed: Feed)(implicit ec: ExecutionContext, currentSpan: Span): Future[Either[String, (Seq[FeedItem], Long)]] = {
    feed.whakaokoSubscription.map { subscriptionId =>
      log.debug("Feed mapped to whakaoko subscription: " + subscriptionId)
      whakaokoService.getSubscriptionFeedItems(subscriptionId)
    }.getOrElse {
      log.warn("No whakaoko subscription found for feed: " + feed)
      Future.successful(Left("No whakaoko subscription found for feed"))
    }

  }

}
