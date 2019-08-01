package nz.co.searchwellington.repositories.mongo

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model._
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Component
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.{UpdateWriteResult, WriteResult}
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.api.{DB, MongoConnection, MongoDriver}
import reactivemongo.bson.{BSONDateTime, BSONDocument, BSONObjectID, BSONReader, BSONString, BSONValue, BSONWriter, Macros}

import scala.concurrent.{Await, ExecutionContext, Future}

@Component
class MongoRepository @Autowired()(@Value("#{config['mongo.uri']}") mongoUri: String) extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[MongoRepository])
  private val AllDocuments: Int = Integer.MAX_VALUE

  def connect()(implicit ec: ExecutionContext): DB = {
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

    val db: DB = Await.result(eventualDatabase, OneMinute)
    log.info("Got database connection: " + db)
    db
  }

  val db = {
    import scala.concurrent.ExecutionContext.Implicits.global
    connect()
  }

  val resourceCollection: BSONCollection = db.collection("resource")
  val supressionCollection: BSONCollection = db.collection("supression")
  val tagCollection: BSONCollection = db.collection("tag")
  val taggingCollection: BSONCollection = db.collection("resource_tags")
  val userCollection: BSONCollection = db.collection("user")
  val discoveredFeedCollection: BSONCollection = db.collection("discovered_feed")

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
  implicit def supressionReader = Macros.reader[Supression]
  implicit def tagReader = Macros.reader[Tag]
  implicit def userReader = Macros.reader[User]
  implicit def watchlistReader = Macros.reader[Watchlist]
  implicit def websiteReader = Macros.reader[Website]
  implicit def discoveredFeedReader = Macros.reader[DiscoveredFeed]

  def getResourceById(id: String)(implicit ec: ExecutionContext): Future[Option[Resource]] = {
    getResourceBy(BSONDocument("id" -> id))
  }

  def getResourceByObjectId(id: BSONObjectID)(implicit ec: ExecutionContext): Future[Option[Resource]] = {
    getResourceBy(BSONDocument("_id" -> id))
  }

  implicit object feedAcceptanceWriter extends BSONWriter[FeedAcceptancePolicy, BSONValue] {
    override def write(t: FeedAcceptancePolicy): BSONValue = {
      BSONString(t.name())
    }
  }

  implicit def taggingWriter = Macros.writer[Tagging]
  implicit def geocodeWriter = Macros.writer[Geocode]
  implicit def feedWriter = Macros.writer[Feed]
  implicit def newsitemWriter = Macros.writer[Newsitem]
  implicit def supressionWriter = Macros.writer[Supression]
  implicit def tagWriter = Macros.writer[Tag]
  implicit def userWriter = Macros.writer[User]
  implicit def watchlistWriter = Macros.writer[Watchlist]
  implicit def websiteWriter = Macros.writer[Website]
  implicit def discoveredFeedWriter = Macros.writer[DiscoveredFeed]

  def saveResource(resource: Resource)(implicit ec: ExecutionContext): Future[UpdateWriteResult] = {
    val id = BSONDocument("_id" -> resource._id)
    log.debug("Updating resource: " + resource._id + " / " + resource.last_scanned)
    resource match { // TODO sick of dealing with Scala implicits and just want to write features so this hack
      case n: Newsitem => resourceCollection.update(id, n, upsert = true)
      case w: Website => resourceCollection.update(id, w, upsert = true)
      case f: Feed => resourceCollection.update(id, f, upsert = true)
      case l: Watchlist => resourceCollection.update(id, l, upsert = true)
    }
  }

  def saveSupression(suppression: Supression)(implicit ec: ExecutionContext): Future[UpdateWriteResult] = {
    val id = BSONDocument("_id" -> suppression._id)
    supressionCollection.update(id, suppression, upsert = true)
  }

  def removeSupressionFor(url: String)(implicit ec: ExecutionContext): Future[WriteResult] = {
    val byUrl = BSONDocument("url" -> url)
    supressionCollection.remove(byUrl)
  }

  def removeResource(resource: Resource)(implicit ec: ExecutionContext): Future[WriteResult] = {
    val id = BSONDocument("_id" -> resource._id)
    resourceCollection.remove(id)
  }

  def removeUser(user: User)(implicit ec: ExecutionContext): Future[WriteResult] = {
    val id = BSONDocument("_id" -> user._id)
    userCollection.remove(id)
  }

  def saveUser(user: User)(implicit ec: ExecutionContext): Future[UpdateWriteResult] = {
    val id = BSONDocument("_id" -> user._id)
    userCollection.update(id, user, upsert = true)
  }

  def getSupressionByUrl(url: String)(implicit ec: ExecutionContext): Future[Option[Supression]] = {
    val byUrl = BSONDocument("url" -> url)
    supressionCollection.find(byUrl).one[Supression]
  }

  def getResourceByUrl(url: String)(implicit ec: ExecutionContext): Future[Option[Resource]] = {
    getResourceBy(BSONDocument("page" -> url))
  }

  def getFeedByUrl(url: String)(implicit ec: ExecutionContext): Future[Option[Feed]] = {
    getResourceBy(BSONDocument("type" -> "F", "page" -> url)).map(ro => ro.map(r => r.asInstanceOf[Feed]))
  }

  def getFeedByUrlwords(urlWords: String)(implicit ec: ExecutionContext): Future[Option[Feed]] = {
    getResourceBy(BSONDocument("type" -> "F", "url_words" -> urlWords)).map( ro => ro.map(r => r.asInstanceOf[Feed]))
  }

  def getWebsiteByName(name: String)(implicit ec: ExecutionContext): Future[Option[Website]] = {
    getResourceBy(BSONDocument("type" -> "W", "title" -> name)).map(ro => ro.map(r => r.asInstanceOf[Website]))
  }

  def getWebsiteByNamePrefix(q: String)(implicit ec: ExecutionContext): Future[List[Website]] = {
    val prefixRegex = BSONDocument("$regex" -> ("^" + q + ".*")) // TODO How to escape
    resourceCollection.find(BSONDocument("type" -> "W", "title" -> prefixRegex)).
      sort(BSONDocument("title" -> 1)).
      cursor[Website]().collect[List](10)
  }

  def getWebsiteByUrlwords(urlWords: String)(implicit ec: ExecutionContext): Future[Option[Website]] = {
    getResourceBy(BSONDocument("type" -> "W", "url_words" -> urlWords)).map( ro => ro.map(r => r.asInstanceOf[Website]))
  }

  def getTagById(id: String)(implicit ec: ExecutionContext): Future[Option[Tag]] = {
    tagCollection.find(BSONDocument("id" -> id)).one[Tag]
  }

  def getTagByObjectId(objectId: BSONObjectID)(implicit ec: ExecutionContext): Future[Option[Tag]] = {
    tagCollection.find(BSONDocument("_id" -> objectId)).one[Tag]
  }

  def getTagByUrlWords(urlWords: String)(implicit ec: ExecutionContext): Future[Option[Tag]] = {
    tagCollection.find(BSONDocument("name" -> urlWords)).one[Tag] // TODO rename field
  }

  def getTagsByParent(parent: BSONObjectID)(implicit ec: ExecutionContext): Future[List[Tag]] = {
    tagCollection.find(BSONDocument("parent" -> parent)).sort(BSONDocument("display_name" -> 1)).cursor[Tag]().collect[List]()
  }

  def saveTag(tag: Tag)(implicit ec: ExecutionContext): Future[UpdateWriteResult] = {
    val id = BSONDocument("_id" -> tag._id)
    tagCollection.update(id, tag, upsert = true)
  }

  def getAllTags()(implicit ec: ExecutionContext): Future[Seq[Tag]] = {
    tagCollection.find(BSONDocument.empty).sort(BSONDocument("display_name" -> 1)).cursor[Tag]().collect[List](AllDocuments)
  }

  def getAllResourceIds()(implicit ec: ExecutionContext): Future[Seq[BSONObjectID]] = {
    val projection = BSONDocument("_id" -> 1)
    resourceCollection.find(BSONDocument.empty, projection).cursor[BSONDocument]().collect[List](AllDocuments).map { r =>
      r.flatMap(i => i.getAs[BSONObjectID]("_id"))
    }
  }

  def getNotCheckedSince(lastScanned: DateTime, maxItems: Int)(implicit ec: ExecutionContext): Future[Seq[BSONObjectID]] = {
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

  def getAllFeeds()(implicit ec: ExecutionContext): Future[Seq[Feed]] = {
    resourceCollection.find(BSONDocument("type" -> "F")).
      cursor[Feed]().
      collect[List](maxDocs = AllDocuments)
  }

  def getAllNewsitemsForFeed(feed: Feed)(implicit ec: ExecutionContext): Future[Seq[Newsitem]] = {
    val newsitemsFromFeed = BSONDocument("type" -> "N", "feed" -> feed._id.stringify)
    resourceCollection.find(newsitemsFromFeed).
      cursor[Newsitem]().
      collect[List](maxDocs = AllDocuments)
  }

  def getResourceIdsByTag(tag: Tag)(implicit ec: ExecutionContext): Future[Seq[BSONObjectID]] = {
    val selector = BSONDocument(
      "resource_tags.tag_id" -> tag._id
    )
    resourceCollection.find(selector).
      cursor[BSONDocument]().
      collect[List](maxDocs = AllDocuments).map { d =>
      d.flatMap { i =>
        i.getAs[BSONObjectID]("_id")
      }
    }
  }

  def getResourceIdsByTaggingUser(user: User)(implicit ec: ExecutionContext): Future[Seq[BSONObjectID]] = {
    val selector = BSONDocument(
      "resource_tags.user_id" -> user._id
    )
    resourceCollection.find(selector).
      cursor[BSONDocument]().
      collect[List](maxDocs = AllDocuments).map { d =>
      d.flatMap { i =>
        i.getAs[BSONObjectID]("_id")
      }
    }
  }

  def getAllDiscoveredFeeds()(implicit ec: ExecutionContext): Future[Seq[DiscoveredFeed]] = {
    discoveredFeedCollection.find(BSONDocument.empty).
      sort(BSONDocument("seen" -> -1)).
      cursor[DiscoveredFeed]().
      collect[List](maxDocs = AllDocuments)
  }

  def getDiscoveredFeedByUrlAndReference(url: String, referencedFrom: String)(implicit ec: ExecutionContext): Future[Option[DiscoveredFeed]] = {
    val selector = BSONDocument("url" -> url, "referencedFrom" -> referencedFrom)
    discoveredFeedCollection.find(selector).one[DiscoveredFeed]
  }

  def saveDiscoveredFeed(discoveredFeed: DiscoveredFeed)(implicit ec: ExecutionContext): Future[UpdateWriteResult] = {
    val id = BSONDocument("_id" -> discoveredFeed._id)
    discoveredFeedCollection.update(id, discoveredFeed, upsert = true)
  }

  def getAllWatchlists()(implicit ec: ExecutionContext): Future[Seq[Watchlist]] = {
    resourceCollection.find(BSONDocument("type" -> "L")).cursor[Watchlist]().collect[List](AllDocuments)
  }

  def getAllUsers()(implicit ec: ExecutionContext): Future[Seq[User]] = {
    userCollection.find(BSONDocument.empty).cursor[User]().collect[List](AllDocuments)
  }

  def getUserByObjectId(objectId: BSONObjectID)(implicit ec: ExecutionContext): Future[Option[User]] = {
    userCollection.find(BSONDocument("_id" -> objectId)).one[User]
  }

  def getUserByProfilename(profileName: String)(implicit ec: ExecutionContext): Future[Option[User]] = {
    userCollection.find(BSONDocument("profilename" -> profileName)).one[User]
  }

  def getUserByTwitterId(twitterId: Long)(implicit ec: ExecutionContext): Future[Option[User]] = {
    userCollection.find(BSONDocument("twitterid" -> twitterId)).one[User]
  }

  private def getResourceBy(selector: BSONDocument)(implicit ec: ExecutionContext): Future[Option[Resource]] = {
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

  def getResourcesOwnedBy(owner: User): Future[Seq[Resource]] = {
    Future.successful(Seq.empty)  // TODO implement
  }

  case class MongoUser(id: Int, profilename: Option[String], twitterid: Option[Long])

  {
    import scala.concurrent.ExecutionContext.Implicits.global
    resourceCollection.create()
    log.info("Ensuring mongo indexes")
    log.info("resource type/url_words index result: " +
      Await.result(resourceCollection.indexesManager.ensure(
        Index(Seq("type" -> IndexType.Ascending, "url_words" -> IndexType.Ascending), name = Some("type_with_url_words"),
        unique = false)), OneMinute))
  }

}
