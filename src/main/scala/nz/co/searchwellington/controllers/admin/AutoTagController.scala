package nz.co.searchwellington.controllers.admin

import com.google.common.base.Splitter
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.{CommonModelObjectsService, LoggedInUserFilter, RequiringLoggedInUser}
import nz.co.searchwellington.filters.AdminRequestFilter
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.model.{Resource, Tag, User}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.repositories.{ContentRetrievalService, HandTaggingService}
import nz.co.searchwellington.tagging.ImpliedTagService
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod}
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

@Controller class AutoTagController @Autowired()(mongoRepository: MongoRepository,
                                                 requestFilter: AdminRequestFilter,
                                                 autoTagService: ImpliedTagService,
                                                 contentUpdateService: ContentUpdateService,
                                                 val loggedInUserFilter: LoggedInUserFilter,
                                                 handTaggingService: HandTaggingService,
                                                 val contentRetrievalService: ContentRetrievalService)
  extends ReasonableWaits with CommonModelObjectsService with RequiringLoggedInUser {

  private val log = Logger.getLogger(classOf[AutoTagController])

  private val commaSplitter = Splitter.on(",")

  @RequestMapping(Array("/*/autotag")) def prompt(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    def prompt(loggedInUser: User): ModelAndView = {
      Option(request.getAttribute("tag").asInstanceOf[Tag]).fold {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND)
        null: ModelAndView

      } { tag =>
        Await.result((for {
          suggestions <- getPossibleAutotagResources(loggedInUser, tag, Some(loggedInUser)) // TODO double pass
        } yield {
          import scala.collection.JavaConverters._
          new ModelAndView("autoTagPrompt").
            addObject("heading", "Autotagging").
            addObject("tag", tag).
            addObject("resources_to_tag", suggestions.asJava)
        }).flatMap(withCommonLocal), TenSeconds)
      }
    }

    requiringAdminUser(prompt)
  }

  @RequestMapping(value = Array("/*/autotag/apply"), method = Array(RequestMethod.POST)) def apply(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    def apply(loggedInUser: User): ModelAndView = {
      requestFilter.loadAttributesOntoRequest(request)
      Option(request.getAttribute("tag").asInstanceOf[Tag]).map { tag =>

        def applyTagTo(resource: Resource, tag: Tag): Future[Resource] = {
          autoTagService.alreadyHasTag(resource, tag).flatMap { alreadyHasTag =>
            if (!alreadyHasTag) {
              log.info("Applying tag " + tag.getName + " to:" + resource.title)
              val withTags = handTaggingService.addTag(loggedInUser, tag, resource)
              contentUpdateService.update(withTags).map { u =>
                withTags
              }
            } else {
              Future.successful(resource)
            }
          }
        }

        val autotaggedResourceIds = request.getParameterValues("autotag")
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

        Await.result(withCommonLocal(mv), TenSeconds)

      }.getOrElse{
        response.setStatus(HttpServletResponse.SC_NOT_FOUND)  // TODO deduplicate 404 response
        null
      }
    }

    requiringAdminUser(apply)
  }

  private def getPossibleAutotagResources(user: User, tag: Tag, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    import scala.collection.JavaConverters._
    val autotag_hints = tag.autotag_hints.map(hints => commaSplitter.split(hints).asScala).getOrElse(Seq.empty).toSet
    contentRetrievalService.getNewsitemsMatchingKeywordsNotTaggedByUser(autotag_hints + tag.display_name, user, tag, loggedInUser)
  }

}
