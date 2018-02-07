package nz.co.searchwellington.repositories

import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.hibernate.criterion.Restrictions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class TagDAO @Autowired() (mongoRepository: MongoRepository) {

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

  def getTopLevelTags: Seq[Tag] = {
    getAllTags().filter(t => t.parent.isEmpty)
  }

  def saveTag(editTag: Tag) {
    //sessionFactory.getCurrentSession.saveOrUpdate(editTag)
    //sessionFactory.evictCollection("nz.co.searchwellington.model.Tag.children")
  }

  def deleteTag(tag: Tag) {
    // sessionFactory.getCurrentSession.delete(tag)
  }

  def getTagNamesStartingWith(q: String): Seq[String] = {
    getAllTags().filter(t => t.name.startsWith(q)).map(t => t.name)
  }

}
