package nz.co.searchwellington.repositories.mongo

import nz.co.searchwellington.model._
import org.springframework.stereotype.Component
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONString, Macros}

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
  def taggingCollection: BSONCollection = connect().collection("resource_tags")

  implicit def feedReader = Macros.reader[FeedImpl]
  implicit def newsitemReader = Macros.reader[NewsitemImpl]
  implicit def websiteReader = Macros.reader[WebsiteImpl]
  implicit def watchlistReader = Macros.reader[Watchlist]
  implicit def tagReader: BSONDocumentReader[Tag] = Macros.reader[Tag]
  implicit def tagggingReader: BSONDocumentReader[Tagging] = Macros.reader[Tagging]

  def getResourceById(id: Int): Future[Option[Resource]] = {
    resourceCollection.find(BSONDocument("id" -> id)).one[BSONDocument].map { bo =>
      bo.flatMap { b =>
        b.get("type").get match {
          case BSONString("N") => Some(b.as[NewsitemImpl])
          case BSONString("W") => Some(b.as[WebsiteImpl])
          case BSONString("F") => Some(b.as[FeedImpl])
          case BSONString("L") => Some(b.as[Watchlist])
          case _ => None
        }
      }
    }
  }

  def getTagById(id: Int): Option[Tag] = {
    val eventualMaybyTag = tagCollection.find(BSONDocument("id" -> id)).one[Tag]
    Await.result(eventualMaybyTag, Duration(10000, MILLISECONDS))
  }

  def getTagByName(name: String): Option[Tag] = {
    val eventualMaybyTag = tagCollection.find(BSONDocument("name" -> name)).one[Tag]
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

  def getAllResourceIds(): Future[Seq[Int]] = {
    val projection = BSONDocument("id" -> 1)
    resourceCollection.find(BSONDocument.empty, projection).cursor[BSONDocument].collect[List](Integer.MAX_VALUE).map { r =>
      r.map { i =>
        i.getAs[Int]("id")
      }.flatten
    }
  }

  def getFeaturedTags(): Seq[Tag] = {
    val eventualTags = tagCollection.find(BSONDocument("featured" -> 1)).cursor[Tag].toList()
    Await.result(eventualTags, Duration(10000, MILLISECONDS))
  }

  def getAllFeeds(): Seq[FeedImpl] = {
    Await.result(resourceCollection.find(BSONDocument("type" -> "F")).cursor[FeedImpl].toList(), Duration(10000, MILLISECONDS))
  }

  def getAllNewsitems(): Seq[NewsitemImpl] = {
    Await.result(resourceCollection.find(BSONDocument("type" -> "N")).cursor[NewsitemImpl].toList(), Duration(10000, MILLISECONDS))
  }

  def getAllWatchlists(): Seq[WebsiteImpl] = {
    Await.result(resourceCollection.find(BSONDocument("type" -> "L")).cursor[WebsiteImpl].toList(), Duration(10000, MILLISECONDS))
  }

  def getAllWebsites(): Seq[WebsiteImpl] = {
    Await.result(resourceCollection.find(BSONDocument("type" -> "W")).cursor[WebsiteImpl].toList(), Duration(10000, MILLISECONDS))
  }

  def getAllTaggings(): Seq[Tagging] = {
    Await.result(taggingCollection.find(BSONDocument.empty).cursor[Tagging].toList(), Duration(10000, MILLISECONDS))
  }

  def getTaggingsFor(resourceId: Int): Future[Seq[Tagging]] = {
    taggingCollection.find(BSONDocument("resource_id" -> resourceId)).cursor[Tagging].toList()
  }

  case class Tagging(resource_id: Int, tag_id: Int)

}
