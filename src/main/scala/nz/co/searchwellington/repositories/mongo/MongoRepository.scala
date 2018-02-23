package nz.co.searchwellington.repositories.mongo

import nz.co.searchwellington.model._
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Component
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}
import reactivemongo.bson.{BSONDocument, BSONString, Macros}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

@Component
class MongoRepository @Autowired()(@Value("#{config['mongo.uri']}") mongoUri: String) {

  private val log = Logger.getLogger(classOf[MongoRepository])

  def connect(): DefaultDB = {
    log.info("Connecting to Mongo: " + mongoUri)

    val driver = MongoDriver()

    val parsedUri = MongoConnection.parseURI(mongoUri)
    val connection = parsedUri.map(driver.connection(_))
    val futureConnection: Future[MongoConnection] = Future.fromTry(connection)

    def database: Future[DefaultDB] = futureConnection.flatMap(_.database("wellynews"))

    Await.result(database, Duration(1, MINUTES))
  }

  val db: DefaultDB = connect()

  def resourceCollection: BSONCollection = db.collection("resource")
  def tagCollection: BSONCollection = db.collection("tag")
  def taggingCollection: BSONCollection = db.collection("resource_tags")
  def userCollection: BSONCollection = db.collection("user")

  implicit def feedReader = Macros.reader[Feed]
  implicit def newsitemReader = Macros.reader[Newsitem]
  implicit def websiteReader = Macros.reader[Website]
  implicit def watchlistReader = Macros.reader[Watchlist]
  implicit def tagReader = Macros.reader[Tag]
  implicit def taggingReader = Macros.reader[Tagging]
  implicit def userReader = Macros.reader[User]

  def getResourceById(id: Int): Future[Option[Resource]] = {
    getResourceBy(BSONDocument("id" -> id))
  }

  def getResourceByUrl(url: String): Future[Option[Resource]] = {
    getResourceBy(BSONDocument("url" -> url))
  }

  def getFeedByUrlwords(urlWords: String): Future[Option[Feed]] = {
    getResourceBy(BSONDocument("type" -> "F", "url_words" -> urlWords)).map( ro => ro.map(r => r.asInstanceOf[Feed]))
  }

  def getWebsiteByUrlwords(urlWords: String): Future[Option[Website]] = {
    getResourceBy(BSONDocument("type" -> "W", "url_words" -> urlWords)).map( ro => ro.map(r => r.asInstanceOf[Website]))
  }

  def getTagById(id: Int): Future[Option[Tag]] = {
    tagCollection.find(BSONDocument("id" -> id)).one[Tag]
  }

  def getTagByName(name: String): Future[Option[Tag]] = {
    tagCollection.find(BSONDocument("name" -> name)).one[Tag]
  }

  def getTagsByParent(parent: Int): Future[List[Tag]] = {
    tagCollection.find(BSONDocument("parent" -> parent)).cursor[Tag]().collect[List]()
  }

  def getAllTags(): Future[Seq[Tag]] = {
    tagCollection.find(BSONDocument.empty).sort(BSONDocument("display_name" -> 1)).cursor[Tag]().collect[List]()
  }

  def getAllResourceIds(): Future[Seq[Int]] = {
    val projection = BSONDocument("id" -> 1)
    resourceCollection.find(BSONDocument.empty, projection).cursor[BSONDocument]().collect[List](Integer.MAX_VALUE).map { r =>
      r.flatMap { i =>
        i.getAs[Int]("id")
      }
    }
  }

  def getAllFeeds(): Future[Seq[Feed]] = {
    resourceCollection.find(BSONDocument("type" -> "F")).cursor[Feed]().collect[List]()
  }

  def getAllWatchlists(): Future[Seq[Website]] = {
    resourceCollection.find(BSONDocument("type" -> "L")).cursor[Website]().collect[List]()
  }

  def getAllWebsites(): Future[Seq[Website]] = {
    resourceCollection.find(BSONDocument("type" -> "W")).cursor[Website]().collect[List]()
  }

  def getAllTaggings(): Future[Seq[Tagging]] = {
    taggingCollection.find(BSONDocument.empty).cursor[Tagging]().toList()
  }

  def getTaggingsFor(resourceId: Int): Future[Seq[Tagging]] = {
    taggingCollection.find(BSONDocument("resource_id" -> resourceId)).cursor[Tagging]().collect[List]()
  }

  def getAllUsers(): Future[Seq[User]] = {
    userCollection.find(BSONDocument.empty).cursor[User]().toList()
  }

  def getUserByProfilename(profileName: String): Future[Option[User]] = {
    userCollection.find(BSONDocument("profilename" -> profileName)).one[User]
  }

  def getUserByTwitterId(twitterId: Long): Future[Option[User]] = {
    userCollection.find(BSONDocument("twitterid" -> twitterId)).one[User]
  }

  private def getResourceBy(selector: BSONDocument) = {
    resourceCollection.find(selector).one[BSONDocument].map { bo =>
      bo.flatMap { b =>
        b.get("type").get match {
          case BSONString("N") => Some(b.as[Newsitem])
          case BSONString("W") => Some(b.as[Website])
          case BSONString("F") => Some(b.as[Feed])
          case BSONString("L") => Some(b.as[Watchlist])
          case _ => None
        }
      }
    }
  }

  case class Tagging(resource_id: Int, tag_id: Int)

  case class MongoUser(id: Int, profilename: Option[String], twitterid: Option[Long])

  {
    log.info("Ensuring indexes")
    log.info("taggings.resource_id index result: " + Await.result(taggingCollection.indexesManager.ensure(Index(Seq("resource_id" -> IndexType.Ascending), name = Some("resource_id"), unique = false)), Duration(10000, MILLISECONDS)))
    log.info("resources.id index result: " + Await.result(resourceCollection.indexesManager.ensure(Index(Seq("id" -> IndexType.Ascending), name = Some("id"), unique = true)), Duration(10000, MILLISECONDS)))
    log.info("tag.id index result: " + Await.result(tagCollection.indexesManager.ensure(Index(Seq("id" -> IndexType.Ascending), name = Some("id"), unique = true)), Duration(10000, MILLISECONDS)))
  }

}
