package nz.co.searchwellington.model

import java.util.Date

import org.joda.time.DateTime

case class Watchlist(
                      override var id: Int = 0,
                      override var `type`: String = "",
                      override var name: String = "",
                      override var url: String = "",
                      override var httpStatus: Int = 0,
                      override var date: Date = DateTime.now.toDate,
                      override var description: String = "",
                      override var lastScanned: Date = null,
                      override var lastChanged: Date = null,
                      override var liveTime: Date = null,
                      override var embargoedUntil: Date = null,
                      override var held: Boolean = false,
                      override var urlWords: String = null,
                      override var geocode: Geocode = null,
                      override var owner: User = null,
                      override var publisher: Website = null
                    ) extends PublishedResource {

  override def getType = "L"

}
