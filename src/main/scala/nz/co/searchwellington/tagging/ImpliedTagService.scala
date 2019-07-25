package nz.co.searchwellington.tagging

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.{Newsitem, Tag, Tagged}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{Await, ExecutionContext}

@Component
class ImpliedTagService @Autowired()(taggingReturnsOfficerService: TaggingReturnsOfficerService,
                                     mongoRepository: MongoRepository) extends ReasonableWaits {

  def alreadyHasTag(resource: Tagged, tag: Tag)(implicit ec: ExecutionContext): Boolean = {
    val isNewsitemWhosPublisherAlreadyHasThisTag = resource match {
      case n: Newsitem =>
        n.publisher.exists { publisherId =>
          Await.result(mongoRepository.getResourceByObjectId(publisherId), TenSeconds).exists { publisher =>
            taggingReturnsOfficerService.getHandTagsForResource(publisher).contains(tag)
          }
        }
      case _ =>
        false
    }

    isNewsitemWhosPublisherAlreadyHasThisTag || taggingReturnsOfficerService.getHandTagsForResource(resource).contains(tag)
  }

}