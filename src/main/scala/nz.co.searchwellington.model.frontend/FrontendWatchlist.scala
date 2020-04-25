package nz.co.searchwellington.model.frontend

import java.util.Date

import nz.co.searchwellington.model.{Geocode, Tag}

case class FrontendWatchlist(id: String,
                             urlWords: String = null,
                             `type`: String = null,
                             name: String = null,
                             url: String = null,
                             httpStatus: Int = 0,
                             date: Date = null,
                             description: String = null,
                             liveTime: Date = null,
                             tags: Seq[Tag] = Seq.empty,
                             handTags: Seq[Tag] = Seq.empty,
                             owner: String = null,
                             place: Option[Geocode] = None,
                             held: Boolean = false,
                             lastScanned: Option[Date],
                             lastChanged: Option[Date]) extends FrontendResource
