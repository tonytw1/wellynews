package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.model.{User, Website}
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.Future

class PublisherMonthModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService) extends ModelBuilder with ArchiveMonth {

  override def isValid(request: HttpServletRequest): Boolean = {
    val publisher = request.getAttribute("publisher").asInstanceOf[Website]
    if (publisher != null) {
      val path = request.getContextPath
      val str = "/" + publisher.url_words.get
      if (path.startsWith(str)) {
        val last = path.split("/").last
        parseYearMonth(last).nonEmpty
      } else {
        false
      }
    } else {
      false
    }
  }

  override def populateContentModel(request: HttpServletRequest, loggedInUser: User): Future[Option[ModelAndView]] = ???

  override def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView, loggedInUser: User): Future[ModelAndView] = ???

  override def getViewName(mv: ModelAndView): String = ???
}
