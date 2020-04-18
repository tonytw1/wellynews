package nz.co.searchwellington.controllers.models

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.models.helpers.{CommonSizes, Pagination}
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.{Tag, TagContentCount, User, Website}
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component class SearchModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService, urlBuilder: UrlBuilder, frontendResourceMapper: FrontendResourceMapper)
  extends ModelBuilder with CommonSizes with Pagination with ReasonableWaits {

  private val KEYWORDS_PARAMETER = "keywords"

  def isValid(request: HttpServletRequest): Boolean = {
    request.getParameter(KEYWORDS_PARAMETER) != null
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User]): Future[Option[ModelAndView]] = {
    val keywords = request.getParameter(KEYWORDS_PARAMETER)
    val page = getPage(request)

    val startIndex = getStartIndex(page, MAX_NEWSITEMS)

    val maybeTag = Option(request.getAttribute("tags")).flatMap { t =>
      t.asInstanceOf[Seq[Tag]].headOption
    }
    val maybePublisher = Option(request.getAttribute("publisher").asInstanceOf[Website])
    val mv = new ModelAndView()
    mv.addObject("page", page)

    val eventualMaybePublisher = maybePublisher.map { publisher =>
      println(publisher)
      val eventualResource = frontendResourceMapper.createFrontendResourceFrom(publisher)
      eventualResource.map(Some(_))
    }.getOrElse{
      Future.successful(None)
    }

    for {
      maybeFrontendPublisher <- eventualMaybePublisher
      contentWithCount <- contentRetrievalService.getNewsitemsMatchingKeywords(keywords, startIndex, MAX_NEWSITEMS, loggedInUser, maybeTag, maybePublisher)
    } yield {
      import scala.collection.JavaConverters._
      mv.addObject(MAIN_CONTENT, contentWithCount._1.asJava)

      val contentCount = contentWithCount._2
      mv.addObject("main_content_total", contentCount)  // TODO duplication of pagination?
      populatePagination(mv, startIndex, contentCount, MAX_NEWSITEMS)

      mv.addObject("publisher", maybeFrontendPublisher.orNull)

      maybeTag.map { tag =>
        mv.addObject("tag", tag)
      }
      val tagRefinements: Seq[TagContentCount] = maybeTag.fold {
        contentRetrievalService.getKeywordSearchFacets(keywords)
      }{ _ =>
        Seq.empty
      }
      if (tagRefinements.nonEmpty) {
        mv.addObject("related_tags", tagRefinements.asJava)
      }

      /*
    if (startIndex > contentCount) {
      return null
    }
    */

      mv.addObject("query", keywords)
      mv.addObject("heading", "Search results - " + keywords)

      mv.addObject("main_heading", "Matching Newsitems")
      mv.addObject("main_description", "Found " + contentCount + " matching newsitems")
      mv.addObject("description", "Search results for '" + keywords + "'")
      mv.addObject("link", urlBuilder.getSearchUrlFor(keywords))

      Some(mv)
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView, loggedInUser: Option[User]): Future[ModelAndView] = {
    withLatestNewsitems(mv, loggedInUser)
  }

  def getViewName(mv: ModelAndView): String = "search"

}
