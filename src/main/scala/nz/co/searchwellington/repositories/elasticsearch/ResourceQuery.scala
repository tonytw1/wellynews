package nz.co.searchwellington.repositories.elasticsearch

import nz.co.searchwellington.model.{Tag, Website}
import org.joda.time.Interval
import reactivemongo.bson.BSONObjectID
import uk.co.eelpieconsulting.common.geo.model.LatLong

case class ResourceQuery(`type`: Option[String] = None,
                         tags: Option[Set[Tag]] = None,
                         maxItems: Int = 30,
                         startIndex: Int = 0,
                         publisher: Option[Website] = None,
                         interval: Option[Interval] = None,
                         q: Option[String] = None,
                         owner: Option[BSONObjectID] = None,
                         circle: Option[Circle] = None,
                         geocoded: Option[Boolean] = None
                        )

case class Circle(centre: LatLong, radius: Double)
