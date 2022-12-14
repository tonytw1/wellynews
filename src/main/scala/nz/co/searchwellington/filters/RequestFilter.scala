package nz.co.searchwellington.filters

import nz.co.searchwellington.filters.attributesetters._
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.{Scope, ScopedProxyMode}
import org.springframework.stereotype.Component

import javax.servlet.http.HttpServletRequest
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
    pageParameterFilter,
    locationParameterFilter,
    tagPageAttributeSetter,
    publisherPageAttributeSetter,
    feedAttributeSetter,
    combinerPageAttributeSetter
  )

  def loadAttributesOntoRequest(request: HttpServletRequest)(implicit ec: ExecutionContext): Future[Seq[Boolean]] = {
    Future.sequence(attributeSetters.map { setter =>
      setter.setAttributes(request)
    })
  }

}