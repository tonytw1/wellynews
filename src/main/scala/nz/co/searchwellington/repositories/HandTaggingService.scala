package nz.co.searchwellington.repositories

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.{Feed, Newsitem, Resource, Tag, User, Website}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

@Component class HandTaggingService @Autowired()(handTaggingDao: HandTaggingDAO, frontendContentUpdater: FrontendContentUpdater,
                                                 mongoRepository: MongoRepository) extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[HandTaggingService])

  def clearTaggingsForTag(tag: Tag) {

    def deleteTagFromResource(tag: Tag, resource: Resource): Resource = {
      resource match {  // TODO how to remove this?
        case w: Website =>
          w.copy(resource_tags = w.resource_tags.filterNot(t => t.tag_id == tag._id))
        case n: Newsitem =>
          n.copy(resource_tags = n.resource_tags.filterNot(t => t.tag_id == tag._id))
        case f: Feed =>
          f.copy(resource_tags = f.resource_tags.filterNot(t => t.tag_id == tag._id))
        case _ =>
          resource
      }
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

      resource match {  // TODO how to remove this?
        case w: Website =>
          w.copy(resource_tags = updatedTaggings)
        case n: Newsitem =>
          n.copy(resource_tags = updatedTaggings)
        case f: Feed =>
          f.copy(resource_tags = updatedTaggings)
        case _ =>
          resource
      }
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