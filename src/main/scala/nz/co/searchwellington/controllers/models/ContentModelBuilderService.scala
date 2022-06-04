package nz.co.searchwellington.controllers.models

import nz.co.searchwellington.controllers.CommonModelObjectsService
import nz.co.searchwellington.controllers.models.helpers.{ContentFields, ModelBuilder}
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.apache.commons.logging.LogFactory
import org.joda.time.{DateTime, Duration}
import org.springframework.ui.ModelMap
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
    modelBuilders.find(mb => mb.isValid(request)).map { mb =>
      val path = RequestPath.getPathFrom(request)
      log.info("Using " + mb.getClass.getSimpleName + " to serve path: " + path)
      val start = new DateTime()

      val eventualMaybeModelAndView = mb.populateContentModel(request, loggedInUser).map { maybeModelAndView: Option[ModelAndView] =>
        maybeModelAndView
      }
      val eventualMainWithDuration: Future[(Option[ModelAndView], Long)] = eventualMaybeModelAndView.map { mv =>
        val mainDuration = new Duration(start, new DateTime()).getMillis
        (mv, mainDuration)
      }

      val eventualMaybeExtras = {
        val isHtmlView =  if (path.endsWith("/rss")) {
          false
        } else if (path.endsWith("/json")) {
          false
        } else {
          true
        }
        if (isHtmlView) {
          for {
            extraContent <- mb.populateExtraModelContent(request, loggedInUser)
            commonLocal <- commonLocal
          } yield {
            val extras = new ModelMap()
            extras.addAttribute("loggedInUser", loggedInUser.orNull)
            extras.addAllAttributes(extraContent)
            extras.addAllAttributes(commonLocal)
            Some(extras)
          }
        } else {
          Future.successful(None)
        }
      }
      val eventualExtrasWithDuration = eventualMaybeExtras.map { extras =>
        val extrasDuration = new Duration(start, new DateTime()).getMillis
        log.info("Completed extras for " + path + " after " + extrasDuration + "ms using " + mb.getClass.getSimpleName)
        (extras, extrasDuration)
      }

      for {
        maybeMv <- eventualMainWithDuration
        maybeExtras <- eventualExtrasWithDuration

      } yield {
        maybeMv._1.map { mv =>
          mv.addAllObjects(maybeExtras._1.getOrElse(new ModelMap()))
          log.info("Created mv for " + path + " after " + new Duration(start, new DateTime()).getMillis + "(" +
            maybeMv._2 + " / " + maybeExtras._2 + ")" + "ms using " + mb.getClass.getSimpleName)
          if (path.endsWith("/rss")) {
            rssViewOf(mv)
          } else if (path.endsWith("/json")) {
            jsonViewOf(mv)
          } else {
            mv.setViewName(mb.getViewName(mv, loggedInUser))
            mv
          }
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
