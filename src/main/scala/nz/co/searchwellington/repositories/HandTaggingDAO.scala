package nz.co.searchwellington.repositories

import java.util.Set

import com.google.common.collect.Sets
import nz.co.searchwellington.model.{Resource, Tag, User}
import nz.co.searchwellington.model.taggingvotes.HandTagging
import org.apache.log4j.Logger
import org.hibernate.SessionFactory
import org.hibernate.criterion.Restrictions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component class HandTaggingDAO @Autowired() (sessionFactory: SessionFactory) {
  
  private val log: Logger = Logger.getLogger(classOf[HandTaggingDAO])

  @SuppressWarnings(Array("unchecked")) def getHandTaggingsForResource(resource: Resource): List[HandTagging] = {
    sessionFactory.getCurrentSession.createCriteria(classOf[HandTagging]).add(Restrictions.eq("resource", resource)).
      setCacheable(true).list.asInstanceOf[List[HandTagging]]
  }

  @Transactional def delete(handTagging: HandTagging) {
    sessionFactory.getCurrentSession.delete(handTagging)
  }

  def getHandpickedTagsForThisResourceByUser(user: User, resource: Resource): Set[Tag] = {
    if (user == null) {
      Sets.newHashSet()
    } else {
      import scala.collection.JavaConversions._
      getHandTaggingsForResourceByUser(resource, user).map(tagging => tagging.getTag).toSet
    }
  }

  @SuppressWarnings(Array("unchecked")) def getVotesForTag(tag: Tag): List[HandTagging] = {
    sessionFactory.getCurrentSession.createCriteria(classOf[HandTagging]).add(Restrictions.eq("tag", tag)).
      setCacheable(true).list.asInstanceOf[List[HandTagging]]
  }

  @Transactional def setUsersTagVotesForResource(editResource: Resource, user: User, tags: Set[Tag]) {
    this.clearTagsForResourceByUser(editResource, user)
    import scala.collection.JavaConversions._
    for (tag <- tags) {
      this.addTag(user, tag, editResource)
    }
  }

  @Transactional def addTag(user: User, tag: Tag, resource: Resource) {
    val existingVotes = getHandpickedTagsForThisResourceByUser(user, resource)
    if (!existingVotes.contains(tag)) {
      val newTagging: HandTagging = new HandTagging(0, resource, user, tag)
      log.info("Adding new hand tagging: " + newTagging)
      sessionFactory.getCurrentSession.save(newTagging)
    }
  }

  def clearTags(resource: Resource) {
    for (handTagging <- this.getHandTaggingsForResource(resource)) {
      sessionFactory.getCurrentSession.delete(handTagging)
    }
  }

  @SuppressWarnings(Array("unchecked")) def getUsersVotes(user: User): List[HandTagging] = {
    sessionFactory.getCurrentSession.createCriteria(classOf[HandTagging]).add(Restrictions.eq("user", user)).
      setCacheable(true).list.asInstanceOf[List[HandTagging]]
  }

  private def clearTagsForResourceByUser(resource: Resource, user: User) {
    for (handTagging <- this.getHandTaggingsForResourceByUser(resource, user)) {
      sessionFactory.getCurrentSession.delete(handTagging)
    }
  }

  @SuppressWarnings(Array("unchecked")) private def getHandTaggingsForResourceByUser(resource: Resource, user: User): Seq[HandTagging] = {
    sessionFactory.getCurrentSession.createCriteria(classOf[HandTagging]).add(Restrictions.eq("resource", resource)).
      add(Restrictions.eq("user", user)).setCacheable(true).list.asInstanceOf[List[HandTagging]]
  }
}