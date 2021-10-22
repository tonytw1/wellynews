package nz.co.searchwellington.repositories

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model._
import nz.co.searchwellington.model.taggingvotes.HandTagging
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactivemongo.api.bson.BSONObjectID

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Deprecated // "tags are attached to resource document now"
@Component class HandTaggingDAO @Autowired()(mongoRepository: MongoRepository) extends ReasonableWaits {

  def getHandTaggingsForResource(resource: Tagged): Future[Seq[HandTagging]] = {
    Future.sequence {
      resource.resource_tags.map { tagging =>
        mongoRepository.getTagByObjectId(tagging.tag_id).flatMap { tag =>
          mongoRepository.getUserByObjectId(tagging.user_id).map { user =>
            HandTagging(user = user.get, tag = tag.get) // TODO Naked gets
          }
        }
      }
    }
  }

  def getHandTaggingsForResourceId(id: BSONObjectID): Future[Seq[HandTagging]] = {
    mongoRepository.getResourceByObjectId(id).flatMap { maybeResource =>
      maybeResource.map { resource =>
        getHandTaggingsForResource(resource)
      }.getOrElse {
        Future.successful(Seq.empty)
      }
    }
  }

  def getHandpickedTagsForThisResourceByUser(user: User, resource: Resource): Set[Tag] = {
    getHandTaggingsForResourceByUser(resource, user).map(tagging => tagging.tag).toSet
  }

  private def getHandTaggingsForResourceByUser(resource: Resource, user: User): Seq[HandTagging] = {
    // sessionFactory.getCurrentSession.createCriteria(classOf[HandTagging]).add(Restrictions.eq("resource", resource)).add(Restrictions.eq("user", user)).setCacheable(true).list.asInstanceOf[java.util.List[HandTagging]]
    Seq() // TODO
  }

}
