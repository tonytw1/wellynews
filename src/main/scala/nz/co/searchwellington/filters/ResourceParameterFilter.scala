package nz.co.searchwellington.filters

import javax.servlet.http.HttpServletRequest

import com.google.common.base.Strings
import nz.co.searchwellington.repositories.HibernateResourceDAO
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("request") class ResourceParameterFilter @Autowired()(var resourceDAO: HibernateResourceDAO) extends RequestAttributeFilter {

  private val log = Logger.getLogger(classOf[ResourceParameterFilter])

  def filter(request: HttpServletRequest) {

    def processResourceId(request: HttpServletRequest, resourceParameter: String) {
      try {
        val resourceId = resourceParameter.toInt
        if (resourceId > 0) {
          resourceDAO.loadResourceById(resourceId).map { resource =>
            log.debug("Found resource: " + resource.title)
            request.setAttribute("resource", resource)
          }
        }
      }
      catch {
        case e: NumberFormatException => {
          log.warn("Invalid resource id given: " + resourceParameter)
        }
      }
    }

    def processResourceUrlWords(request: HttpServletRequest, resourceParameter: String) {
      resourceDAO.loadByUrlWords(resourceParameter).map { resource =>
        log.debug("Found resource by urlWords: " + resource.title)
        request.setAttribute("resource", resource)
      }
    }

    val resourceParameter = request.getParameter("resource")
    if (!Strings.isNullOrEmpty(resourceParameter)) {
      if (resourceParameter.matches("\\d+")) {
        processResourceId(request, resourceParameter)
      } else {
        processResourceUrlWords(request, resourceParameter)
      }
    }
  }

}
