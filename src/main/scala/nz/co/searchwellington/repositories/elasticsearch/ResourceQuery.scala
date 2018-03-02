package nz.co.searchwellington.repositories.elasticsearch

import nz.co.searchwellington.model.{Tag, Website}
import org.joda.time.Interval

case class ResourceQuery(`type`: Option[String] = None, tags: Option[Set[Tag]] = None, maxItems: Int = 30, startIndex: Int = 0,
                         publisher: Option[Website] = None, interval: Option[Interval] = None, q: Option[String] = None)
