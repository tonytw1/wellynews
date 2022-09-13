package nz.co.searchwellington.repositories

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.{Resource, Tag, Tagging, User}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactivemongo.api.bson.BSONObjectID

import scala.concurrent.{ExecutionContext, Future}

@Component class HandTaggingService @Autowired()(contentUpdateService: ContentUpdateService,
                                                 mongoRepository: MongoRepository) extends ReasonableWaits {

  private val log = LogFactory.getLog(classOf[HandTaggingService])

  def addUserTagging(user: User, tag: Tag, resource: Resource): Resource = {
    val existingUserTags = resource.resource_tags.filter(_.user_id == user._id).map(_.tag_id).toSet
    val withNewTagging = existingUserTags + tag._id
    setUsersTagging(user, withNewTagging, resource)
  }

  def setUsersTagging(user: User, tags: Set[BSONObjectID], resource: Resource): Resource = {
    val otherUsersTaggings = resource.resource_tags.filterNot(_.user_id == user._id)
    val usersNewsTaggings = tags.map(t => Tagging(tag_id = t, user_id = user._id))
    resource.withTaggings(otherUsersTaggings ++ usersNewsTaggings)
  }

  def clearTaggingsForTag(tag: Tag)(implicit ec: ExecutionContext): Future[Boolean] = {
    def removeTaggingsForDeletedTag(resource: Resource): Resource = {
      val taggingsToRetain = resource.resource_tags.filterNot(t => t.tag_id == tag._id)
      resource.withTaggings(taggingsToRetain)
    }

    log.info("Clearing tagging votes for tag: " + tag.getName)
    val eventualOutcomes = mongoRepository.getResourceIdsByTag(tag).flatMap { resourceIdsTaggedWithTag =>
      log.info(resourceIdsTaggedWithTag.size + " votes will needs to be cleared and the frontend resources updated.")

      val eventualResources = Future.sequence(resourceIdsTaggedWithTag.map { rid =>
        mongoRepository.getResourceByObjectId(rid)  // TODO don't bring all into memory once
      }).map(_.flatten)

      eventualResources.flatMap { taggedResources =>
        val eventualDeletions = taggedResources.map { taggedResource =>
          updateAndReindexResource(removeTaggingsForDeletedTag, taggedResource)
        }
        Future.sequence(eventualDeletions)
      }
    }

    eventualOutcomes.map(_.forall(_ == true))
  }

  def transferVotes(previousOwner: User, newOwner: User)(implicit ec: ExecutionContext): Future[Boolean] = {
    def transferTaggings(resource: Resource): Resource = {
      val updatedTaggings = resource.resource_tags.map { t =>
        if (t.user_id == previousOwner._id) {
          t.copy(user_id = newOwner._id)
        } else {
          t
        }
      }
      resource.withTaggings(updatedTaggings)
    }

    val eventualTransferOutcomes: Future[Seq[Boolean]] = mongoRepository.getResourceIdsByTaggingUser(previousOwner).flatMap { resourcesTaggedByPreviousUser =>
      log.info("Transferring taggings on " + resourcesTaggedByPreviousUser.size + " resources from user " + previousOwner.getName + " to " + newOwner.getName)
      val eventualResourcesToTransfer = Future.sequence(resourcesTaggedByPreviousUser.map { rid =>
        mongoRepository.getResourceByObjectId(rid)
      }).map(_.flatten)

      eventualResourcesToTransfer.flatMap { resourcesToTransfer =>
        val eventualTransferOutcomes = resourcesToTransfer.map { resource =>
          val updatedResource = transferTaggings(resource)
          mongoRepository.saveResource(updatedResource).flatMap { saveWriteResult =>
            contentUpdateService.update(updatedResource)
          }
        }
        Future.sequence(eventualTransferOutcomes)
      }
    }

    eventualTransferOutcomes.map(_.forall(_ == true))
  }

  private def updateAndReindexResource(update: Resource => Resource, resource: Resource)(implicit ec: ExecutionContext): Future[Boolean] = {
    val updatedResource = update(resource)
    mongoRepository.saveResource(updatedResource).flatMap { saveWriteResult =>
      contentUpdateService.update(updatedResource)
    }
  }

}