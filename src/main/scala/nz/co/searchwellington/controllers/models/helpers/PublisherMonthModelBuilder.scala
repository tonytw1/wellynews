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
    Option(request.getAttribute("publisher").asInstanceOf[Website]).flatMap { publisher =>
      publisher.url_words.flatMap { publisherUrlWords =>
        val path = request.getContextPath
        if (path.startsWith("/" + publisherUrlWords)) {
          val last = path.split("/").last
          parseYearMonth(last)
        } else {
          None
        }
      }
    }.nonEmpty
  }

  override def populateContentModel(request: HttpServletRequest, loggedInUser: User): Future[Option[ModelAndView]] = ???

  override def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView, loggedInUser: User): Future[ModelAndView] = ???

  override def getViewName(mv: ModelAndView): String = ???
}
