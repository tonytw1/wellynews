package nz.co.searchwellington.controllers.admin

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.{CommonModelObjectsService, LoggedInUserFilter}
import nz.co.searchwellington.filters.AdminRequestFilter
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.model.{Resource, Tag, User}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.{ContentRetrievalService, HandTaggingService}
import nz.co.searchwellington.repositories.elasticsearch.KeywordSearchService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.tagging.ImpliedTagService
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod}
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

@Controller class AutoTagController @Autowired()(mongoRepository: MongoRepository,
                                                 requestFilter: AdminRequestFilter,
                                                 autoTagService: ImpliedTagService,
                                                 keywordSearchService: KeywordSearchService,
                                                 contentUpdateService: ContentUpdateService,
                                                 loggedInUserFilter: LoggedInUserFilter,
                                                 handTaggingService: HandTaggingService,
                                                 val contentRetrievalService: ContentRetrievalService)
  extends ReasonableWaits with CommonModelObjectsService {

  private val log = Logger.getLogger(classOf[AutoTagController])

  @RequestMapping(Array("/*/autotag")) def prompt(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    Option(loggedInUserFilter.getLoggedInUser).fold {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN)
      null: ModelAndView

    } { loggedInUser =>
      Option(request.getAttribute("tag").asInstanceOf[Tag]).fold {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND)
        null: ModelAndView

      } { tag =>
        Await.result(for {
          suggestions <- getPossibleAutotagResources(loggedInUser, tag)
        } yield {
          import scala.collection.JavaConverters._
          withCommonLocal(new ModelAndView("autoTagPrompt").
            addObject("heading", "Autotagging").
            addObject("tag", tag).
            addObject("resources_to_tag", suggestions.asJava))
        }, TenSeconds)
      }
    }
  }

  @RequestMapping(value = Array("/*/autotag/apply"), method = Array(RequestMethod.POST)) def apply(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val loggedInUser: User = loggedInUserFilter.getLoggedInUser
    if (loggedInUser == null) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN)
      null

    } else {
      requestFilter.loadAttributesOntoRequest(request)
      val tag = request.getAttribute("tag").asInstanceOf[Tag]
      if (tag == null) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND)
        null

      } else {
        val autotaggedResourceIds = request.getParameterValues("autotag")

        def applyTagTo(resource: Resource, tag: Tag): Future[Resource] = {
          autoTagService.alreadyHasTag(resource, tag).flatMap { alreadyHasTag =>
            if (!alreadyHasTag) {
              log.info("Applying tag " + tag.getName + " to:" + resource.title)
              handTaggingService.addTag(loggedInUser, tag, resource)
            }
            contentUpdateService.update(resource).map { _ =>
              resource
            }
          }
        }

        val eventuallyAutoTaggedResources = Future.sequence {
          autotaggedResourceIds.toSeq.map(mongoRepository.getResourceById).map { ero =>
            ero.flatMap { ro =>
              ro.map(applyTagTo(_, tag).map(Some(_))).
                getOrElse(Future.successful(None))
            }
          }
        }

        import scala.collection.JavaConverters._
        val mv = new ModelAndView("autoTagApply").
          addObject("heading", "Autotagging").
          addObject("tag", tag).
          addObject("resources_to_tag", Await.result(eventuallyAutoTaggedResources, ThirtySeconds).flatten.asJava)

        withCommonLocal(mv)
      }
    }
  }

  private def getPossibleAutotagResources(user: User, tag: Tag): Future[Seq[FrontendResource]] = {
    val keywords: Set[String] = Set(tag.autotag_hints, Some(tag.display_name)).flatten
    contentRetrievalService.getResourcesMatchingKeywordsNotTaggedByUser(keywords, user, tag)
  }

}
