package nz.co.searchwellington.feeds.whakaoko.model

import nz.co.searchwellington.model.{HttpStatus, Tag}
import nz.co.searchwellington.model.frontend.{Action, FrontendResource}
import nz.co.searchwellington.model.geo.Geocode
import org.joda.time.DateTime

import java.util.Date

case class FeedItem(id: String,
                    title: Option[String] = None,
                    body: Option[String] = None,
                    subscriptionId: String,
                    url: String,
                    imageUrl: Option[String] = None,
                    date: Option[Date] = None,
                    place: Option[Place] = None,
                    categories: Option[Seq[Category]] = None,
                    accepted: Option[DateTime] = None
                   ) extends FrontendResource {
  override val `type` = "FI"
  override val urlWords: String = ""
  override val name: String = title.getOrElse(url)
  override val httpStatus: Option[HttpStatus] = None
  override val description: String = body.getOrElse("")
  override val liveTime: Date = null
  override val tags: Option[Seq[Tag]] = None
  override val handTags: Option[Seq[Tag]] = None
  override val owner: String = ""
  override val geocode: Option[Geocode] = None // TODO
  override val held: Boolean = false
  override val lastChanged: Option[Date] = None
  override val lastScanned: Option[Date] = None
  override val actions: Seq[Action] = Seq.empty
}
