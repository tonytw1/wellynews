package nz.co.searchwellington.repositories

import nz.co.searchwellington.model.taggingvotes.HandTagging
import nz.co.searchwellington.model.{Resource, Tag, User}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.hibernate.criterion.Restrictions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class HandTaggingDAO @Autowired() (mongoRepository: MongoRepository) {
  
  private val log = Logger.getLogger(classOf[HandTaggingDAO])

  @SuppressWarnings(Array("unchecked")) def getHandTaggingsForResource(resource: Resource): Seq[HandTagging] = {
    //sessionFactory.getCurrentSession.createCriteria(classOf[HandTagging]).add(Restrictions.eq("resource", resource)).setCacheable(true).list.asInstanceOf[java.util.List[HandTagging]]
    Seq() // TODO
  }

  def delete(handTagging: HandTagging) {
    //sessionFactory.getCurrentSession.delete(handTagging)
  }

  def getHandpickedTagsForThisResourceByUser(user: User, resource: Resource): Set[Tag] = {
    getHandTaggingsForResourceByUser(resource, user).map(tagging => tagging.getTag).toSet
  }

  @SuppressWarnings(Array("unchecked")) def getVotesForTag(tag: Tag): Seq[HandTagging] = {
    //sessionFactory.getCurrentSession.createCriteria(classOf[HandTagging]).add(Restrictions.eq("tag", tag)).setCacheable(true).list.asInstanceOf[java.util.List[HandTagging]]
    Seq() // TODO

  }

  def setUsersTagVotesForResource(editResource: Resource, user: User, tags: Set[Tag]) {
    this.clearTagsForResourceByUser(editResource, user)
    for (tag <- tags) {
      this.addTag(user, tag, editResource)
    }
  }

  def addTag(user: User, tag: Tag, resource: Resource) {
    val existingVotes = getHandpickedTagsForThisResourceByUser(user, resource)
    if (!existingVotes.contains(tag)) {
      val newTagging: HandTagging = new HandTagging(0, resource, user, tag)
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

  private def clearTagsForResourceByUser(resource: Resource, user: User) {
    for (handTagging <- getHandTaggingsForResourceByUser(resource, user)) {
      // sessionFactory.getCurrentSession.delete(handTagging)
    }
  }

  @SuppressWarnings(Array("unchecked")) private def getHandTaggingsForResourceByUser(resource: Resource, user: User): Seq[HandTagging] = {
    // sessionFactory.getCurrentSession.createCriteria(classOf[HandTagging]).add(Restrictions.eq("resource", resource)).add(Restrictions.eq("user", user)).setCacheable(true).list.asInstanceOf[java.util.List[HandTagging]]
    Seq() // TODO
  }

}