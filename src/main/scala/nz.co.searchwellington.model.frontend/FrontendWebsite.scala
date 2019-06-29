package nz.co.searchwellington.model.frontend

import java.util.{Date, List}

import nz.co.searchwellington.model.{Geocode, Tag}

@SerialVersionUID(1L)
case class FrontendWebsite(id: String,
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
                           held: Boolean = false) extends FrontendResource
