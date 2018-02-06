package nz.co.searchwellington.repositories.mongo

import nz.co.searchwellington.model.{NewsitemImpl, Tag, WebsiteImpl}
import org.springframework.stereotype.Component
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, Macros}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

@Component
class MongoRepository {

  def connect(): DefaultDB = {
    val mongoUri = "mongodb://localhost:27017/wellynews"
    val driver = MongoDriver()
    val parsedUri = MongoConnection.parseURI(mongoUri)
    val connection = parsedUri.map(driver.connection(_))

    val futureConnection: Future[MongoConnection] = Future.fromTry(connection)

    def database: Future[DefaultDB] = futureConnection.flatMap(_.database("wellynews"))

    Await.result(database, Duration(1, MINUTES))
  }

  val db: DefaultDB = connect()

  def resourceCollection: BSONCollection = connect().collection("resource")
  def tagCollection: BSONCollection = connect().collection("tag")

  implicit def newsitemReader = Macros.reader[NewsitemImpl]
  implicit def websiteReader = Macros.reader[WebsiteImpl]
  implicit def tagReader: BSONDocumentReader[Tag] = Macros.reader[Tag]

  def getResourceById(id: Int): Option[WebsiteImpl] = {
    val eventualMaybyResource = resourceCollection.find(BSONDocument("id" -> id)).one[WebsiteImpl]
    Await.result(eventualMaybyResource, Duration(10000, MILLISECONDS))
  }

  def getTagById(id: Int): Option[Tag] = {
    val eventualMaybyTag = tagCollection.find(BSONDocument("id" -> id)).one[Tag]
    Await.result(eventualMaybyTag, Duration(10000, MILLISECONDS))
  }

  def getTagsByParent(parent: Int): Seq[Tag] = {
    val eventualMaybyTag = tagCollection.find(BSONDocument("parent" -> parent)).cursor[Tag].toList()
    Await.result(eventualMaybyTag, Duration(10000, MILLISECONDS))
  }

  def getAllTags(): Seq[Tag] = {
    val eventualTags = tagCollection.find(BSONDocument.empty).cursor[Tag].toList()
    Await.result(eventualTags, Duration(10000, MILLISECONDS))
  }

  def getFeaturedTags(): Seq[Tag] = {
    val eventualTags = tagCollection.find(BSONDocument("featured" -> 1)).cursor[Tag].toList()
    Await.result(eventualTags, Duration(10000, MILLISECONDS))
  }

  def getAllNewsitems(): Seq[NewsitemImpl] = {
    Await.result(resourceCollection.find(BSONDocument("type" -> "N")).cursor[NewsitemImpl].toList(), Duration(10000, MILLISECONDS))
  }

  def getAllWebsites(): Seq[WebsiteImpl] = {
    Await.result(resourceCollection.find(BSONDocument("type" -> "W")).cursor[WebsiteImpl].toList(), Duration(10000, MILLISECONDS))
  }

}
