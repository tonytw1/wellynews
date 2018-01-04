package nz.co.searchwellington.controllers.models

import nz.co.searchwellington.controllers.CommonModelObjectsService
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.views.ViewFactory
import uk.co.eelpieconsulting.common.views.json.JsonView
import javax.servlet.http.HttpServletRequest

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

    val modelBuilderToUse = modelBuilders.filter(mb => mb.isValid(request)).headOption // TODO collect first?

    val x = modelBuilderToUse.fold {
      logger.warn("No matching model builder found for path: " + request.getPathInfo)
      None // TODO Blocks the above value

    } { mb =>
      logger.info("Using " + modelBuilder.getClass.getName + " to serve path: " + request.getPathInfo)
      modelBuilder.populateContentModel(request).map { mv =>
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
          modelBuilder.populateExtraModelContent(request, mv)
          mv.setViewName(modelBuilder.getViewName(mv))
          commonModelObjectsService.populateCommonLocal(mv)
        }

        mv
      }

    }

    x // TODO
  }

}
