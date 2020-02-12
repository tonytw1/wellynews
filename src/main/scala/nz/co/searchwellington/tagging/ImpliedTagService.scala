package nz.co.searchwellington.tagging

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.{Newsitem, Tag, Tagged}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component
class ImpliedTagService @Autowired()(taggingReturnsOfficerService: TaggingReturnsOfficerService,
                                     mongoRepository: MongoRepository) extends ReasonableWaits {

  def alreadyHasTag(resource: Tagged, tag: Tag)(implicit ec: ExecutionContext): Future[Boolean] = {
    val eventualIsNewsitemWhosPublisherAlreadyHasThisTag = resource match {
      case n: Newsitem =>
        n.publisher.map { publisherId =>
          mongoRepository.getResourceByObjectId(publisherId).map { publisher =>
            publisher.exists { publisher =>
              taggingReturnsOfficerService.getHandTagsForResource(publisher).contains(tag)
            }
          }
        }.getOrElse {
          Future.successful(false)
        }
      case _ =>
        Future.successful(false)
    }

    eventualIsNewsitemWhosPublisherAlreadyHasThisTag.map { isNewsitemWhosPublisherAlreadyHasThisTag =>
      isNewsitemWhosPublisherAlreadyHasThisTag || taggingReturnsOfficerService.getHandTagsForResource(resource).contains(tag)
    }
  }

}