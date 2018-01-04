package nz.co.searchwellington.controllers.models

import javax.servlet.http.HttpServletRequest

import nz.co.searchwellington.controllers.CommonModelObjectsService
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.views.ViewFactory

@Component class ContentModelBuilderService @Autowired() (viewFactory: ViewFactory, jsonCallbackNameValidator: JsonCallbackNameValidator, commonModelObjectsService: CommonModelObjectsService, modelBuilders: ModelBuilder*) {

  private val logger = Logger.getLogger(classOf[ContentModelBuilderService])
  private val JSON_CALLBACK_PARAMETER = "callback"

  def populateContentModel(request: HttpServletRequest): Option[ModelAndView] = {

    def populateJsonCallback(request: HttpServletRequest, mv: ModelAndView) = if (request.getParameter(JSON_CALLBACK_PARAMETER) != null) {
      // TODO use CORS
      val callback = request.getParameter(JSON_CALLBACK_PARAMETER)
      if (jsonCallbackNameValidator.isValidCallbackName(callback)) {
        logger.debug("Adding callback to model:" + callback)
        mv.addObject(JSON_CALLBACK_PARAMETER, callback)
      }
    }

    val modelBuilderToUse: Option[ModelBuilder] = modelBuilders.filter(mb => mb.isValid(request)).headOption // TODO collect first?

    modelBuilderToUse.map { mb =>
      logger.info("Using " + mb.getClass.getName + " to serve path: " + request.getPathInfo)

      mb.populateContentModel(request).map { mv =>
        val path = request.getPathInfo

        if (path.endsWith("/rss")) {
          logger.debug("Selecting rss view for path: " + path)
          mv.setView(viewFactory.getRssView(mv.getModel.get("heading").asInstanceOf[String], mv.getModel.get("link").asInstanceOf[String], mv.getModel.get("description").asInstanceOf[String]))
          mv.addObject("data", mv.getModel.get("main_content"))

        } else if (path.endsWith("/json")) {
            logger.debug("Selecting json view for path: " + path)
            val jsonView = viewFactory.getJsonView
            jsonView.setDataField("main_content")
            mv.setView(jsonView)
            populateJsonCallback(request, mv)

        } else {
          mb.populateExtraModelContent(request, mv)
          mv.setViewName(mb.getViewName(mv))
          commonModelObjectsService.populateCommonLocal(mv)
        }

        mv
      }

    }.getOrElse {
      logger.warn("No matching model builder found for path: " + request.getPathInfo)
      None
    }
  }

}
