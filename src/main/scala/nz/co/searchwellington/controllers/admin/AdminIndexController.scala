package nz.co.searchwellington.controllers.admin

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.{LoggedInUserFilter, RequiringLoggedInUser}
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.utils.StringWrangling
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.views.ViewFactory

import javax.servlet.http.HttpServletRequest
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Controller class AdminIndexController @Autowired()(val loggedInUserFilter: LoggedInUserFilter,
                                                    viewFactory: ViewFactory, mongoRepository: MongoRepository)
  extends RequiringLoggedInUser with StringWrangling with ReasonableWaits {

  private val log = Logger.getLogger(classOf[AdminIndexController])

  @GetMapping(Array("/admin"))
  def acceptAllFrom(request: HttpServletRequest): ModelAndView = {
    def show(loggedInUser: User): ModelAndView = {
      new ModelAndView("adminindex").addObject("heading", "Admin index")
    }
    requiringAdminUser(show)
  }

  @GetMapping(Array("/admin/tags"))
  def migrateTags(): ModelAndView = {
    def migrate(loggedInUser: User): ModelAndView = {
      val tags = Await.result(mongoRepository.getAllTags(), TenSeconds)
      tags.foreach { tag =>
        val hints = tag.autotag_hints.map(splitCommaDelimited).getOrElse(Seq.empty)
        tag.copy(hints = hints)
        log.info(s"Tag ${tag.name} migrated hints: $hints")
        mongoRepository.saveTag(tag)
      }
      val view = viewFactory.getJsonView()
      new ModelAndView (view).addObject ("data", "ok")
    }
    requiringAdminUser(migrate)
  }

}
