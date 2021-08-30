package nz.co.searchwellington.repositories

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.{Resource, Tag, Tagging, User}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactivemongo.api.bson.BSONObjectID

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

@Component class HandTaggingService @Autowired()(frontendContentUpdater: FrontendContentUpdater,
                                                 mongoRepository: MongoRepository) extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[HandTaggingService])

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

  def clearTaggingsForTag(tag: Tag): Boolean = {
    def withTagRemoved(resource: Resource, tag: Tag): Resource = {
      val tagsToRetain = resource.resource_tags.filterNot(t => t.tag_id == tag._id)
      resource.withTaggings(tagsToRetain)
    }

    log.info("Clearing tagging votes for tag: " + tag.getName)
    val eventualOutcomes = mongoRepository.getResourceIdsByTag(tag).flatMap { resourceIdsTaggedWithTag =>
      log.info(resourceIdsTaggedWithTag.size + " votes will needs to be cleared and the frontend resources updated.")

      val eventualResources = Future.sequence(resourceIdsTaggedWithTag.map { rid =>
        mongoRepository.getResourceByObjectId(rid)
      }).map(_.flatten)

      eventualResources.flatMap { taggedResources =>
        val eventualDeletions = taggedResources.map { taggedResource =>
          val updatedResource = withTagRemoved(taggedResource, tag)
          mongoRepository.saveResource(updatedResource).flatMap { saveWriteResult =>
            frontendContentUpdater.update(updatedResource)
          }
        }
        Future.sequence(eventualDeletions)
      }
    }

    Await.result(eventualOutcomes, TenSeconds).forall(_ == true)
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
      resource.withTaggings(updatedTaggings)
    }

    val resourcesTaggedByPreviousUser = Await.result(mongoRepository.getResourceIdsByTaggingUser(previousOwner), TenSeconds)

    log.info("Transferring taggings on " + resourcesTaggedByPreviousUser.size + " resources from user " + previousOwner.getName + " to " + newOwner.getName)
    val eventualResources = Future.sequence(resourcesTaggedByPreviousUser.map { rid =>
      mongoRepository.getResourceByObjectId(rid)
    }).map(_.flatten)

    Await.result(eventualResources, TenSeconds).foreach { resource =>
      val updatedResource = transferTaggings(resource)
      mongoRepository.saveResource(updatedResource) // TODO map
      frontendContentUpdater.update(updatedResource)
    }
  }

}