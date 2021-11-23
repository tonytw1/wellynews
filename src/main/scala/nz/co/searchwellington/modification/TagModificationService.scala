package nz.co.searchwellington.modification

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.repositories.elasticsearch.ElasticSearchIndexRebuildService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.repositories.{HandTaggingService, TagDAO}
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global // TODO

@Component class TagModificationService @Autowired()(val tagDAO: TagDAO,
                                                     val handTaggingService: HandTaggingService,
                                                     val mongoRepository: MongoRepository,
                                                     val elasticSearchIndexRebuildService: ElasticSearchIndexRebuildService)
  extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[TagModificationService])

  def deleteTag(tag: Tag): Boolean = {
    log.info("Deleting tag " + tag.getName)
    val eventualOutcome = handTaggingService.clearTaggingsForTag(tag).flatMap { tagClearanceOutcome =>
      if (tag.getParent != null) {
        //tag.getParent.getChildren.remove(tag)
      }
      tagDAO.deleteTag(tag)
    }
    Await.result(eventualOutcome, ThirtySeconds)
  }

  // Reindex resources which may have been effected by a change to a tag
  def updateEffectedResources(tag: Tag, updatedTag: Tag): Unit = {
    val parentHasChanged = tag.parent != updatedTag.parent
    if (parentHasChanged) {
      mongoRepository.getResourceIdsByTag(tag).flatMap { taggedResourceIds =>
        elasticSearchIndexRebuildService.reindexResources(taggedResourceIds)  // TODO unmapped
      }.map { i =>
        log.info("Reindexed resource after tag parent change: " + i)
      }
    }
    // TODO reindex when tag geocode change
  }

}
