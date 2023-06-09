package nz.co.searchwellington.model

import nz.co.searchwellington.model.geo.Geocode

import java.util.Date
import reactivemongo.api.bson.BSONObjectID

trait Resource extends Tagged {

  val _id: BSONObjectID
  val id: String
  val `type`: String
  var title: String
  var description: Option[String]
  val page: String
  var httpStatus: Option[HttpStatus]
  var date: Option[Date]  // TODO backfill ~ 100 records with no date and make mandatory
  var last_scanned: Option[Date]
  var last_changed: Option[Date]
  var live_time: Option[Date]
  var embargoed_until: Option[Date]
  var held: Boolean
  var url_words: Option[String]
  var geocode: Option[Geocode]
  var owner: Option[BSONObjectID]
  val resource_tags: Seq[Tagging]

  def setHttpStatus(status: Int, redirecting: Boolean): Unit = {
    if (httpStatus.nonEmpty) {
      httpStatus = httpStatus.map(_.copy(status = status, redirecting = redirecting))
    } else {
      httpStatus = Some(HttpStatus(status, redirecting = redirecting))
    }
  }

  def setLastScanned(lastScanned: Date): Unit = this.last_scanned = Some(lastScanned)
  def setLastChanged(lastChanged: Date): Unit = this.last_changed = Some(lastChanged)

}
