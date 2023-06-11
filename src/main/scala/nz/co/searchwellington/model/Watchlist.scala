package nz.co.searchwellington.model

import nz.co.searchwellington.model.geo.Geocode

import java.util.{Date, UUID}
import reactivemongo.api.bson.BSONObjectID

case class Watchlist(override val _id: BSONObjectID = BSONObjectID.generate,
                     override val id: String = UUID.randomUUID().toString,
                     override val `type`: String = "L",
                     override var title: String = "",
                     override var description: Option[String] = None,
                     override val page: String = "",
                     override var httpStatus: Option[HttpStatus] = None,
                     override var date: Option[Date] = None,
                     override var last_scanned: Option[Date] = None,
                     override var last_changed: Option[Date] = None,
                     override var live_time: Option[Date] = None,
                     override var embargoed_until: Option[Date] = None,
                     override var held: Boolean = true,
                     override var url_words: Option[String] = None,
                     override var geocode: Option[Geocode] = None,
                     override var owner: Option[BSONObjectID] = None,
                     override var publisher: Option[BSONObjectID] = None,
                     override val resource_tags: Seq[Tagging] = Seq()
                    ) extends PublishedResource {

  override def withTaggings(taggings: Seq[Tagging]): Watchlist = this.copy(resource_tags = taggings)

}
