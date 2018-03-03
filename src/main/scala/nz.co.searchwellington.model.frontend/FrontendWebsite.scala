package nz.co.searchwellington.model.frontend

import java.util.{Date, List}

import uk.co.eelpieconsulting.common.geo.model.Place

@SerialVersionUID(1L)
case class FrontendWebsite(id: Int = 0,
                           urlWords: String = null,
                           `type`: String = null,
                           name: String = null,
                           url: String = null,
                           httpStatus: Int = 0,
                           date: Date = null,
                           description: String = null,
                           liveTime: Date = null,
                           tags: List[FrontendTag] = null,
                           handTags: List[FrontendTag] = null,
                           owner: String = null,
                           place: Place = null,
                           held: Boolean = false) extends FrontendResource
