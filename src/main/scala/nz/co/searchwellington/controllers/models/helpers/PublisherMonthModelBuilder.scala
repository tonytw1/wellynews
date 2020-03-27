package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.model.{User, Website}
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.joda.time.Interval
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.Future

class PublisherMonthModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService) extends ModelBuilder with ArchiveMonth {

  override def isValid(request: HttpServletRequest): Boolean = {
    Option(request.getAttribute("publisher").asInstanceOf[Website]).flatMap { publisher =>
      parseMonth(publisher, request.getContextPath)
    }.nonEmpty
  }

  override def populateContentModel(request: HttpServletRequest, loggedInUser: User): Future[Option[ModelAndView]] = {
    Option(request.getAttribute("publisher").asInstanceOf[Website]).map { publisher =>

      parseMonth(publisher, request.getContextPath).map { month =>
        Future.successful(None)

      }.getOrElse {
        Future.successful(None)
      }

    }.getOrElse {
      Future.successful(None)
    }
  }

  override def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView, loggedInUser: User): Future[ModelAndView] = ???

  override def getViewName(mv: ModelAndView): String = ???

  private def parseMonth(publisher: Website, path: String): Option[Interval] = {
    publisher.url_words.flatMap { publisherUrlWords =>
      if (path.startsWith("/" + publisherUrlWords)) {
        val last = path.split("/").last
        parseYearMonth(last)
      } else {
        None
      }
    }
  }

}
