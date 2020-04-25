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
import reactivemongo.api.{Cursor, DB, MongoConnection, MongoDriver}
import reactivemongo.bson.{BSONDateTime, BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID, BSONReader, BSONString, BSONValue, BSONWriter, Macros}

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

  private val db = {
    import scala.concurrent.ExecutionContext.Implicits.global
    connect()
  }

  val resourceCollection: BSONCollection = db.collection("resource")
  val supressionCollection: BSONCollection = db.collection("supression")
  val tagCollection: BSONCollection = db.collection("tag")
  val userCollection: BSONCollection = db.collection("user")
  val discoveredFeedCollection: BSONCollection = db.collection("discovered_feed")

  implicit object feedAcceptanceReader extends BSONReader[BSONValue, FeedAcceptancePolicy] {
    override def read(bson: BSONValue): FeedAcceptancePolicy = bson match {
      case s: BSONString => FeedAcceptancePolicy.valueOf(s.value)
      case _ => throw new RuntimeException("Could not map FeedAcceptancePolicy from: " + bson)
    }
  }

  implicit def taggingReader: BSONDocumentReader[Tagging] = Macros.reader[Tagging]

  // TODO This can be removed if we migrate the osm fields to a nested object in Mongo
  implicit object GeocodeReader extends BSONDocumentReader[Geocode] {
    override def read(bson: BSONDocument): Geocode = {
      val osmId = for {
        i <- bson.getAs[Long]("osm_id")
        t <- bson.getAs[String]("osm_type")
      } yield {
        OsmId(id = i, `type` = t)
      }
      Geocode(
        address = bson.getAs[String]("address"),
        latitude = bson.getAs[Double]("latitude"),
        longitude = bson.getAs[Double]("longitude"),
        osmId = osmId
      )
    }
  }

  implicit def feedReader: BSONDocumentReader[Feed] = Macros.reader[Feed]

  implicit def newsitemReader: BSONDocumentReader[Newsitem] = Macros.reader[Newsitem]

  implicit def supressionReader: BSONDocumentReader[Supression] = Macros.reader[Supression]

  implicit def tagReader: BSONDocumentReader[Tag] = Macros.reader[Tag]

  implicit def userReader: BSONDocumentReader[User] = Macros.reader[User]

  implicit def watchlistReader: BSONDocumentReader[Watchlist] = Macros.reader[Watchlist]

  implicit def websiteReader: BSONDocumentReader[Website] = Macros.reader[Website]

  implicit def discoveredFeedReader: BSONDocumentReader[DiscoveredFeed] = Macros.reader[DiscoveredFeed]

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

  implicit def taggingWriter: BSONDocumentWriter[Tagging] = Macros.writer[Tagging]

  implicit object FormResponseWriter extends BSONDocumentWriter[Geocode] {
    override def write(t: Geocode): BSONDocument = {
      BSONDocument(
        "address" -> t.address,
        "latitude" -> t.latitude,
        "longitude" -> t.longitude,
        "osm_id" -> t.osmId.map(_.id),
        "osm_type" -> t.osmId.map(_.`type`)
      )
    }
  }

  implicit def feedWriter: BSONDocumentWriter[Feed] = Macros.writer[Feed]

  implicit def newsitemWriter: BSONDocumentWriter[Newsitem] = Macros.writer[Newsitem]

  implicit def supressionWriter: BSONDocumentWriter[Supression] = Macros.writer[Supression]

  implicit def tagWriter: BSONDocumentWriter[Tag] = Macros.writer[Tag]

  implicit def userWriter: BSONDocumentWriter[User] = Macros.writer[User]

  implicit def watchlistWriter: BSONDocumentWriter[Watchlist] = Macros.writer[Watchlist]

  implicit def websiteWriter: BSONDocumentWriter[Website] = Macros.writer[Website]

  implicit def discoveredFeedWriter: BSONDocumentWriter[DiscoveredFeed] = Macros.writer[DiscoveredFeed]

  def saveResource(resource: Resource)(implicit ec: ExecutionContext): Future[UpdateWriteResult] = {
    val id = BSONDocument("_id" -> resource._id)
    log.info("Updating resource: " + resource._id + " / " + resource.last_scanned + " / " + resource.resource_tags)
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
    getResourceBy(BSONDocument("type" -> "F", "url_words" -> urlWords)).map(ro => ro.map(r => r.asInstanceOf[Feed]))
  }

  def getWebsiteByName(name: String)(implicit ec: ExecutionContext): Future[Option[Website]] = {
    getResourceBy(BSONDocument("type" -> "W", "title" -> name)).map(ro => ro.map(r => r.asInstanceOf[Website]))
  }

  def getWebsitesByNamePrefix(q: String, showHeld: Boolean)(implicit ec: ExecutionContext): Future[Seq[Website]] = {
    val prefixRegex = BSONDocument("$regex" -> ("^" + q + ".*")) // TODO How to escape

    val selector = BSONDocument(
      "type" -> "W",
      "title" -> prefixRegex
    )
    val withHeldFilter = if (!showHeld) {
      selector ++ BSONDocument("held" -> false)
    } else {
      selector
    }

    resourceCollection.find(withHeldFilter).
      sort(BSONDocument("title" -> 1)).
      cursor[Website]().collect[List](maxDocs = 10, err = Cursor.FailOnError[List[Website]]())
  }

  def getWebsiteByUrlwords(urlWords: String)(implicit ec: ExecutionContext): Future[Option[Website]] = {
    getResourceBy(BSONDocument("type" -> "W", "url_words" -> urlWords)).map(ro => ro.map(r => r.asInstanceOf[Website]))
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
    tagCollection.find(BSONDocument("parent" -> parent)).sort(BSONDocument("display_name" -> 1)).cursor[Tag]().
      collect[List](maxDocs = Integer.MAX_VALUE, err = Cursor.FailOnError[List[Tag]]())
  }

  def saveTag(tag: Tag)(implicit ec: ExecutionContext): Future[UpdateWriteResult] = {
    val id = BSONDocument("_id" -> tag._id)
    tagCollection.update(id, tag, upsert = true)
  }

  def getAllTags()(implicit ec: ExecutionContext): Future[Seq[Tag]] = {
    tagCollection.find(BSONDocument.empty).sort(BSONDocument("display_name" -> 1)).cursor[Tag]().
      collect[List](maxDocs = AllDocuments, err = Cursor.FailOnError[List[Tag]]())
  }

  def getAllResourceIds()(implicit ec: ExecutionContext): Future[Seq[BSONObjectID]] = {
    val projection = BSONDocument("_id" -> 1)
    resourceCollection.find(BSONDocument.empty, projection).cursor[BSONDocument]().
      collect[List](maxDocs = AllDocuments, err = Cursor.FailOnError[List[BSONDocument]]()).map { r =>
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
    resourceCollection.find(selector, projection).cursor[BSONDocument]().
      collect[List](maxDocs = maxItems, err = Cursor.FailOnError[List[BSONDocument]]()).map { r =>
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
      collect[List](maxDocs = AllDocuments, Cursor.FailOnError[List[Feed]]())
  }

  def getAllNewsitemsForFeed(feed: Feed)(implicit ec: ExecutionContext): Future[Seq[Newsitem]] = {
    val newsitemsFromFeed = BSONDocument("type" -> "N", "feed" -> feed._id.stringify)
    resourceCollection.find(newsitemsFromFeed).
      cursor[Newsitem]().
      collect[List](maxDocs = AllDocuments, err = Cursor.FailOnError[List[Newsitem]]())
  }

  def getResourceIdsByTag(tag: Tag)(implicit ec: ExecutionContext): Future[Seq[BSONObjectID]] = {
    val selector = BSONDocument(
      "resource_tags.tag_id" -> tag._id
    )

    resourceCollection.find(selector).
      cursor[BSONDocument]().
      collect[List](maxDocs = AllDocuments, err = Cursor.FailOnError[List[BSONDocument]]()).map { d =>
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
      collect[List](maxDocs = AllDocuments, err = Cursor.FailOnError[List[BSONDocument]]()).map { d =>
      d.flatMap { i =>
        i.getAs[BSONObjectID]("_id")
      }
    }
  }

  def getAllDiscoveredFeeds()(implicit ec: ExecutionContext): Future[Seq[DiscoveredFeed]] = {
    discoveredFeedCollection.find(BSONDocument.empty).
      sort(BSONDocument("seen" -> -1)).
      cursor[DiscoveredFeed]().
      collect[List](maxDocs = AllDocuments, err = Cursor.FailOnError[List[DiscoveredFeed]]())
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
    resourceCollection.find(BSONDocument("type" -> "L")).cursor[Watchlist]().collect[List](AllDocuments, Cursor.FailOnError[List[Watchlist]]())
  }

  def getAllUsers()(implicit ec: ExecutionContext): Future[Seq[User]] = {
    userCollection.find(BSONDocument.empty).cursor[User]().collect[List](AllDocuments, Cursor.FailOnError[List[User]]())
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
    Future.successful(Seq.empty) // TODO implement
  }

  case class MongoUser(id: Int, profilename: Option[String], twitterid: Option[Long]) {

    import scala.concurrent.ExecutionContext.Implicits.global

    resourceCollection.create()
    log.info("Ensuring mongo indexes")
    log.info("resource type/url_words index result: " +
      Await.result(resourceCollection.indexesManager.ensure(
        Index(Seq("type" -> IndexType.Ascending, "url_words" -> IndexType.Ascending), name = Some("type_with_url_words"),
          unique = false)), OneMinute))
  }

}
