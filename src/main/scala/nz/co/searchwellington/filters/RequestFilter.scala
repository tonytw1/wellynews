package nz.co.searchwellington.filters

import nz.co.searchwellington.filters.attributesetters._
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.{Scope, ScopedProxyMode}
import org.springframework.stereotype.Component

import javax.servlet.http.HttpServletRequest

@Component("requestFilter")
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
class RequestFilter @Autowired()(combinerPageAttributeSetter: CombinerPageAttributeSetter, publisherPageAttributeSetter: PublisherPageAttributeSetter,
                                 feedAttributeSetter: FeedAttributeSetter, tagPageAttributeSetter: TagPageAttributeSetter,
                                 pageParameterFilter: PageParameterFilter,
                                 locationParameterFilter: LocationParameterFilter) {

  private val attributeSetters = Seq(
    pageParameterFilter,
    locationParameterFilter,
    pageParameterFilter,
    locationParameterFilter,
    tagPageAttributeSetter,
    publisherPageAttributeSetter, feedAttributeSetter, combinerPageAttributeSetter,

  )

  private val reservedUrlWords = Set(
    "/about",
    "/api",
    "/autotag",
    "/comment",
    "/feeds",
    "/geotagged",
    "/tags")

  def loadAttributesOntoRequest(request: HttpServletRequest): Unit = {
    if (!isReservedPath(RequestPath.getPathFrom(request))) {
      for (attributeSetter <- attributeSetters) {
        if (attributeSetter.setAttributes(request)) {
          return
        }
      }
    }
  }

  private def isReservedPath(path: String): Boolean = {
    reservedUrlWords.exists(path.startsWith)
  }

}