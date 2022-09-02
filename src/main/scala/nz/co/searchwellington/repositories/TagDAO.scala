package nz.co.searchwellington.repositories

import java.util.UUID
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactivemongo.api.bson.BSONObjectID

import scala.concurrent.{ExecutionContext, Future}

@Component class TagDAO @Autowired() (mongoRepository: MongoRepository) extends ReasonableWaits {

  def createNewTag(tagUrlWords: String, displayName: String): Tag = {
    Tag(id = UUID.randomUUID().toString, name = tagUrlWords, display_name = displayName)
  }

  def loadTagByObjectId(objectId: BSONObjectID)(implicit ec: ExecutionContext): Future[Option[Tag]] = {
    mongoRepository.getTagByObjectId(objectId)
  }

  def getAllTags()(implicit ec: ExecutionContext): Future[Seq[Tag]] = mongoRepository.getAllTags()

  def getFeaturedTags()(implicit ec: ExecutionContext): Future[Seq[Tag]] = getAllTags.map(ts => ts.filter(t => t.isFeatured))

  def loadTagsById(tagIds: Seq[String])(implicit ec: ExecutionContext): Future[Seq[Tag]] = {
    Future.sequence(tagIds.map(mongoRepository.getTagById)).map(_.flatten)
  }

  def loadTagsByParent(parentId: BSONObjectID)(implicit ec: ExecutionContext): Future[Seq[Tag]] = {
    mongoRepository.getTagsByParent(parentId)
  }

  def getTopLevelTags()(implicit ec: ExecutionContext): Future[Seq[Tag]] = {
    getAllTags.map(ts => ts.filter(t => t.parent.isEmpty))
  }

  def deleteTag(tag: Tag)(implicit ec: ExecutionContext): Future[Boolean] = {
    mongoRepository.deleteTag(tag).map { writeResult =>
      writeResult.writeErrors.isEmpty
    }
  }

  def getTagDisplayNamesStartingWith(q: String)(implicit ec: ExecutionContext): Future[Seq[String]] = {
    val asLowercase = q.toLowerCase()
    getAllTags.map(ts => ts.filter(t => t.display_name.toLowerCase.startsWith(asLowercase)).map(t => t.display_name))
  }

}
