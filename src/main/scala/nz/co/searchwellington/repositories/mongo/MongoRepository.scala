package nz.co.searchwellington.repositories.mongo

import java.util.Date

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model._
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Component
import reactivemongo.api.MongoConnection.ParsedURIWithDB
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.bson.{BSONDateTime, BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONHandler, BSONObjectID, BSONReader, BSONString, BSONValue, BSONWriter, Macros}
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.api.{AsyncDriver, Cursor, DB, MongoConnection}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Try

@Component
class MongoRepository @Autowired()(@Value("${mongo.uri}") mongoUri: String) extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[MongoRepository])

  private val AllDocuments: Int = Integer.MAX_VALUE

  def connect()(implicit ec: ExecutionContext): DB = {
    log.info("Connecting to Mongo: " + mongoUri)

    val driver = AsyncDriver()

    val eventualParsedUri: Future[ParsedURIWithDB] = MongoConnection.fromStringWithDB(mongoUri)
    val eventualDatabase = eventualParsedUri.flatMap { parsedUri =>
      val eventualConnection: Future[MongoConnection] = driver.connect(parsedUri)
      eventualConnection.flatMap { connection =>
          connection.database(parsedUri.db)
      }
    }

    Await.result(eventualDatabase, OneMinute)
  }

  private val db = {
    import scala.concurrent.ExecutionContext.Implicits.global
    connect()
  }

  val resourceCollection: BSONCollection = db.collection("resource")
  val suppressionCollection: BSONCollection = db.collection("supression") // TODO spelling
  val tagCollection: BSONCollection = db.collection("tag")
  val userCollection: BSONCollection = db.collection("user")
  val discoveredFeedCollection: BSONCollection = db.collection("discovered_feed")

  {
    import scala.concurrent.ExecutionContext.Implicits.global
    log.info("Got database connection: " + db)

    resourceCollection.create(failsIfExists = false)
    suppressionCollection.create(failsIfExists = false)
    tagCollection.create(failsIfExists = false)
    userCollection.create(failsIfExists = false)
    discoveredFeedCollection.create(failsIfExists = false)

    log.info("Ensuring mongo indexes")
    val resourceByTypeAndUrlWords = Index(Seq("type" -> IndexType.Ascending, "url_words" -> IndexType.Ascending), name = Some("type_with_url_words"), unique = false)
    val resourceByUrl = Index(Seq("page" -> IndexType.Ascending), name = Some("page"), unique = false) // TODO Resources with null urls are the problem here
    val resourceById = Index(Seq("id" -> IndexType.Ascending), name = Some("id"), unique = true)

    val requiredResourceIndexes = Seq(resourceByTypeAndUrlWords, resourceByUrl, resourceById)

    requiredResourceIndexes.foreach { requiredIndex =>
      val result = Await.result(resourceCollection.indexesManager.ensure(requiredIndex), OneMinute)
      log.info("Ensured index result for " + requiredIndex.name + ": " + result)
    }

    val suppressedUrls = Index(Seq("url" -> IndexType.Ascending), name = Some("url"), unique = false)
    val suppressedUrlsResult = Await.result(suppressionCollection.indexesManager.ensure(suppressedUrls), OneMinute)
    log.info("Ensured index result for " + suppressedUrls.name + ": " + suppressedUrlsResult)

    val discoveredFeedsSeen = Index(Seq("seen" -> IndexType.Descending), name = Some("seen"), unique = false)
    val discoveredFeedsSeenResult = Await.result(discoveredFeedCollection.indexesManager.ensure(discoveredFeedsSeen), OneMinute)
    log.info("Ensured index result for " + suppressedUrls.name + ": " + discoveredFeedsSeenResult)
  }

  // TODO This feels wrong; why is the reactive mongo supplied one private?
  implicit object DateHandler extends BSONHandler[Date] {
    override def readTry(bson: BSONValue): Try[Date] = {
      bson match {
        case d: BSONDateTime =>
          scala.util.Success(new DateTime(d.value).toDate)
        case _ => scala.util.Failure(new RuntimeException)
      }
    }

    override def writeTry(d: Date): Try[BSONValue] = {
      scala.util.Success(BSONDateTime(d.getTime))
    }
  }

  implicit object feedAcceptanceReader extends BSONReader[FeedAcceptancePolicy] {
     def readTry(bson: BSONValue): Try[FeedAcceptancePolicy] = bson match {
      case s: BSONString => scala.util.Success(FeedAcceptancePolicy.valueOf(s.value))
      case _ => scala.util.Failure(throw new RuntimeException("Could not map FeedAcceptancePolicy from: " + bson))
    }
  }

  implicit def taggingReader: BSONDocumentReader[Tagging] = Macros.reader[Tagging]

  implicit def osmIdReader: BSONDocumentReader[OsmId] = Macros.reader[OsmId]
  implicit def geocodeReader: BSONDocumentReader[Geocode] = Macros.reader[Geocode]

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

  implicit object feedAcceptanceWriter extends BSONWriter[FeedAcceptancePolicy] {
    override def writeTry(t: FeedAcceptancePolicy): Try[BSONValue] = {
      scala.util.Success(BSONString(t.name()))
    }
  }

  implicit def taggingWriter: BSONDocumentWriter[Tagging] = Macros.writer[Tagging]

  implicit def osmIdWriter: BSONDocumentWriter[OsmId] = Macros.writer[OsmId]
  implicit def geocodeWriter: BSONDocumentWriter[Geocode] = Macros.writer[Geocode]

  implicit def feedWriter: BSONDocumentWriter[Feed] = Macros.writer[Feed]

  implicit def newsitemWriter: BSONDocumentWriter[Newsitem] = Macros.writer[Newsitem]

  implicit def supressionWriter: BSONDocumentWriter[Supression] = Macros.writer[Supression]

  implicit def tagWriter: BSONDocumentWriter[Tag] = Macros.writer[Tag]

  implicit def userWriter: BSONDocumentWriter[User] = Macros.writer[User]

  implicit def watchlistWriter: BSONDocumentWriter[Watchlist] = Macros.writer[Watchlist]

  implicit def websiteWriter: BSONDocumentWriter[Website] = Macros.writer[Website]

  implicit def discoveredFeedWriter: BSONDocumentWriter[DiscoveredFeed] = Macros.writer[DiscoveredFeed]

  def saveResource(resource: Resource)(implicit ec: ExecutionContext): Future[WriteResult] = {
    log.debug("Updating resource: " + resource._id + " / " + resource.last_scanned + " / " + resource.resource_tags)
    val byId = BSONDocument("_id" -> resource._id)
    resource match { // TODO sick of dealing with Scala implicits and just want to write features so this hack
      case n: Newsitem => resourceCollection.update.one(byId, n, upsert = true)
      case w: Website => resourceCollection.update.one(byId, w, upsert = true)
      case f: Feed => resourceCollection.update.one(byId, f, upsert = true)
      case l: Watchlist => resourceCollection.update.one(byId, l, upsert = true)
    }
  }

  def saveSupression(suppression: Supression)(implicit ec: ExecutionContext): Future[WriteResult] = {
    val byId = BSONDocument("_id" -> suppression._id)
    suppressionCollection.update.one(byId, suppression, upsert = true)
  }

  def removeSupressionFor(url: String)(implicit ec: ExecutionContext): Future[WriteResult] = {
    val byUrl = BSONDocument("url" -> url)
    suppressionCollection.delete.one(byUrl)
  }

  def removeResource(resource: Resource)(implicit ec: ExecutionContext): Future[WriteResult] = {
    val byId = BSONDocument("_id" -> resource._id)
    resourceCollection.delete.one(byId)
  }

  def removeUser(user: User)(implicit ec: ExecutionContext): Future[WriteResult] = {
    val byId = BSONDocument("_id" -> user._id)
    userCollection.delete.one(byId)
  }

  def saveUser(user: User)(implicit ec: ExecutionContext): Future[WriteResult] = {
    val byId = BSONDocument("_id" -> user._id)
    userCollection.update.one(byId, user, upsert = true)
  }

  def getSupressionByUrl(url: String)(implicit ec: ExecutionContext): Future[Option[Supression]] = {
    val byUrl = BSONDocument("url" -> url)
    suppressionCollection.find(byUrl, noProjection).one[Supression]
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

    resourceCollection.find(withHeldFilter, noProjection).
      sort(BSONDocument("title" -> 1)).
      cursor[Website]().collect[List](maxDocs = 10, err = Cursor.FailOnError[List[Website]]())
  }

  def getWebsiteByUrlwords(urlWords: String)(implicit ec: ExecutionContext): Future[Option[Website]] = {
    getResourceBy(BSONDocument("type" -> "W", "url_words" -> urlWords)).map(ro => ro.map(r => r.asInstanceOf[Website]))
  }

  def getTagById(id: String)(implicit ec: ExecutionContext): Future[Option[Tag]] = {
    tagCollection.find(BSONDocument("id" -> id), noProjection).one[Tag]
  }

  def getTagByObjectId(objectId: BSONObjectID)(implicit ec: ExecutionContext): Future[Option[Tag]] = {
    tagCollection.find(BSONDocument("_id" -> objectId), noProjection).one[Tag]
  }

  def getTagByUrlWords(urlWords: String)(implicit ec: ExecutionContext): Future[Option[Tag]] = {
    tagCollection.find(BSONDocument("name" -> urlWords), noProjection).one[Tag] // TODO rename field
  }

  def getTagsByParent(parent: BSONObjectID)(implicit ec: ExecutionContext): Future[List[Tag]] = {
    tagCollection.find(BSONDocument("parent" -> parent), noProjection).sort(BSONDocument("display_name" -> 1)).cursor[Tag]().
      collect[List](maxDocs = Integer.MAX_VALUE, err = Cursor.FailOnError[List[Tag]]())
  }

  def saveTag(tag: Tag)(implicit ec: ExecutionContext): Future[WriteResult] = {
    val byId = BSONDocument("_id" -> tag._id)
    tagCollection.update.one(byId, tag, upsert = true)
  }

  def getAllTags()(implicit ec: ExecutionContext): Future[Seq[Tag]] = {
    tagCollection.find(BSONDocument.empty, noProjection).sort(BSONDocument("display_name" -> 1)).cursor[Tag]().
      collect[List](maxDocs = AllDocuments, err = Cursor.FailOnError[List[Tag]]())
  }

  def getAllResourceIds()(implicit ec: ExecutionContext): Future[Seq[BSONObjectID]] = {
    resourceCollection.find(BSONDocument.empty, Some(idOnlyProjection)).cursor[BSONDocument]().
      collect[List](maxDocs = AllDocuments, err = Cursor.FailOnError[List[BSONDocument]]()).map { r =>
      r.flatMap(i => i.getAsOpt[BSONObjectID]("_id"))
    }
  }

  def getNeverScanned(maxItems: Int)(implicit ec: ExecutionContext): Future[List[BSONObjectID]] = {
    val selector = BSONDocument(
      "last_scanned" -> BSONDocument(
        "$exists" -> false
      )
    )

    // TODO time window to prevent race for new items

    resourceCollection.find(selector, Some(idOnlyProjection)).cursor[BSONDocument]().
      collect[List](maxDocs = maxItems, err = Cursor.FailOnError[List[BSONDocument]]()).map { r =>
      r.flatMap(i => i.getAsOpt[BSONObjectID]("_id"))
    }
  }

  def getNotCheckedSince(lastScanned: DateTime, maxItems: Int)(implicit ec: ExecutionContext): Future[Seq[BSONObjectID]] = {
    val selector = BSONDocument(
      "last_scanned" -> BSONDocument(
        "$lt" -> BSONDateTime(lastScanned.getMillis)
      )
    )
    resourceCollection.find(selector, Some(idOnlyProjection)).cursor[BSONDocument]().collect[List](maxDocs = maxItems, err = Cursor.FailOnError[List[BSONDocument]]()).map { r =>
      r.flatMap(i => i.getAsOpt[BSONObjectID]("_id"))
    }

    //return sessionFactory.getCurrentSession.createCriteria(classOf[Resource]).
    // add(Restrictions.gt("liveTime", launchedDate)).
    // add(Restrictions.lt("lastScanned", lastScanned)).
    // addOrder(Order.asc("lastScanned")).setMaxResults(maxItems).list.asInstanceOf[List[Resource]]
  }

  def getAllFeeds()(implicit ec: ExecutionContext): Future[Seq[Feed]] = {
    resourceCollection.find(BSONDocument("type" -> "F"), noProjection).
      cursor[Feed]().
      collect[List](maxDocs = AllDocuments, Cursor.FailOnError[List[Feed]]())
  }

  def getAllNewsitemsForFeed(feed: Feed)(implicit ec: ExecutionContext): Future[Seq[Newsitem]] = {
    val newsitemsFromFeed = BSONDocument("type" -> "N", "feed" -> feed._id.stringify)
    resourceCollection.find(newsitemsFromFeed, noProjection).
      cursor[Newsitem]().
      collect[List](maxDocs = AllDocuments, err = Cursor.FailOnError[List[Newsitem]]())
  }

  def getResourceIdsByTag(tag: Tag)(implicit ec: ExecutionContext): Future[Seq[BSONObjectID]] = {
    val selector = BSONDocument("resource_tags.tag_id" -> tag._id)
    allResourceIdsFor(selector)
  }

  def getResourcesIdsForPublisher(publisher: Website)(implicit ec: ExecutionContext): Future[Seq[BSONObjectID]] = {
    val byPublisher = BSONDocument("publisher" -> publisher._id)
    allResourceIdsFor(byPublisher)
  }

  def getResourceIdsByTaggingUser(user: User)(implicit ec: ExecutionContext): Future[Seq[BSONObjectID]] = {
    val byTaggingUser = BSONDocument("resource_tags.user_id" -> user._id)
    allResourceIdsFor(byTaggingUser)
  }

  private def allResourceIdsFor(selector: BSONDocument)(implicit ec: ExecutionContext): Future[Seq[BSONObjectID]] = {
    resourceCollection.find(selector, noProjection).
      cursor[BSONDocument]().
      collect[List](maxDocs = AllDocuments, err = Cursor.FailOnError[List[BSONDocument]]()).map { d =>
      d.flatMap { i =>
        i.getAsOpt[BSONObjectID]("_id")
      }
    }
  }

  def getAllDiscoveredFeeds()(implicit ec: ExecutionContext): Future[Seq[DiscoveredFeed]] = {
    discoveredFeedCollection.find(BSONDocument.empty, noProjection).
      sort(BSONDocument("seen" -> -1)).
      cursor[DiscoveredFeed]().
      collect[List](maxDocs = AllDocuments, err = Cursor.FailOnError[List[DiscoveredFeed]]())
  }

  def getDiscoveredFeedByUrlAndReference(url: String, referencedFrom: String)(implicit ec: ExecutionContext): Future[Option[DiscoveredFeed]] = {
    val selector = BSONDocument("url" -> url, "referencedFrom" -> referencedFrom)
    discoveredFeedCollection.find(selector, noProjection).one[DiscoveredFeed]
  }

  def saveDiscoveredFeed(discoveredFeed: DiscoveredFeed)(implicit ec: ExecutionContext): Future[WriteResult] = {
    val byId = BSONDocument("_id" -> discoveredFeed._id)
    discoveredFeedCollection.update.one(byId, discoveredFeed, upsert = true)
  }

  def getAllWatchlists()(implicit ec: ExecutionContext): Future[Seq[Watchlist]] = {
    resourceCollection.find(BSONDocument("type" -> "L"), noProjection).cursor[Watchlist]().collect[List](AllDocuments, Cursor.FailOnError[List[Watchlist]]())
  }

  def getAllUsers()(implicit ec: ExecutionContext): Future[Seq[User]] = {
    userCollection.find(BSONDocument.empty, noProjection).cursor[User]().collect[List](AllDocuments, Cursor.FailOnError[List[User]]())
  }

  def getUserByObjectId(objectId: BSONObjectID)(implicit ec: ExecutionContext): Future[Option[User]] = {
    userCollection.find(BSONDocument("_id" -> objectId), noProjection).one[User]
  }

  def getUserByProfilename(profileName: String)(implicit ec: ExecutionContext): Future[Option[User]] = {
    userCollection.find(BSONDocument("profilename" -> profileName), noProjection).one[User]
  }

  def getUserByTwitterId(twitterId: Long)(implicit ec: ExecutionContext): Future[Option[User]] = {
    userCollection.find(BSONDocument("twitterid" -> twitterId), noProjection).one[User]
  }

  private def getResourceBy(selector: BSONDocument)(implicit ec: ExecutionContext): Future[Option[Resource]] = {
    resourceCollection.find(selector, noProjection).one[BSONDocument].map { bo =>
      bo.flatMap { b =>
        val `type` = b.get("type").get
        `type` match {
          case BSONString("N") => b.asOpt[Newsitem]
          case BSONString("W") => b.asOpt[Website]
          case BSONString("F") => b.asOpt[Feed]
          case BSONString("L") => b.asOpt[Watchlist]
          case _ => {
            log.warn("Resource had unexpected type: " + `type`)
            None
          }
        }
      }
    }
  }

  def getResourcesOwnedBy(owner: User): Future[Seq[Resource]] = {
    Future.successful(Seq.empty) // TODO implement
  }

  private val noProjection: Option[BSONDocument] = None
  private val idOnlyProjection = BSONDocument("_id" -> 1)
}
