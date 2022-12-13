package nz.co.searchwellington.filters

import nz.co.searchwellington.filters.attributesetters._
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.{Scope, ScopedProxyMode}
import org.springframework.stereotype.Component

import javax.servlet.http.HttpServletRequest

@Component("requestFilter")
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
class RequestFilter @Autowired()(val combinerPageAttributeSetter: CombinerPageAttributeSetter, val publisherPageAttributeSetter: PublisherPageAttributeSetter,
                                 val feedAttributeSetter: FeedAttributeSetter, val tagPageAttributeSetter: TagPageAttributeSetter, val filters: Array[RequestAttributeFilter]) {

  private val log = LogFactory.getLog(classOf[RequestFilter])

  private val attributeSetters = Seq(tagPageAttributeSetter, publisherPageAttributeSetter, feedAttributeSetter, combinerPageAttributeSetter)

  private val reservedUrlWords = Set(
    "/about",
    "/api",
    "/autotag",
    "/comment",
    "/feeds",
    "/geotagged",
    "/tags")

  def loadAttributesOntoRequest(request: HttpServletRequest): Unit = {
    for (filter <- filters) {
      filter.filter(request)
    }
    if (isReservedPath(RequestPath.getPathFrom(request))) {
      return
    }

    for (attributeSetter <- attributeSetters) {
      if (attributeSetter.setAttributes(request)) {
        return
      }
    }
    log.debug("Looking for single publisher and tag urls")
  }


  def getFilters: Array[RequestAttributeFilter] = {
    filters
  }


  private def isReservedPath(path: String): Boolean = {
    reservedUrlWords.exists(path.startsWith)
  }

}