package nz.co.searchwellington.model

import java.util.{Date, UUID}

import reactivemongo.bson.BSONObjectID

case class Feed(override val _id: BSONObjectID = BSONObjectID.generate,
                override val id: String = UUID.randomUUID().toString,
                override val `type`: String = "F",
                override var title: Option[String] = None,
                override var description: Option[String] = None,
                override val page: String = "",
                override var http_status: Int = 0,
                override var date: Option[Date] = None,
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
                var last_read: Option[Date] = None) extends PublishedResource {

  def getId: String = id

  def getAcceptancePolicy: FeedAcceptancePolicy = acceptance

  def setAcceptancePolicy(acceptancePolicy: FeedAcceptancePolicy): Unit = this.acceptance = acceptancePolicy

  def getLatestItemDate: Date = latestItemDate.getOrElse(null)

  def getLastRead: Date = last_read.getOrElse(null)

  def isScreenScraped: Boolean = page.startsWith("http://brownbag.wellington.gen.nz/")

  override def withTags(taggings: Seq[Tagging]): Feed = this.copy(resource_tags = taggings)

}

