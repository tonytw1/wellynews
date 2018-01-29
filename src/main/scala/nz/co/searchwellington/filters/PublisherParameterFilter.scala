package nz.co.searchwellington.filters

import javax.servlet.http.HttpServletRequest

import com.google.common.base.Strings
import nz.co.searchwellington.repositories.HibernateResourceDAO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@deprecated
@Component
@Scope("request") class PublisherParameterFilter @Autowired()(var resourceDAO: HibernateResourceDAO) extends RequestAttributeFilter {

  override def filter(request: HttpServletRequest): Unit = {
    if (!(Strings.isNullOrEmpty(request.getParameter("publisher")))) {
      val publisherUrlWords = request.getParameter("publisher")
      resourceDAO.getPublisherByUrlWords(publisherUrlWords).map { publisher =>
        request.setAttribute("publisher", publisher)
      }
    }
  }

}