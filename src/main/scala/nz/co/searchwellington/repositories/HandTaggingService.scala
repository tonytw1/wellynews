package nz.co.searchwellington.repositories

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.{Resource, Tag, Tagging, User}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

@Component class HandTaggingService @Autowired()(handTaggingDao: HandTaggingDAO, frontendContentUpdater: FrontendContentUpdater,
                                                 mongoRepository: MongoRepository) extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[HandTaggingService])

  def addTag(user: User, tag: Tag, resource: Resource): Unit = {
    val newTagging = Tagging(user_id = user._id, tag_id= tag._id)

    if (!resource.resource_tags.contains(newTagging)) {
      log.info("Adding new tagging: " + newTagging)
      val updatedTaggings = resource.resource_tags :+ newTagging
      mongoRepository.saveResource(resource.withTags(updatedTaggings))
    }
  }

  def clearTaggingsForTag(tag: Tag) {

    def deleteTagFromResource(tag: Tag, resource: Resource): Resource = {
      val filtered = resource.resource_tags.filterNot(t => t.tag_id == tag._id)
      resource.withTags(filtered)
    }

    log.info("Clearing tagging votes for tag: " + tag.getName)
    val resourceIdsTaggedWithTag = Await.result(mongoRepository.getResourceIdsByTag(tag), TenSeconds)
    log.info(resourceIdsTaggedWithTag.size + " votes will needs to be cleared and the frontend resources updated.")

    val eventualResources  = Future.sequence(resourceIdsTaggedWithTag.map { rid =>
      mongoRepository.getResourceByObjectId(rid)
    }).map( _.flatten)

    Await.result(eventualResources, TenSeconds).foreach { taggedResource =>
      val updatedResource = deleteTagFromResource(tag, taggedResource)
      mongoRepository.saveResource(updatedResource) // TODO Map
      frontendContentUpdater.update(updatedResource)
    }
  }

  def transferVotes(previousOwner: User, newOwner: User) {

    def transferTaggings(resource: Resource): Resource = {
      val updatedTaggings = resource.resource_tags.map { t =>
        if (t.user_id == previousOwner._id) {
          t.copy(user_id = newOwner._id)
        } else {
          t
        }
      }
      resource.withTags(updatedTaggings)
    }

    val resourcesTaggedByPreviousUser = Await.result(mongoRepository.getResourceIdsByTaggingUser(previousOwner), TenSeconds)

    log.info("Transferring taggings on " + resourcesTaggedByPreviousUser.size + " resources from user " + previousOwner.getName + " to " + newOwner.getName)
    val eventualResources  = Future.sequence(resourcesTaggedByPreviousUser.map { rid =>
      mongoRepository.getResourceByObjectId(rid)
    }).map( _.flatten)

    Await.result(eventualResources, TenSeconds).foreach { resource =>
      val updatedResource = transferTaggings(resource)
      mongoRepository.saveResource(updatedResource) // TODO map
      frontendContentUpdater.update(updatedResource)
    }
  }

}