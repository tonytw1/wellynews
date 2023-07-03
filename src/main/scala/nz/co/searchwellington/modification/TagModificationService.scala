package nz.co.searchwellington.modification

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.queues.ElasticIndexQueue
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.repositories.{HandTaggingService, TagDAO}
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{Await, ExecutionContext, Future}

@Component class TagModificationService @Autowired()(tagDAO: TagDAO,
                                                     handTaggingService: HandTaggingService,
                                                     mongoRepository: MongoRepository,
                                                     elasticIndexQueue: ElasticIndexQueue)
  extends ReasonableWaits {

  private val log = LogFactory.getLog(classOf[TagModificationService])

  def deleteTag(tag: Tag)(implicit ec: ExecutionContext): Boolean = {
    log.info("Deleting tag " + tag.getName)
    val eventualOutcome = handTaggingService.clearTaggingsForTag(tag).flatMap { tagClearanceOutcome =>
      if (tagClearanceOutcome) {
        tagDAO.deleteTag(tag)
      } else {
        Future.successful(tagClearanceOutcome)
      }
    }
    Await.result(eventualOutcome, ThirtySeconds)
  }

  // Reindex resources which may have been effected by a change to a tag
  def updateAffectedResources(tag: Tag, updatedTag: Tag)(implicit ec: ExecutionContext): Future[Int] = {
    val parentHasChanged = tag.parent != updatedTag.parent
    val geocodeChanged = tag.geocode != updatedTag.geocode
    val needToUpdateTagsResource = parentHasChanged || geocodeChanged
    if (needToUpdateTagsResource) {
      mongoRepository.getResourceIdsByTag(tag).map { taggedResourceIds =>
        taggedResourceIds.foreach { rid =>
          elasticIndexQueue.add(rid)
        }
        taggedResourceIds.size
      }.map { numberReindexed =>
        log.info(s"Reindexed $numberReindexed resource after tag parent change")
        numberReindexed
      }
    } else {
      Future.successful(0)
    }
  }

}
