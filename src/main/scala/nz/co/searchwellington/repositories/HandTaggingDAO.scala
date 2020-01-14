package nz.co.searchwellington.repositories

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.taggingvotes.HandTagging
import nz.co.searchwellington.model._
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactivemongo.bson.BSONObjectID

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.{Duration, SECONDS}
import scala.concurrent.ExecutionContext.Implicits.global

@Deprecated // "tags are attached to resource document now"
@Component class HandTaggingDAO @Autowired()(mongoRepository: MongoRepository) extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[HandTaggingDAO])

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
    val resource = Await.result(mongoRepository.getResourceByObjectId(id), TenSeconds).get
    getHandTaggingsForResource(resource)
  }

  def getHandpickedTagsForThisResourceByUser(user: User, resource: Resource): Set[Tag] = {
    getHandTaggingsForResourceByUser(resource, user).map(tagging => tagging.tag).toSet
  }

  def setUsersTagVotesForResource(resource: Resource, user: User, tags: Set[Tag]) {
    val withoutUsersTaggings = resource.resource_tags.filterNot(_.user_id == user._id).toSet
    val newTaggings = tags.map(t => Tagging(tag_id = t._id, user_id = user._id))
    val withNewTaggings = (withoutUsersTaggings ++ newTaggings).toSeq

    val updated = resource.withTags(withNewTaggings)

    mongoRepository.saveResource(updated).map { _ =>
      //contentUpdater.update(resource) // TODO?
    }
  }

  def clearTags(resource: Resource) {
    for (handTagging <- this.getHandTaggingsForResource(resource)) {
      // sessionFactory.getCurrentSession.delete(handTagging)
    }
  }

  @SuppressWarnings(Array("unchecked")) private def getHandTaggingsForResourceByUser(resource: Resource, user: User): Seq[HandTagging] = {
    // sessionFactory.getCurrentSession.createCriteria(classOf[HandTagging]).add(Restrictions.eq("resource", resource)).add(Restrictions.eq("user", user)).setCacheable(true).list.asInstanceOf[java.util.List[HandTagging]]
    Seq() // TODO
  }

}
