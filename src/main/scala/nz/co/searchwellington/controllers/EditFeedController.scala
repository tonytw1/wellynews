package nz.co.searchwellington.controllers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.reading.WhakaokoService
import nz.co.searchwellington.forms.EditFeed
import nz.co.searchwellington.model.{Feed, Resource, UrlWordsGenerator}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{PathVariable, RequestMapping, RequestMethod}
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.{Await, Future}

@Controller
class EditFeedController @Autowired()(contentUpdateService: ContentUpdateService,
                                      mongoRepository: MongoRepository,
                                      urlWordsGenerator: UrlWordsGenerator, urlBuilder: UrlBuilder,
                                      whakaokoService: WhakaokoService,
                                      loggedInUserFilter: LoggedInUserFilter) extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[EditFeedController])

  @RequestMapping(value = Array("/edit-feed/{id}"), method = Array(RequestMethod.GET))
  def prompt(@PathVariable id: String): ModelAndView = {
    val eventualMaybeResource: Future[Option[Resource]] = mongoRepository.getResourceById(id)
    Await.result(eventualMaybeResource, TenSeconds).map { r =>
      r match {
        case f: Feed =>

          val publisher: Option[Resource] = f.publisher.flatMap(pid => Await.result(mongoRepository.getResourceByObjectId(pid), TenSeconds))

          val mv = new ModelAndView("editFeed")
          val editFeed = new EditFeed()
          editFeed.setTitle(f.title.getOrElse(""))
          editFeed.setUrl(f.page.getOrElse(""))
          editFeed.setPublisher(publisher.flatMap(_.title).getOrElse(""))
          editFeed.setAcceptancePolicy(f.acceptance)

          mv.addObject("feed", f)
          mv.addObject("editFeed", editFeed)
          return mv

        case _ =>
          null   // TODO 404
      }

    }.getOrElse {
      null  // TODO 404
    }
  }

}