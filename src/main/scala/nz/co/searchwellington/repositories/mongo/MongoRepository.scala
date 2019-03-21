package nz.co.searchwellington.repositories.mongo

import nz.co.searchwellington.model._
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Component
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}
import reactivemongo.bson.{BSONDateTime, BSONDocument, BSONObjectID, BSONReader, BSONString, BSONValue, BSONWriter, Macros}

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

    val eventualDatabase = Future.fromTry {
      parsedUri.map { uri =>
        uri.db.map { db =>
          driver.connection(uri).database(db)
        }.getOrElse {
          Future.failed(new RuntimeException("No database given in Mongo URI"))
        }
      }
    }.flatten

    Await.result(eventualDatabase, Duration(1, MINUTES))
  }

  val db: DefaultDB = connect()

  def resourceCollection: BSONCollection = db.collection("resource")
  def tagCollection: BSONCollection = db.collection("tag")
  def taggingCollection: BSONCollection = db.collection("resource_tags")
  def userCollection: BSONCollection = db.collection("user")

  implicit object feedAcceptanceReader extends BSONReader[BSONValue, FeedAcceptancePolicy] {
    override def read(bson: BSONValue): FeedAcceptancePolicy = bson match {
      case s: BSONString => FeedAcceptancePolicy.valueOf(s.value)
      case _ => throw new RuntimeException("Could not map FeedAcceptancePolicy from: " + bson)
    }
  }

  implicit def taggingReader = Macros.reader[Tagging]
  implicit def geocodeReader = Macros.reader[Geocode]
  implicit def feedReader = Macros.reader[Feed]
  implicit def newsitemReader = Macros.reader[Newsitem]
  implicit def websiteReader = Macros.reader[Website]
  implicit def watchlistReader = Macros.reader[Watchlist]
  implicit def tagReader = Macros.reader[Tag]
  implicit def userReader = Macros.reader[User]

  def getResourceById(id: String): Future[Option[Resource]] = {
    getResourceBy(BSONDocument("id" -> id))
  }

  def getResourceByObjectId(id: BSONObjectID): Future[Option[Resource]] = {
    getResourceBy(BSONDocument("_id" -> id))
  }

  implicit object feedAcceptanceWriter extends BSONWriter[FeedAcceptancePolicy, BSONValue] {
    override def write(t: FeedAcceptancePolicy): BSONValue = {
      BSONString(t.getLabel)
    }
  }

  implicit def taggingWriter = Macros.writer[Tagging]
  implicit def geocodeWriter = Macros.writer[Geocode]
  implicit def feedWriter = Macros.writer[Feed]
  implicit def newsitemWriter = Macros.writer[Newsitem]
  implicit def websiteWriter = Macros.writer[Website]
  implicit def watchlistWriter = Macros.writer[Watchlist]
  implicit def tagWriter = Macros.writer[Tag]
  implicit def userWriter = Macros.writer[User]

  def saveResource(resource: Resource): Unit = {
    val id = BSONDocument("_id" -> resource._id.get)
    log.info("Updating resource: " + resource._id + " / " + resource.last_scanned)
    resource match {  // TODO sick of dealing with Scala implicits and just want to write features so this hack
      case n: Newsitem => resourceCollection.update(id, n)
      case w: Website => resourceCollection.update(id, w)
      case f: Feed => resourceCollection.update(id, f)
      case l: Watchlist => resourceCollection.update(id, l)
    }
  }

  def getResourceByUrl(url: String): Future[Option[Resource]] = {
    getResourceBy(BSONDocument("page" -> url))
  }

  def getFeedByWhakaokoSubscription(subscription: String): Future[Option[Feed]] = {
    getResourceBy(BSONDocument("type" -> "F", "whakaoko_id" -> subscription)).map( ro => ro.map(r => r.asInstanceOf[Feed]))
  }

  def getFeedByUrlwords(urlWords: String): Future[Option[Feed]] = {
    getResourceBy(BSONDocument("type" -> "F", "url_words" -> urlWords)).map( ro => ro.map(r => r.asInstanceOf[Feed]))
  }

  def getWebsiteByName(name: String): Future[Option[Website]] = {
    getResourceBy(BSONDocument("type" -> "W", "title" -> name)).map(ro => ro.map(r => r.asInstanceOf[Website]))
  }

  def getWebsiteByNamePrefix(q: String): Future[List[Website]] = {
    val prefixRegex = BSONDocument("$regex" -> ("/^" + q + "/i")) // TODO How to escape
    resourceCollection.find(BSONDocument("type" -> "W", "name" -> prefixRegex)).cursor[Website]().collect[List]()
  }

  def getWebsiteByUrlwords(urlWords: String): Future[Option[Website]] = {
    getResourceBy(BSONDocument("type" -> "W", "url_words" -> urlWords)).map( ro => ro.map(r => r.asInstanceOf[Website]))
  }

  def getTagById(id: String): Future[Option[Tag]] = {
    tagCollection.find(BSONDocument("id" -> id)).one[Tag]
  }

  def getTagByObjectId(objectId: BSONObjectID): Future[Option[Tag]] = {
    tagCollection.find(BSONDocument("_id" -> objectId)).one[Tag]
  }

  def getTagByName(name: String): Future[Option[Tag]] = {
    tagCollection.find(BSONDocument("name" -> name)).one[Tag]
  }

  def getTagsByParent(parent: BSONObjectID): Future[List[Tag]] = {
    tagCollection.find(BSONDocument("parent" -> parent)).cursor[Tag]().collect[List]()
  }

  def getAllTags(): Future[Seq[Tag]] = {
    tagCollection.find(BSONDocument.empty).sort(BSONDocument("display_name" -> 1)).cursor[Tag]().collect[List]()
  }

  def getAllResourceIds(): Future[Seq[BSONObjectID]] = {
    val projection = BSONDocument("_id" -> 1)
    resourceCollection.find(BSONDocument.empty, projection).cursor[BSONDocument]().collect[List](Integer.MAX_VALUE).map { r =>
      r.flatMap(i => i.getAs[BSONObjectID]("_id"))
    }
  }

  def getNotCheckedSince(lastScanned: DateTime, maxItems: Int): Future[Seq[BSONObjectID]] = {
    val projection = BSONDocument("_id" -> 1)

    val selector = BSONDocument(
      "last_scanned" -> BSONDocument(
        "$lt" -> BSONDateTime(lastScanned.getMillis)
      )
    )

    resourceCollection.find(selector, projection).cursor[BSONDocument]().collect[List](maxItems).map { r =>
      r.flatMap(i => i.getAs[BSONObjectID]("_id"))
    }

    //return sessionFactory.getCurrentSession.createCriteria(classOf[Resource]).
    // add(Restrictions.gt("liveTime", launchedDate)).
    // add(Restrictions.lt("lastScanned", lastScanned)).
    // addOrder(Order.asc("lastScanned")).setMaxResults(maxItems).list.asInstanceOf[List[Resource]]
  }

  def getAllFeeds(): Future[Seq[Feed]] = {
    resourceCollection.find(BSONDocument("type" -> "F")).cursor[Feed]().collect[List]()
  }

  def getAllNewsitems(): Future[Seq[Newsitem]] = {
    resourceCollection.find(BSONDocument("type" -> "N")).cursor[Newsitem]().collect[List]()
  }

  def getAllWatchlists(): Future[Seq[Watchlist]] = {
    resourceCollection.find(BSONDocument("type" -> "L")).cursor[Watchlist]().collect[List]()
  }

  def getAllWebsites(): Future[Seq[Website]] = {
    resourceCollection.find(BSONDocument("type" -> "W")).cursor[Website]().collect[List]()
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

  case class MongoUser(id: Int, profilename: Option[String], twitterid: Option[Long])

  {
    log.info("Ensuring indexes")
    //log.info("taggings.resource_id index result: " + Await.result(taggingCollection.indexesManager.ensure(Index(Seq("resource_id" -> IndexType.Ascending), name = Some("resource_id"), unique = false)), Duration(10000, MILLISECONDS)))
    //log.info("resources.id index result: " + Await.result(resourceCollection.indexesManager.ensure(Index(Seq("id" -> IndexType.Ascending), name = Some("id"), unique = true)), Duration(10000, MILLISECONDS)))
    //log.info("tag.id index result: " + Await.result(tagCollection.indexesManager.ensure(Index(Seq("id" -> IndexType.Ascending), name = Some("id"), unique = true)), Duration(10000, MILLISECONDS)))
  }

}
