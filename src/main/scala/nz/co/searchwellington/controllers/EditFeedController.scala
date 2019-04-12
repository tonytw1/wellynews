package nz.co.searchwellington.controllers

import javax.validation.Valid
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.forms.EditFeed
import nz.co.searchwellington.model.{Feed, UrlWordsGenerator}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.{ModelAttribute, PathVariable, RequestMapping, RequestMethod}
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

import scala.concurrent.Await

@Controller
class EditFeedController @Autowired()(contentUpdateService: ContentUpdateService,
                                      mongoRepository: MongoRepository,
                                      urlWordsGenerator: UrlWordsGenerator, urlBuilder: UrlBuilder,
                                      loggedInUserFilter: LoggedInUserFilter) extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[EditFeedController])

  @RequestMapping(value = Array("/edit-feed/{id}"), method = Array(RequestMethod.GET))
  def prompt(@PathVariable id: String): ModelAndView = {
    Await.result(mongoRepository.getResourceById(id), TenSeconds).map { r =>
      r match {
        case f: Feed =>
          val publisher = f.publisher.flatMap(pid => Await.result(mongoRepository.getResourceByObjectId(pid), TenSeconds))

          val editFeed = new EditFeed()
          editFeed.setTitle(f.title.getOrElse(""))
          editFeed.setUrl(f.page.getOrElse(""))
          editFeed.setPublisher(publisher.flatMap(_.title).getOrElse(""))
          editFeed.setAcceptancePolicy(f.acceptance)

          return renderEditForm(f, editFeed)

        case _ =>
          null   // TODO 404
      }

    }.getOrElse {
      null  // TODO 404
    }
  }

  @RequestMapping(value = Array("/edit-feed/{id}"), method = Array(RequestMethod.POST))
  def submit(@PathVariable id: String, @Valid @ModelAttribute("newFeed") editFeed: EditFeed, result: BindingResult): ModelAndView = {
    Await.result(mongoRepository.getResourceById(id), TenSeconds).map { r =>
      r match {
        case f: Feed =>

          if (result.hasErrors) {
            log.warn("Edit feed submission has errors: " + result)
            return renderEditForm(f, editFeed)

          } else {
            log.info("Got valid edit feed submission: " + editFeed)

            val publisherName = if (editFeed.getPublisher.trim.nonEmpty) {
              Some(editFeed.getPublisher.trim)
            } else {
              None
            }
            val publisher = publisherName.flatMap { publisherName =>
              Await.result(mongoRepository.getWebsiteByName(publisherName), TenSeconds)
            }

            val owner = Option(loggedInUserFilter.getLoggedInUser)

            val updatedFeed = f.copy(
              title = Some(editFeed.getTitle),
              page = Some(editFeed.getUrl),
              url_words = Some(urlWordsGenerator.makeUrlWordsFromName(editFeed.getTitle)),
              publisher = publisher.map(_._id),
              acceptance = editFeed.getAcceptancePolicy,
              owner = owner.map(_._id)
            )

            contentUpdateService.update(updatedFeed)
            log.info("Updated feed: " + updatedFeed)

            new ModelAndView(new RedirectView(urlBuilder.getFeedUrl(updatedFeed)))
          }
        case _ =>
          null
      }

    }.getOrElse {
      null
    }
  }

  private def renderEditForm(f: Feed, editFeed: EditFeed): ModelAndView = {
    val mv = new ModelAndView("editFeed")
    mv.addObject("feed", f)
    mv.addObject("editFeed", editFeed)
    return mv
  }

}