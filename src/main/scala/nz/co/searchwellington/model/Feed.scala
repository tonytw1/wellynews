package nz.co.searchwellington.model

import nz.co.searchwellington.model.geo.Geocode
import org.joda.time.DateTime

import java.util.{Date, UUID}
import reactivemongo.api.bson.BSONObjectID

case class Feed(override val _id: BSONObjectID = BSONObjectID.generate,
                override val id: String = UUID.randomUUID().toString,
                override val `type`: String = "F",
                override var title: String = "",
                override var description: Option[String] = None,
                override val page: String = "",
                override var httpStatus: Option[HttpStatus] = None,
                override var date: Date = DateTime.now.toDate,
                override var last_scanned: Option[Date] = None,
                override var last_changed: Option[Date] = None,
                override var live_time: Option[Date] = None,
                override var embargoed_until: Option[Date] = None,
                override var held: Boolean = true,
                override var url_words: Option[String] = None,
                override var geocode: Option[Geocode] = None,
                override var owner: Option[BSONObjectID] = None,
                override val resource_tags: Seq[Tagging] = Seq(),
                override var publisher: Option[BSONObjectID] = None,
                var acceptance: FeedAcceptancePolicy = FeedAcceptancePolicy.IGNORE,
                var latestItemDate: Option[Date] = None,
                var last_read: Option[Date] = None,
                var whakaokoSubscription: Option[String] = None,
               ) extends PublishedResource {

  def getId: String = id

  def getAcceptancePolicy: FeedAcceptancePolicy = acceptance

  def setAcceptancePolicy(acceptancePolicy: FeedAcceptancePolicy): Unit = this.acceptance = acceptancePolicy

  def getLatestItemDate: Date = latestItemDate.orNull

  def getLastRead: Date = last_read.orNull

  def isScreenScraped: Boolean = page.startsWith("http://brownbag.wellington.gen.nz/")

  override def withTaggings(taggings: Seq[Tagging]): Feed = this.copy(resource_tags = taggings)

}

