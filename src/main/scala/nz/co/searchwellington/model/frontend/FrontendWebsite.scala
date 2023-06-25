package nz.co.searchwellington.model.frontend

import java.util.Date
import nz.co.searchwellington.model.{HttpStatus, Tag}
import nz.co.searchwellington.model.geo.Geocode

@SerialVersionUID(1L)
case class FrontendWebsite(id: String,
                           urlWords: String = null,
                           `type`: String = null,
                           name: String = null,
                           url: String = null,
                           httpStatus: Option[HttpStatus] = None,
                           date: Option[Date] = null,
                           description: String = null,
                           liveTime: Date = null,
                           tags: Option[Seq[Tag]] = Some(Seq.empty),
                           handTags: Option[Seq[Tag]] = Some(Seq.empty),
                           owner: String = null,
                           geocode: Option[Geocode] = None,
                           held: Boolean = false,
                           lastScanned: Option[Date] = None,
                           lastChanged: Option[Date] = None,
                           actions: Seq[Action] = Seq.empty) extends FrontendResource

