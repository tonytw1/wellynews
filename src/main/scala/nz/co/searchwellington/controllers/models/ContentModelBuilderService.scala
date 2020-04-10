package nz.co.searchwellington.controllers.models

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.controllers.CommonModelObjectsService
import nz.co.searchwellington.controllers.models.helpers.ContentFields
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.apache.log4j.Logger
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.views.ViewFactory

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ContentModelBuilderService(viewFactory: ViewFactory,
                                 val contentRetrievalService: ContentRetrievalService,
                                 modelBuilders: Seq[ModelBuilder]) extends CommonModelObjectsService with ContentFields {

  private val logger = Logger.getLogger(classOf[ContentModelBuilderService])

  def populateContentModel(request: HttpServletRequest, loggedInUser: User = null): Future[Option[ModelAndView]] = {
    logger.info("Serving path: " + request.getPathInfo)
    modelBuilders.find(mb => {
      val r = mb.isValid(request)
      logger.info(mb.getClass.getCanonicalName + " / " + request.getPathInfo + ": " + r)
      r
    }).map { mb =>
      logger.info("Using " + mb.getClass.getName + " to serve path: " + request.getPathInfo)
      mb.populateContentModel(request, Option(loggedInUser)).flatMap { eventualMaybeModelAndView =>
        eventualMaybeModelAndView.map { mv =>
          val path = request.getPathInfo
          val eventualWithViewAndExtraContent = if (path.endsWith("/rss")) {
            logger.debug("Selecting rss view for path: " + path)
            mv.setView(viewFactory.getRssView(mv.getModel.get("heading").asInstanceOf[String], mv.getModel.get("link").asInstanceOf[String], mv.getModel.get("description").asInstanceOf[String]))
            mv.addObject("data", mv.getModel.get(MAIN_CONTENT))
            Future.successful(Some(mv))

          } else if (path.endsWith("/json")) {
            logger.debug("Selecting json view for path: " + path)
            val jsonView = viewFactory.getJsonView
            jsonView.setDataField(MAIN_CONTENT) // TODO push to a parameter of getJsonView
            mv.setView(jsonView)
            Future.successful(Some(mv))

          } else {
            mb.populateExtraModelContent(request, mv, Option(loggedInUser)).flatMap { mv =>
              mv.setViewName(mb.getViewName(mv))
              withCommonLocal(mv).map { mv =>
                Some(mv)
              }
            }
          }
          eventualWithViewAndExtraContent
        }.getOrElse {
          Future.successful(None)
        }
      }

    }.getOrElse {
      logger.warn("No matching model builder found for path: " + request.getPathInfo)
      Future.successful(None)
    }
  }

}
