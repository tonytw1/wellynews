package nz.co.searchwellington.repositories.elasticsearch

import nz.co.searchwellington.model.{FeedAcceptancePolicy, Tag, Website}
import org.joda.time.Interval
import reactivemongo.api.bson.BSONObjectID
import uk.co.eelpieconsulting.common.geo.model.LatLong

case class ResourceQuery(`type`: Option[Set[String]] = None,
                         tags: Option[Set[Tag]] = None,
                         maxItems: Int = 30,
                         startIndex: Int = 0,
                         publisher: Option[Website] = None,
                         interval: Option[Interval] = None,
                         q: Option[String] = None,
                         owner: Option[BSONObjectID] = None,
                         circle: Option[Circle] = None,
                         geocoded: Option[Boolean] = None,
                         taggingUser: Option[BSONObjectID] = None,
                         feedAcceptancePolicy: Option[FeedAcceptancePolicy] = None,
                         held: Option[Boolean] = None,
                         hostname: Option[String] = None
                        )

case class Circle(centre: LatLong, radius: Double)
