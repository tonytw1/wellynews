package nz.co.searchwellington.model

import nz.co.searchwellington.model.geo.Geocode

import java.util.{Date, UUID}
import reactivemongo.api.bson.BSONObjectID

case class Newsitem(override val _id: BSONObjectID = BSONObjectID.generate,
                    override val id: String = UUID.randomUUID().toString,
                    override val `type`: String = "N",
                    override var title: String = "",
                    override val page: String = "",
                    override var http_status: Int = 0,
                    override var date: Option[Date] = None,
                    override var description: Option[String] = None,
                    override var last_scanned: Option[Date] = None,
                    override var last_changed: Option[Date] = None,
                    override var live_time: Option[Date] = None,
                    override var embargoed_until: Option[Date] = None,
                    override var held: Boolean = true,
                    override var url_words: Option[String] = None,
                    override var geocode: Option[Geocode] = None,
                    override var owner: Option[BSONObjectID] = None,
                    override var publisher: Option[BSONObjectID] = None,
                    override val resource_tags: Seq[Tagging] = Seq(),
                    var feed: Option[BSONObjectID] = None,
                    var accepted: Option[Date] = None,
                    acceptedBy: Option[BSONObjectID] = None
) extends PublishedResource {

  override def withTaggings(taggings: Seq[Tagging]): Newsitem = this.copy(resource_tags = taggings)

}