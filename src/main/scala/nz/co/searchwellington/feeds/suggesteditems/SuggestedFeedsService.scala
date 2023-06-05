package nz.co.searchwellington.feeds.suggesteditems

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.User
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component
class SuggestedFeedsService @Autowired()(contentRetrievalService: ContentRetrievalService) extends ReasonableWaits {

  def getSuggestedFeedsOrderedByLatestFeeditemDate(loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[Seq[FrontendResource]] = {
    contentRetrievalService.getSuggestOnlyFeeds(loggedInUser = loggedInUser)
  }

}
