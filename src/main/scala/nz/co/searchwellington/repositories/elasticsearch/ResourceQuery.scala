package nz.co.searchwellington.repositories.elasticsearch

import nz.co.searchwellington.model.{Tag, Website}

case class ResourceQuery(`type`: Option[String] = None, tags: Option[Set[Tag]] = None, maxItems: Int = 30, startIndex: Int = 0,
                         publisher: Option[Website] = None)
