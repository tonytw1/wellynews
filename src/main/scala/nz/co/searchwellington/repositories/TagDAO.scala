package nz.co.searchwellington.repositories

import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, SECONDS}

@Component class TagDAO @Autowired() (mongoRepository: MongoRepository) {

  def createNewTag(tagUrlWords: String, displayName: String): Tag = {
    new Tag(name = tagUrlWords, display_name = displayName)
  }

  def loadTagById(tagID: Int): Option[Tag] = {
    Await.result(mongoRepository.getTagById(tagID), Duration(10, SECONDS))
  }

  def loadTagByName(name: String): Option[Tag] = {
    Await.result(mongoRepository.getTagByName(name), Duration(10, SECONDS))
  }

  def getAllTags(): Seq[Tag] = {
    Await.result(mongoRepository.getAllTags(), Duration(10, SECONDS))
  }

  def getFeaturedTags: Seq[Tag] = {
    getAllTags().filter(t => t.isFeatured)
  }

  def loadTagsById(tagIds: Seq[Integer]): Seq[Tag] = {
    tagIds.flatMap { id =>
      Option(loadTagById(id))
    }.flatten
  }

  def loadTagsByParent(parentId: Int): Seq[Tag] = {
    Await.result(mongoRepository.getTagsByParent(parentId), Duration(10, SECONDS))
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
