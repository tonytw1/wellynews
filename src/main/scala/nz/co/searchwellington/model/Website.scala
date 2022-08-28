package nz.co.searchwellington.model

import nz.co.searchwellington.model.geo.Geocode

import java.util.{Date, UUID}
import reactivemongo.api.bson.BSONObjectID

case class Website(override val _id: BSONObjectID = BSONObjectID.generate,
                   override val id: String = UUID.randomUUID().toString,
                   override val `type`: String = "W",
                   override var title: String = "",
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
                   override val resource_tags: Seq[Tagging] = Seq()) extends Resource {

  def getTitle: String = title

  override def withTaggings(taggings: Seq[Tagging]): Website = this.copy(resource_tags = taggings)

}
