package nz.co.searchwellington.controllers.admin

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.{CommonModelObjectsService, LoggedInUserFilter, RequiringLoggedInUser}
import nz.co.searchwellington.filters.attributesetters.TagPageAttributeSetter
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.{Resource, Tag, User}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.repositories.{ContentRetrievalService, HandTaggingService}
import nz.co.searchwellington.tagging.ImpliedTagService
import nz.co.searchwellington.views.Errors
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod}
import org.springframework.web.servlet.ModelAndView

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters._

@Controller class AutoTagController @Autowired()(mongoRepository: MongoRepository,
                                                 impliedTagService: ImpliedTagService,
                                                 contentUpdateService: ContentUpdateService,
                                                 val loggedInUserFilter: LoggedInUserFilter,
                                                 handTaggingService: HandTaggingService,
                                                 val contentRetrievalService: ContentRetrievalService,
                                                 frontendResourceMapper: FrontendResourceMapper)
  extends ReasonableWaits with CommonModelObjectsService with RequiringLoggedInUser with Errors {

  private val log = LogFactory.getLog(classOf[AutoTagController])

  @RequestMapping(Array("/*/autotag"), method = Array(RequestMethod.GET))
  def prompt(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    implicit val currentSpan: Span = Span.current()

    def prompt(loggedInUser: User): ModelAndView = {
      Option(request.getAttribute(TagPageAttributeSetter.TAG).asInstanceOf[Tag]).fold {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND)
        NotFound

      } { tag =>
        Await.result(for {
          suggestions <- getPossibleAutotagResources(loggedInUser, tag, Some(loggedInUser)) // TODO double pass
        } yield {
          new ModelAndView("autoTagPrompt").
            addObject("heading", "Autotagging").
            addObject("tag", tag).
            addObject("resources_to_tag", suggestions.asJava)
        }, TenSeconds)
      }
    }

    requiringAdminUser(prompt)
  }

  @RequestMapping(value = Array("/*/autotag"), method = Array(RequestMethod.POST))
  def apply(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    def apply(loggedInUser: User): ModelAndView = {
      Option(request.getAttribute(TagPageAttributeSetter.TAG).asInstanceOf[Tag]).map { tag =>

        def applyTagTo(resource: Resource, tag: Tag): Future[Resource] = {
          // There is no need to apply a tag if it is already implied on this resource (by a publisher or feed tag)
          // TODO The autotagging on acceptances isn't making the same check?
          impliedTagService.alreadyHasTag(resource, tag).flatMap { alreadyHasTag =>
            if (!alreadyHasTag) {
              log.info("Applying tag " + tag.getName + " to:" + resource.title)
              val withTag = handTaggingService.addUserTagging(loggedInUser, tag, resource)
              contentUpdateService.update(withTag).map { u =>
                withTag
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

        val results = Await.result(eventuallyAutoTaggedResources, ThirtySeconds).flatten
        val frontendResults = Await.result(Future.sequence(results.map(r => frontendResourceMapper.createFrontendResourceFrom(r, None))), TenSeconds)
        val mv = new ModelAndView("autoTagApply").
          addObject("heading", "Autotagging").
          addObject("tag", tag).
          addObject("resources_to_tag", frontendResults.asJava)

        mv

      }.getOrElse{
        response.setStatus(HttpServletResponse.SC_NOT_FOUND)
        NotFound
      }
    }

    requiringAdminUser(apply)
  }

  private def getPossibleAutotagResources(user: User, tag: Tag, loggedInUser: Option[User])(implicit currentSpan: Span): Future[Seq[FrontendResource]] = {
    val autotagHints = tag.hints.toSet
    contentRetrievalService.getNewsitemsMatchingKeywordsNotTaggedByUser(autotagHints + tag.display_name, user, tag, loggedInUser)
  }

}
