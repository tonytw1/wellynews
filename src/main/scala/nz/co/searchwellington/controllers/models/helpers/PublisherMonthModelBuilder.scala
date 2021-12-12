package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.{PublisherArchiveLink, User, Website}
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.joda.time.Interval
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.dates.DateFormatter

import javax.servlet.http.HttpServletRequest
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.jdk.CollectionConverters._

@Component class PublisherMonthModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService,
                                                         frontendResourceMapper: FrontendResourceMapper,
                                                         dateFormatter: DateFormatter, rssUrlBuilder: RssUrlBuilder)
  extends ModelBuilder with ArchiveMonth {

  override def isValid(request: HttpServletRequest): Boolean = {
    Option(request.getAttribute("publisher").asInstanceOf[Website]).flatMap { publisher =>
      parseMonth(publisher, RequestPath.getPathFrom(request))
    }.nonEmpty
  }

  override def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User]): Future[Option[ModelAndView]] = {
    Option(request.getAttribute("publisher").asInstanceOf[Website]).map { publisher =>
      parseMonth(publisher, RequestPath.getPathFrom(request)).map { month =>
        for {
          eventualFrontendWebsite <- frontendResourceMapper.createFrontendResourceFrom(publisher, loggedInUser)
          newsitemsForMonth <- contentRetrievalService.getNewsitemsForPublisherInterval(publisher, month, loggedInUser)
        } yield {
          Some(new ModelAndView().
            addObject("publisher", eventualFrontendWebsite).
            addObject(MAIN_CONTENT, newsitemsForMonth.asJava).
            addObject("heading", publisher.getTitle + " - " + dateFormatter.fullMonthYear(month.getStart.toDate)).
            addObject("month", month).
            addObject("rss_url", rssUrlBuilder.getRssUrlForPublisher(publisher)))
        }

      }.getOrElse {
        Future.successful(None)
      }

    }.getOrElse {
      Future.successful(None)
    }
  }

  override def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView, loggedInUser: Option[User]): Future[ModelAndView] = {
    val publisher = request.getAttribute("publisher").asInstanceOf[Website]
    val frontendPublisher = mv.getModel.get("publisher").asInstanceOf[FrontendResource]
    for {
      archiveLinks <- contentRetrievalService.getPublisherArchiveMonths(publisher, loggedInUser)
    } yield {
      val publisherArchiveLinks = archiveLinks.map { a =>
        PublisherArchiveLink(publisher = frontendPublisher, interval = a.interval, count = a.count)
      }
      mv.addObject("publisher_archive_links", publisherArchiveLinks.asJava)
    }

    Future.successful(mv)
  }

  override def getViewName(mv: ModelAndView, loggedInUser: Option[User]): String = "publisherMonth"

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
