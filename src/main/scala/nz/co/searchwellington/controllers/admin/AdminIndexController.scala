package nz.co.searchwellington.controllers.admin

import io.opentelemetry.api.trace.Span
import jakarta.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.models.helpers.ContentFields
import nz.co.searchwellington.controllers.{LoggedInUserFilter, RequiringLoggedInUser}
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.jdk.CollectionConverters._

@Controller class AdminIndexController @Autowired()(val loggedInUserFilter: LoggedInUserFilter,
                                                    contentRetrievalService: ContentRetrievalService)
  extends RequiringLoggedInUser with ReasonableWaits with ContentFields {

  @GetMapping(Array("/admin"))
  def acceptAllFrom(request: HttpServletRequest): ModelAndView = {
    def show(loggedInUser: User): ModelAndView = {
      implicit val currentSpan: Span = Span.current()

      val withNoDate = Await.result(contentRetrievalService.getWithNoDate(Some(loggedInUser)), TenSeconds)

      new ModelAndView("adminindex").
        addObject("heading", "Admin index").
        addObject(MAIN_CONTENT, withNoDate._1.asJava)
    }
    requiringAdminUser(show)
  }

}
