package nz.co.searchwellington.filters

import javax.servlet.http.HttpServletRequest

import nz.co.searchwellington.repositories.TagDAO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

// TODO depricate be using a url tagname instead of a form parameter - move to adminFilter?
// Used by the rssfeeds index page?
@Component
@Scope("request") class TagParameterFilter @Autowired()(var tagDAO: TagDAO) extends RequestAttributeFilter {
  override def filter(request: HttpServletRequest): Unit = {
    if (request.getParameter("tag") != null) {
      val tagName: String = request.getParameter("tag")
      tagDAO.loadTagByName(tagName).map { tag =>
        request.setAttribute("tag", tag)
      }
    }
  }
}