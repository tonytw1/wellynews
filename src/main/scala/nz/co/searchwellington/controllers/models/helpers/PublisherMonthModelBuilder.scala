package nz.co.searchwellington.controllers.models.helpers

import io.opentelemetry.api.trace.Span
import jakarta.servlet.http.HttpServletRequest
import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.filters.attributesetters.PublisherPageAttributeSetter
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.{PublisherArchiveLink, User, Website}
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.joda.time.Interval
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.ui.ModelMap
import uk.co.eelpieconsulting.common.dates.DateFormatter

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

@Component class PublisherMonthModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService,
                                                         frontendResourceMapper: FrontendResourceMapper,
                                                         dateFormatter: DateFormatter, rssUrlBuilder: RssUrlBuilder)
  extends ModelBuilder with ArchiveMonth with ArchiveMonths {

  override def isValid(request: HttpServletRequest): Boolean = {
    Option(request.getAttribute(PublisherPageAttributeSetter.PUBLISHER_ATTRIBUTE).asInstanceOf[Website]).flatMap { publisher =>
      parseMonth(publisher, RequestPath.getPathFrom(request))
    }.nonEmpty
  }

  override def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[Option[ModelMap]] = {
    Option(request.getAttribute(PublisherPageAttributeSetter.PUBLISHER_ATTRIBUTE).asInstanceOf[Website]).map { publisher =>
      parseMonth(publisher, RequestPath.getPathFrom(request)).map { month =>
        for {
          eventualFrontendWebsite <- frontendResourceMapper.createFrontendResourceFrom(publisher, loggedInUser)
          newsitemsForMonth <- contentRetrievalService.getNewsitemsForPublisherInterval(publisher, month, loggedInUser)
        } yield {
          Some(new ModelMap().
            addAttribute("publisher", eventualFrontendWebsite).
            addAttribute(MAIN_CONTENT, newsitemsForMonth.asJava).
            addAttribute("heading", publisher.getTitle + " - " + dateFormatter.fullMonthYear(month.getStart.toDate)).
            addAttribute("month", month).
            addAttribute("rss_url", rssUrlBuilder.getRssUrlForPublisher(publisher)))
        }

      }.getOrElse {
        Future.successful(None)
      }

    }.getOrElse {
      Future.successful(None)
    }
  }

  override def populateExtraModelContent(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[ModelMap] = {
    Option(request.getAttribute(PublisherPageAttributeSetter.PUBLISHER_ATTRIBUTE).asInstanceOf[Website]).flatMap { publisher =>
      parseMonth(publisher, RequestPath.getPathFrom(request)).map { month =>
        val eventualPublisherArchiveMonths = contentRetrievalService.getPublisherArchiveMonths(publisher, loggedInUser)
        val eventualFrontendPublisher = frontendResourceMapper.createFrontendResourceFrom(publisher, loggedInUser)

        for {
          frontendPublisher <- eventualFrontendPublisher
          archiveLinks <- eventualPublisherArchiveMonths
        } yield {
          val publisherArchiveLinks = archiveLinks.map { a =>
            // TODO Are we sure we really need a frontend publisher in this context?
            PublisherArchiveLink(publisher = frontendPublisher, interval = a.interval, count = a.count)
          }
          new ModelMap().addAttribute("archive_links", publisherArchiveLinks.asJava).addAllAttributes(
            populateNextAndPreviousLinks(month, publisherArchiveLinks))

        }
      }
    }.getOrElse(Future.successful(new ModelMap()))
  }

  override def getViewName(mv: ModelMap, loggedInUser: Option[User]): String = "publisherMonth"

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
