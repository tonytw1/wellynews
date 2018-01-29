package nz.co.searchwellington.filters

import javax.servlet.http.HttpServletRequest

import nz.co.searchwellington.repositories.TagDAO
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class TagsParameterFilter @Autowired() (var tagDAO: TagDAO) extends RequestAttributeFilter {

  private val log = Logger.getLogger(classOf[TagsParameterFilter])

  override def filter(request: HttpServletRequest): Unit = {
    if (request.getParameter("tags") != null) {
      val tagNames = request.getParameterValues("tags")
      val tags = tagNames.map { tagName => // TODO cleaning
        tagDAO.loadTagByName(tagName)
      }.flatten.toSeq
      request.setAttribute("tags", tags)
    }
  }

}