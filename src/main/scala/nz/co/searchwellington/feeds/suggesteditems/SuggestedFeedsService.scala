package nz.co.searchwellington.feeds.suggesteditems

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.FeedAcceptancePolicy
import nz.co.searchwellington.model.frontend.{FrontendFeed, FrontendResource}
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component
class SuggestedFeedsService @Autowired()(contentRetrievalService: ContentRetrievalService) extends ReasonableWaits {

  def getSuggestedFeedsOrderedByLatestFeeditemDate()(implicit ec: ExecutionContext, currentSpan: Span): Future[Seq[FrontendFeed]] = {
    val eventualSuggestOnlyFeeds = contentRetrievalService.getFeeds(loggedInUser = None, acceptancePolicy = Some(FeedAcceptancePolicy.SUGGEST)).map { rs =>
      rs.flatMap {
        case f: FrontendFeed => Some(f)
        case _ => None
      }
    }
    eventualSuggestOnlyFeeds.map { rs =>
      rs.filter(_.latestItemDate != null).sortBy(_.latestItemDate).reverse
    }
  }

}
