package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.{Tag, User, Website}
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import javax.servlet.http.HttpServletRequest
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.jdk.CollectionConverters._

@Component class SearchModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService,
                                                 val urlBuilder: UrlBuilder, frontendResourceMapper: FrontendResourceMapper)
  extends ModelBuilder with CommonSizes with Pagination with ReasonableWaits {

  private val log = LogFactory.getLog(classOf[SearchModelBuilder])
  private val KEYWORDS_PARAMETER = "q"

  def isValid(request: HttpServletRequest): Boolean = {
    request.getParameter(KEYWORDS_PARAMETER) != null
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User]): Future[Option[ModelAndView]] = {
    val keywords = request.getParameter(KEYWORDS_PARAMETER)
    val page = getPage(request)

    val startIndex = getStartIndex(page, MAX_NEWSITEMS)

    val maybeTag = Option(request.getAttribute("tag")).map(_.asInstanceOf[Tag])
    val maybePublisher = Option(request.getAttribute("publisher").asInstanceOf[Website])

    log.info("Search parameters: ", keywords, maybeTag, maybePublisher)

    val eventualMaybeFrontendPublisher = maybePublisher.map { publisher =>
      val eventualResource = frontendResourceMapper.createFrontendResourceFrom(publisher, loggedInUser)
      eventualResource.map(Some(_))
    }.getOrElse{
      Future.successful(None)
    }

    val shouldShowRefinements = maybeTag.isEmpty && maybePublisher.isEmpty
    val eventualTagRefinements = if (shouldShowRefinements) contentRetrievalService.getNewsitemKeywordSearchRelatedTags(keywords, loggedInUser) else Future.successful(Seq.empty)
    val eventualPublisherRefinements = if (shouldShowRefinements) contentRetrievalService.getNewsitemKeywordSearchRelatedPublishers(keywords, loggedInUser) else Future.successful(Seq.empty)
    val eventualMatchingPublishers = {
      if (maybePublisher.isEmpty) {
        contentRetrievalService.getWebsitesMatchingKeywords(keywords, maybeTag, 0, MAX_NEWSITEMS, loggedInUser)
      } else {
        Future.successful((Seq.empty, 0L))
      }
    }

    for {
      newsitemsWithCount <- contentRetrievalService.getNewsitemsMatchingKeywords(keywords, startIndex, MAX_NEWSITEMS, loggedInUser, maybeTag, maybePublisher)
      matchingWebsites <- eventualMatchingPublishers
      maybeFrontendPublisher <- eventualMaybeFrontendPublisher
      tagRefinements <- eventualTagRefinements
      publisherRefinements <- eventualPublisherRefinements

    } yield {
      val mv = new ModelAndView().
        addObject("page", page).
        addObject("heading", "Search results - " + keywords).
        addObject(MAIN_CONTENT, newsitemsWithCount._1.asJava).
        addObject("main_heading", "Matching Newsitems").
        addObject("query", keywords).
        addObject("tag", maybeTag.orNull).
        addObject("publisher", maybeFrontendPublisher.orNull)

      if (matchingWebsites._2 > 0) {
        mv.addObject("secondary_heading", "Matching websites")
        mv.addObject("secondary_content", matchingWebsites._1.asJava)
      }
      if (tagRefinements.nonEmpty) {
        mv.addObject("related_tags", tagRefinements.asJava)
      }
      if (publisherRefinements.nonEmpty) {
        mv.addObject("related_publishers", publisherRefinements.asJava)
      }

      val contentCount = newsitemsWithCount._2
      def paginationLinks(page: Int) = urlBuilder.getSearchUrlFor(keywords, Some(page))
      populatePagination(mv, startIndex, contentCount, MAX_NEWSITEMS, paginationLinks)

      /*
    if (startIndex > contentCount) {
      return null
    }
    */

      mv.addObject("main_description", "Found " + contentCount + " matching newsitems")
      mv.addObject("description", "Search results for '" + keywords + "'")
      mv.addObject("link", urlBuilder.fullyQualified(urlBuilder.getSearchUrlFor(keywords)))

      Some(mv)
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView, loggedInUser: Option[User]): Future[ModelAndView] = {
    withLatestNewsitems(mv, loggedInUser)
  }

  def getViewName(mv: ModelAndView, loggedInUser: Option[User]): String = "search"

}
