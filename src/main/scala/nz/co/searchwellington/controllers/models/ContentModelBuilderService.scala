package nz.co.searchwellington.controllers.models

import nz.co.searchwellington.controllers.CommonModelObjectsService
import nz.co.searchwellington.controllers.models.helpers.{ContentFields, ModelBuilder, ProfileModelBuilder}
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.apache.commons.logging.LogFactory
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.views.ViewFactory

import javax.servlet.http.HttpServletRequest
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ContentModelBuilderService(viewFactory: ViewFactory,
                                 val contentRetrievalService: ContentRetrievalService,
                                 modelBuilders: Seq[ModelBuilder]) extends CommonModelObjectsService with ContentFields {

  private val log = LogFactory.getLog(classOf[ContentModelBuilderService])

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User] = None): Future[Option[ModelAndView]] = {
    log.info("Serving path: " + RequestPath.getPathFrom(request))
    modelBuilders.find(mb => mb.isValid(request)).map { mb =>
      log.info("Using " + mb.getClass.getName + " to serve path: " + RequestPath.getPathFrom(request))
      mb.populateContentModel(request, loggedInUser).flatMap { eventualMaybeModelAndView =>
        eventualMaybeModelAndView.map { mv =>
          mv.addObject("loggedInUser", loggedInUser.orNull)

          val path = RequestPath.getPathFrom(request)
          if (path.endsWith("/rss")) {
            Future.successful(Some(rssViewOf(mv)))

          } else if (path.endsWith("/json")) {
            Future.successful(Some(jsonViewOf(mv)))

          } else {
            mb.populateExtraModelContent(request, mv, loggedInUser).flatMap { mv =>
              mv.setViewName(mb.getViewName(mv, loggedInUser))
              withCommonLocal(mv).map { mv =>
                Some(mv)
              }
            }
          }

        }.getOrElse {
          Future.successful(None)
        }
      }

    }.getOrElse {
      log.warn("No matching model builder found for path: " + RequestPath.getPathFrom(request))
      Future.successful(None)
    }
  }

  private def jsonViewOf(mv: ModelAndView): ModelAndView = {
    val jsonView = viewFactory.getJsonView
    jsonView.setDataField(MAIN_CONTENT) // TODO push to a parameter of getJsonView
    new ModelAndView(jsonView).addObject(MAIN_CONTENT, mv.getModel.get(MAIN_CONTENT))
  }

  private def rssViewOf(mv: ModelAndView): ModelAndView = {
    val rssView = viewFactory.getRssView(mv.getModel.get("heading").asInstanceOf[String], mv.getModel.get("link").asInstanceOf[String], mv.getModel.get("description").asInstanceOf[String])
    new ModelAndView(rssView).addObject("data", mv.getModel.get(MAIN_CONTENT))
  }

}
