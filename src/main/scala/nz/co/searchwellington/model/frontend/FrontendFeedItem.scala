package nz.co.searchwellington.model.frontend

import nz.co.searchwellington.model.{HttpStatus, Tag}
import nz.co.searchwellington.model.geo.Geocode

import java.util.Date

case class FrontendFeedItem( id: String,
                            `type`: String = "FNI",
                             urlWords: String = "",
                             name: String = "",
                             url: String = "",
                             httpStatus: Option[HttpStatus] = None,
                             date: Option[Date] = None,
                             description: String = null,
                             tags: Option[Seq[Tag]] = None,
                             handTags: Option[Seq[Tag]] = None,
                             owner: String = "",
                             geocode: Option[Geocode] = None,
                             held: Boolean = false,
                             lastChanged: Option[Date] = None,
                             lastScanned: Option[Date] = None,
                             publisherName: Option[String] = None,
                             publisherUrlWords: Option[String] = None,
                             actions: Seq[Action] = Seq.empty) extends FrontendResource