package nz.co.searchwellington.repositories

import java.util.List

import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.hibernate.SessionFactory
import org.hibernate.criterion.{Order, Restrictions}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component class TagDAO @Autowired() (sessionFactory: SessionFactory, mongoRepository: MongoRepository) {

  def createNewTag(tagUrlWords: String, displayName: String): Tag = {
    new Tag(name = tagUrlWords, display_name = displayName)
  }

  def loadTagById(tagID: Int): Option[Tag] = {
    mongoRepository.getTagById(tagID)
  }

  def loadTagByName(tagName: String): Option[Tag] = {
    Option(sessionFactory.getCurrentSession.createCriteria(classOf[Tag]).add(Restrictions.eq("name", tagName)).uniqueResult.asInstanceOf[Tag])
  }

  def getAllTags(): Seq[Tag] = {
    mongoRepository.getAllTags()
  }

  def getFeaturedTags: Seq[Tag] = {
    mongoRepository.getFeaturedTags()
  }

  def loadTagsById(tagIds: Seq[Integer]): Seq[Tag] = {
    tagIds.flatMap { id =>
      Option(loadTagById(id))
    }.flatten
  }

  def loadTagsByParent(parentId: Int): Seq[Tag] = {
    mongoRepository.getTagsByParent(parentId)
  }

  @SuppressWarnings(Array("unchecked")) def getTopLevelTags: Seq[Tag] = {
    import scala.collection.JavaConversions._
    sessionFactory.getCurrentSession.createCriteria(classOf[Tag]).add(Restrictions.isNull("parent")).
      addOrder(Order.asc("name")).setCacheable(true).list.asInstanceOf[List[Tag]]
  }

  def saveTag(editTag: Tag) {
    sessionFactory.getCurrentSession.saveOrUpdate(editTag)
    sessionFactory.evictCollection("nz.co.searchwellington.model.Tag.children")
  }

  def deleteTag(tag: Tag) {
    sessionFactory.getCurrentSession.delete(tag)
  }

  def getTagNamesStartingWith(q: String): Seq[String] = {
    import scala.collection.JavaConversions._
    val session = sessionFactory.getCurrentSession
    session.createQuery("select name from nz.co.searchwellington.model.Tag where name like ? order by name").
      setString(0, q + '%').setMaxResults(50).list.asInstanceOf[List[String]]
  }

}
