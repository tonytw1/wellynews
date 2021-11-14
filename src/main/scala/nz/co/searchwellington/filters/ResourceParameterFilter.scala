package nz.co.searchwellington.filters

import com.google.common.base.Strings
import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Component
@Scope("request") class ResourceParameterFilter @Autowired()(var mongoRepository: MongoRepository) extends RequestAttributeFilter with ReasonableWaits {

  private val log = Logger.getLogger(classOf[ResourceParameterFilter])

  def filter(request: HttpServletRequest) {

    def processResourceId(request: HttpServletRequest, resourceParameter: String) {
      try {
        val resourceId = resourceParameter
        Await.result(mongoRepository.getResourceById(resourceId), TenSeconds).foreach { resource =>
          log.debug("Found resource: " + resource)
          request.setAttribute("resource", resource)
        }
      }
      catch {
        case e: NumberFormatException =>
          log.warn("Invalid resource id given: " + resourceParameter)
      }
    }

    /*
    def processResourceUrlWords(request: HttpServletRequest, resourceParameter: String) {
      mongoRepository.loadByUrlWords(resourceParameter).map { resource =>
        log.debug("Found resource by urlWords: " + resource.title)
        request.setAttribute("resource", resource)
      }
    }
    */

    val resourceParameter = request.getParameter("resource")
    if (!Strings.isNullOrEmpty(resourceParameter)) {
      //if (resourceParameter.matches("\\d+")) {
        processResourceId(request, resourceParameter)
      //} else {
      //  processResourceUrlWords(request, resourceParameter)
      //}
    }
  }

}
