package nz.co.searchwellington.tagging

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.{Newsitem, Tag, Tagged}
import nz.co.searchwellington.repositories.HandTaggingDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{Await, ExecutionContext, Future}

@Component
class ImpliedTagService @Autowired()(mongoRepository: MongoRepository, val handTaggingDAO: HandTaggingDAO)
  extends ReasonableWaits with ResourceTagging {

  def alreadyHasTag(resource: Tagged, tag: Tag)(implicit ec: ExecutionContext): Future[Boolean] = {
    // TODO This should just delegate to the tagging returns officer?
    val eventualIsNewsitemWhosPublisherAlreadyHasThisTag = resource match {
      case n: Newsitem =>
        n.publisher.map { publisherId =>
          mongoRepository.getResourceByObjectId(publisherId).map { publisher =>
            publisher.exists { publisher =>
              Await.result(getHandTagsForResource(publisher), TenSeconds).contains(tag)
            }
          }
        }.getOrElse {
          Future.successful(false)
        }
      case _ =>
        Future.successful(false)
    }

    eventualIsNewsitemWhosPublisherAlreadyHasThisTag.map { isNewsitemWhosPublisherAlreadyHasThisTag =>
      isNewsitemWhosPublisherAlreadyHasThisTag ||
        Await.result(getHandTagsForResource(resource), TenSeconds).contains(tag)  // TODO Await
    }
  }

}