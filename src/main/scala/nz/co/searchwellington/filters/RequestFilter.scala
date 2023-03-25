package nz.co.searchwellington.filters

import jakarta.servlet.http.HttpServletRequest
import nz.co.searchwellington.filters.attributesetters._
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.{Scope, ScopedProxyMode}
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component("requestFilter")
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
class RequestFilter @Autowired()(combinerPageAttributeSetter: CombinerPageAttributeSetter,
                                 publisherPageAttributeSetter: PublisherPageAttributeSetter,
                                 feedAttributeSetter: FeedAttributeSetter,
                                 tagPageAttributeSetter: TagPageAttributeSetter,
                                 pageParameterFilter: PageParameterFilter,
                                 locationParameterFilter: LocationParameterFilter) {

  private val attributeSetters = Seq(
    pageParameterFilter,
    locationParameterFilter,
    tagPageAttributeSetter,
    publisherPageAttributeSetter,
    feedAttributeSetter,
    combinerPageAttributeSetter
  )

  def loadAttributesOntoRequest(request: HttpServletRequest)(implicit ec: ExecutionContext): Future[Map[String, Any]] = {
    Future.sequence(attributeSetters.map { setter =>
      setter.setAttributes(request)
    }).map { attributes =>
      // Merge all the contributions from the various setters
      attributes.flatten.toMap
    }
  }

}