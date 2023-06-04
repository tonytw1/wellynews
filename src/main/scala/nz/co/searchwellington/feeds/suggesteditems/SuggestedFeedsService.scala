package nz.co.searchwellington.feeds.suggesteditems

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.FeedAcceptancePolicy
import nz.co.searchwellington.model.frontend.FrontendFeed
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component
class SuggestedFeedsService @Autowired()(mongoRepository: MongoRepository, frontendResourceMapper: FrontendResourceMapper)
  extends ReasonableWaits {

  def getSuggestedFeedsOrderedByLatestFeeditemDate()(implicit ec: ExecutionContext, currentSpan: Span): Future[Seq[FrontendFeed]] = {
    for {
      allFeeds <- mongoRepository.getAllFeeds
      suggestOnlyFeeds = allFeeds.filter(_.getAcceptancePolicy == FeedAcceptancePolicy.SUGGEST)
      sortedByLatestItemDate <- {
        val eventualSortedFrontend = Future.sequence(suggestOnlyFeeds.sortBy(_.latestItemDate).reverse.map(frontendResourceMapper.createFrontendResourceFrom(_)))
        eventualSortedFrontend.map { rs =>
          rs.flatMap {
            case f: FrontendFeed => Some(f)
            case _ => None
          }
        }
      }
    } yield {
      sortedByLatestItemDate
    }
  }

}
