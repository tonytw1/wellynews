package nz.co.searchwellington.repositories

import nz.co.searchwellington.model.taggingvotes.HandTagging
import nz.co.searchwellington.model.{Resource, Tag, User}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactivemongo.bson.BSONObjectID

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.{Duration, SECONDS}
import scala.concurrent.ExecutionContext.Implicits.global

@Deprecated // "tags are attached to resource document now"
@Component class HandTaggingDAO @Autowired() (mongoRepository: MongoRepository) {
  
  private val log = Logger.getLogger(classOf[HandTaggingDAO])
  private val TenSeconds = Duration(10, SECONDS)

  def getHandTaggingsForResource(resource: Resource): Future[Seq[HandTagging]] = {
    Future.sequence {
      resource.resource_tags.map { tagging =>
        mongoRepository.getTagByObjectId(tagging.tag_id).flatMap { tag =>
          mongoRepository.getUserByObjectId(tagging.user_id).map { user =>
            new HandTagging(user = user.get, tag = tag.get) // TODO Naked gets
          }
        }
      }
    }
  }

  def getHandTaggingsForResourceId(id: BSONObjectID): Future[Seq[HandTagging]] = {
    val resource = Await.result(mongoRepository.getResourceByObjectId(id), TenSeconds).get
    getHandTaggingsForResource(resource)
  }

  def delete(handTagging: HandTagging) {
    //sessionFactory.getCurrentSession.delete(handTagging)
  }

  def getHandpickedTagsForThisResourceByUser(user: User, resource: Resource): Set[Tag] = {
    getHandTaggingsForResourceByUser(resource, user).map(tagging => tagging.tag).toSet
  }

  @SuppressWarnings(Array("unchecked")) def getVotesForTag(tag: Tag): Seq[HandTagging] = {
    //sessionFactory.getCurrentSession.createCriteria(classOf[HandTagging]).add(Restrictions.eq("tag", tag)).setCacheable(true).list.asInstanceOf[java.util.List[HandTagging]]
    Seq() // TODO

  }

  def setUsersTagVotesForResource(resource: Resource, user: User, tags: Set[Tag]) {
    def clearTagsForResourceByUser(resource: Resource, user: User) {
      for (handTagging <- getHandTaggingsForResourceByUser(resource, user)) {
        // sessionFactory.getCurrentSession.delete(handTagging)
      }
    }

    clearTagsForResourceByUser(resource, user)

    for (tag <- tags) {
      this.addTag(user, tag, resource)
    }
  }

  def addTag(user: User, tag: Tag, resource: Resource) {
    val existingVotes = getHandpickedTagsForThisResourceByUser(user, resource)
    if (!existingVotes.contains(tag)) {
      val newTagging = new HandTagging(user = user, tag = tag)
      log.info("Adding new hand tagging: " + newTagging)
      // sessionFactory.getCurrentSession.save(newTagging)
    }
  }

  def clearTags(resource: Resource) {
    for (handTagging <- this.getHandTaggingsForResource(resource)) {
      // sessionFactory.getCurrentSession.delete(handTagging)
    }
  }

  @SuppressWarnings(Array("unchecked")) def getUsersVotes(user: User): Seq[HandTagging] = {
    // sessionFactory.getCurrentSession.createCriteria(classOf[HandTagging]).add(Restrictions.eq("user", user)).setCacheable(true).list.asInstanceOf[java.util.List[HandTagging]]
    Seq() // TODO
  }

  @SuppressWarnings(Array("unchecked")) private def getHandTaggingsForResourceByUser(resource: Resource, user: User): Seq[HandTagging] = {
    // sessionFactory.getCurrentSession.createCriteria(classOf[HandTagging]).add(Restrictions.eq("resource", resource)).add(Restrictions.eq("user", user)).setCacheable(true).list.asInstanceOf[java.util.List[HandTagging]]
    Seq() // TODO
  }

}
