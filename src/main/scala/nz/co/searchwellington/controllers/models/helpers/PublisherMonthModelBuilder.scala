package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.{User, Website}
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.apache.log4j.Logger
import org.joda.time.Interval
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


@Component class PublisherMonthModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService, frontendResourceMapper: FrontendResourceMapper)
  extends ModelBuilder with ArchiveMonth {

  val PublisherMonthPath = "/*/[0-9]+-.*?"

  private val logger = Logger.getLogger(classOf[PublisherModelBuilder])

  override def isValid(request: HttpServletRequest): Boolean = {
    val maybeWebsite = Option(request.getAttribute("publisher").asInstanceOf[Website])
    val r = maybeWebsite.flatMap { publisher =>
      parseMonth(publisher, request.getPathInfo)
    }.nonEmpty

    logger.info("Publisher month builder is valid", maybeWebsite, request.getPathInfo, r)
    r
  }

  override def populateContentModel(request: HttpServletRequest, loggedInUser: User): Future[Option[ModelAndView]] = {
    Option(request.getAttribute("publisher").asInstanceOf[Website]).map { publisher =>
      parseMonth(publisher, request.getPathInfo).map { month =>
        for {
          eventualFrontendWebsite <- frontendResourceMapper.createFrontendResourceFrom(publisher)
          newsitemsForMonth <- contentRetrievalService.getNewsitemsForPublisherInterval(publisher, month, Option(loggedInUser))
        } yield {
          import scala.collection.JavaConverters._
          Some(new ModelAndView().
            addObject("publisher", eventualFrontendWebsite).
            addObject(MAIN_CONTENT, newsitemsForMonth.asJava).
            addObject("month", month))
        }

      }.getOrElse {
        Future.successful(None)
      }

    }.getOrElse {
      Future.successful(None)
    }
  }

  override def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView, loggedInUser: User): Future[ModelAndView] = Future.successful(mv)

  override def getViewName(mv: ModelAndView): String = "publisherMonth"

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
