package nz.co.searchwellington.modification

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.repositories.elasticsearch.ElasticSearchIndexRebuildService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.repositories.{HandTaggingService, TagDAO}
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{Await, ExecutionContext, Future}

@Component class TagModificationService @Autowired()(val tagDAO: TagDAO,
                                                     val handTaggingService: HandTaggingService,
                                                     val mongoRepository: MongoRepository,
                                                     val elasticSearchIndexRebuildService: ElasticSearchIndexRebuildService)
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
      mongoRepository.getResourceIdsByTag(tag).flatMap { taggedResourceIds =>
        elasticSearchIndexRebuildService.reindexResources(taggedResourceIds, totalResources = taggedResourceIds.size)
      }.map { numberReindexed =>
        log.info("Reindexed resource after tag parent change: " + numberReindexed)
        numberReindexed
      }
    } else {
      Future.successful(0)
    }
  }

}
