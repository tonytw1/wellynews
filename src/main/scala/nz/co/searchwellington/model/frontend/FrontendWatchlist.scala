package nz.co.searchwellington.model.frontend

import java.util.Date
import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.model.geo.Geocode

case class FrontendWatchlist(id: String,
                             urlWords: String = null,
                             `type`: String = null,
                             name: String = null,
                             url: String = null,
                             httpStatus: Option[Int] = None,
                             date: Date = null,
                             description: String = null,
                             liveTime: Date = null,
                             tags: Option[Seq[Tag]] = Some(Seq.empty),
                             handTags: Option[Seq[Tag]] = Some(Seq.empty),
                             owner: String = null,
                             geocode: Option[Geocode] = None,
                             held: Boolean = false,
                             lastScanned: Option[Date],
                             lastChanged: Option[Date],
                             publisherName: Option[String] = None,
                             publisherUrlWords: Option[String] = None,
                             actions: Seq[Action] = Seq.empty) extends FrontendResource {

  def getPublisherName: String = publisherName.orNull
  def getPublisherUrlWords: String = publisherUrlWords.orNull

}

