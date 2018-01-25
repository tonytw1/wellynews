package nz.co.searchwellington.repositories

import java.util.List

import nz.co.searchwellington.model.Tag
import org.hibernate.{Session, SessionFactory}
import org.hibernate.criterion.{Order, Restrictions}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component class TagDAO @Autowired() (sessionFactory: SessionFactory) {

  @deprecated def createNewTag: Tag = {
    new Tag(0, "", "", null, new java.util.HashSet[Tag], 0, false, false)
  }

  def createNewTag(tagUrlWords: String, displayName: String): Tag = {
    new Tag(0, tagUrlWords, displayName, null, new java.util.HashSet[Tag], 0, false, false)
  }

  def loadTagById(tagID: Int): Tag = {
    sessionFactory.getCurrentSession.get(classOf[Tag], tagID).asInstanceOf[Tag]
  }

  def loadTagByName(tagName: String): Tag = {
    sessionFactory.getCurrentSession.createCriteria(classOf[Tag]).add(Restrictions.eq("name", tagName)).uniqueResult.asInstanceOf[Tag]
  }

  @SuppressWarnings(Array("unchecked")) def getAllTags: java.util.List[Tag] = {
    sessionFactory.getCurrentSession.createCriteria(classOf[Tag]).addOrder(Order.asc("displayName")).
      setCacheable(true).list.asInstanceOf[List[Tag]]
  }

  def loadTagsById(tagIds: java.util.List[Integer]): java.util.List[Tag] = {
    import scala.collection.JavaConversions._
    tagIds.flatMap { id =>
      Option(loadTagById(id))
    }
  }

  @SuppressWarnings(Array("unchecked")) def getTopLevelTags: java.util.List[Tag] = {
    sessionFactory.getCurrentSession.createCriteria(classOf[Tag]).add(Restrictions.isNull("parent")).
      addOrder(Order.asc("name")).setCacheable(true).list.asInstanceOf[List[Tag]]
  }

  @Transactional def saveTag(editTag: Tag) {
    sessionFactory.getCurrentSession.saveOrUpdate(editTag)
    sessionFactory.evictCollection("nz.co.searchwellington.model.Tag.children")
  }

  @Transactional def deleteTag(tag: Tag) {
    sessionFactory.getCurrentSession.delete(tag)
  }

  def getTagNamesStartingWith(q: String): java.util.List[String] = {
    val session: Session = sessionFactory.getCurrentSession
    session.createQuery("select name from nz.co.searchwellington.model.Tag where name like ? order by name").
      setString(0, q + '%').setMaxResults(50).list.asInstanceOf[List[String]]
  }

  def getFeaturedTags: java.util.List[Tag] = {
    sessionFactory.getCurrentSession.createCriteria(classOf[Tag]).add(Restrictions.eq("featured", true)).addOrder(Order.asc("name")).setCacheable(true).
      list.asInstanceOf[List[Tag]]
  }

}