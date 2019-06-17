package nz.co.searchwellington.repositories

import java.util.UUID

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactivemongo.bson.BSONObjectID

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

@Component class TagDAO @Autowired() (mongoRepository: MongoRepository) extends ReasonableWaits {

  def createNewTag(tagUrlWords: String, displayName: String): Tag = {
    new Tag(id = UUID.randomUUID().toString, name = tagUrlWords, display_name = displayName)
  }

  def loadTagById(tagId: String): Option[Tag] = {
    Await.result(mongoRepository.getTagById(tagId), TenSeconds)
  }

  def loadTagByObjectId(objectId: BSONObjectID): Option[Tag] = {
    Await.result(mongoRepository.getTagByObjectId(objectId), TenSeconds)
  }

  def getAllTags: Future[Seq[Tag]] = mongoRepository.getAllTags()

  def getFeaturedTags: Future[Seq[Tag]] = getAllTags.map(ts => ts.filter(t => t.isFeatured))

  def loadTagsById(tagIds: Seq[String]): Seq[Tag] = {
    tagIds.flatMap { id =>
      Option(loadTagById(id))
    }.flatten
  }

  def loadTagsByParent(parentId: BSONObjectID): Future[List[Tag]] = {
    mongoRepository.getTagsByParent(parentId)
  }

  def getTopLevelTags: Future[Seq[Tag]] = {
    getAllTags.map(ts => ts.filter(t => t.parent.isEmpty))
  }

  def deleteTag(tag: Tag) {
    // sessionFactory.getCurrentSession.delete(tag)
  }

  def getTagNamesStartingWith(q: String): Future[Seq[String]] = getAllTags.map(ts => ts.filter(t => t.name.startsWith(q)).map(t => t.name))

}
